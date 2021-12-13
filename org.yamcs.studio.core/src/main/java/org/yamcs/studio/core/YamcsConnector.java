/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.UserInfo;
import org.yamcs.protobuf.YamcsInstance;
import org.yamcs.studio.connect.YamcsConfiguration;
import org.yamcs.studio.connect.YamcsConfiguration.AuthType;

public class YamcsConnector implements IRunnableWithProgress {

    private static final Logger log = Logger.getLogger(YamcsConnector.class.getName());

    private Shell shell;
    private YamcsConfiguration conf;

    public YamcsConnector(Shell shell, YamcsConfiguration yprops) {
        this.shell = shell;
        conf = yprops;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        YamcsPlugin.listeners().forEachRemaining(YamcsAware::onYamcsConnecting);

        // All the things we want to fetch from Yamcs
        // (when the whole setup is successful, these get stored in YamcsPlugin
        var holder = new RemoteEntityHolder();

        monitor.beginTask("Connecting to " + conf, IProgressMonitor.UNKNOWN);
        try {
            holder.yamcsClient = doConnect(monitor);
            holder.serverInfo = fetchServerInfo(monitor, holder.yamcsClient);
            holder.userInfo = fetchUserInfo(monitor, holder.yamcsClient);

            if (conf.getInstance() == null || conf.getInstance().trim().isEmpty()) {
                // We allow this for when studio is used to make new sessions from a bare Yamcs.
                log.warning("Connected to Yamcs but no instance was specified in the login dialog");
            } else {
                var instanceInfo = verifyInstance(monitor, holder.yamcsClient, conf.getInstance().trim());
                holder.instance = instanceInfo.getName();

                if (instanceInfo.getProcessorsCount() > 0) {
                    holder.processor = instanceInfo.getProcessors(0);
                } else {
                    log.warning("Instance '" + holder.instance + "' does not have any active processors");
                }

                holder.missionDatabase = loadMissionDatabase(monitor, holder.yamcsClient, holder.instance);
            }

            YamcsPlugin.updateEntities(holder);
            YamcsPlugin.listeners().forEachRemaining(YamcsAware::onYamcsConnected);
        } catch (BootstrapException e) {
            if (holder.yamcsClient != null) {
                holder.yamcsClient.close();
            }
            YamcsPlugin.listeners().forEachRemaining(l -> l.onYamcsConnectionFailed(e));
            log.log(Level.SEVERE, "Exception while connecting to Yamcs: " + e.getCause().getMessage(), e);
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openError(shell, "Failed to connect", e.getCause().getMessage());
            });
        }
        monitor.done();
    }

    private YamcsClient doConnect(IProgressMonitor monitor) throws InterruptedException, BootstrapException {
        try {
            var clientBuilder = YamcsClient.newBuilder(conf.getURL()).withVerifyTls(false)
                    .withUserAgent(YamcsPlugin.getProductString());

            if (conf.getCaCertFile() != null) {
                clientBuilder.withCaCertFile(Paths.get(conf.getCaCertFile()));
            }
            var yamcsClient = clientBuilder.build();

            log.info("Connecting to " + conf);
            if (conf.getAuthType() == AuthType.KERBEROS) {
                yamcsClient.loginWithKerberos();
                yamcsClient.connectWebSocket();
            } else if (conf.getUser() == null) {
                yamcsClient.connectWebSocket();
            } else {
                var password = conf.getTransientPassword();
                if (password != null && !password.isEmpty()) {
                    yamcsClient.login(conf.getUser(), password.toCharArray());
                    yamcsClient.connectWebSocket();
                } else {
                    throw new ClientException("No password was provided");
                }
            }

            return yamcsClient;
        } catch (ClientException e) {
            throw new BootstrapException("Failed to connect", e);
        }
    }

    private GetServerInfoResponse fetchServerInfo(IProgressMonitor monitor, YamcsClient client)
            throws InterruptedException, BootstrapException {
        monitor.subTask("Fetching server info");
        try {
            return client.getServerInfo().get();
        } catch (ExecutionException e) {
            throw new BootstrapException("Cannot fetch server info", e);
        }
    }

    private UserInfo fetchUserInfo(IProgressMonitor monitor, YamcsClient client)
            throws InterruptedException, BootstrapException {
        monitor.subTask("Fetching user info");
        try {
            return client.getOwnUserInfo().get();
        } catch (ExecutionException e) {
            throw new BootstrapException("Cannot fetch user info", e);
        }
    }

    private YamcsInstance verifyInstance(IProgressMonitor monitor, YamcsClient client, String instance)
            throws InterruptedException, BootstrapException {
        monitor.subTask("Attaching to '" + instance + "' instance");
        try {
            return client.getInstance(instance).get();
        } catch (ExecutionException e) {
            throw new BootstrapException("Cannot attach to instance '" + instance + "'", e);
        }
    }

    private MissionDatabase loadMissionDatabase(IProgressMonitor monitor, YamcsClient client, String instance)
            throws InterruptedException, BootstrapException {
        monitor.subTask("Loading mission database");
        var missionDatabase = new MissionDatabase();

        var mdbClient = client.createMissionDatabaseClient(instance);
        try {
            log.fine("Fetching available parameters");
            var page = mdbClient.listParameters(ListOptions.limit(500)).get();
            page.iterator().forEachRemaining(missionDatabase::addParameter);
            while (page.hasNextPage()) {
                page = page.getNextPage().get();
                page.iterator().forEachRemaining(missionDatabase::addParameter);
            }

            log.fine("Fetching available commands");
            var commandPage = mdbClient.listCommands(ListOptions.limit(200)).get();
            commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
            while (commandPage.hasNextPage()) {
                commandPage = commandPage.getNextPage().get();
                commandPage.iterator().forEachRemaining(missionDatabase::addCommand);
            }
            log.info(String.format("Loaded %d parameters and %d commands", missionDatabase.getParameterCount(),
                    missionDatabase.getCommandCount()));
            return missionDatabase;
        } catch (ExecutionException e) {
            throw new BootstrapException("Failed to load mission database", e);
        }
    }

    @SuppressWarnings("serial")
    private static class BootstrapException extends Exception {

        BootstrapException(String message, ExecutionException e) {
            super(message, e.getCause());
        }

        BootstrapException(String message, ClientException e) {
            super(message, e);
        }
    }
}
