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
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.yamcs.client.ClientException;
import org.yamcs.client.YamcsClient;
import org.yamcs.client.mdb.MissionDatabaseClient.ListOptions;
import org.yamcs.protobuf.GetServerInfoResponse;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.UserInfo;

/**
 * A job that helps to switch context (instance/processor) while keeping the same YamcsClient.
 */
public class ContextSwitcher implements IRunnableWithProgress {

    private static final Logger log = Logger.getLogger(ContextSwitcher.class.getName());

    private YamcsClient yamcsClient;
    private GetServerInfoResponse serverInfo;
    private UserInfo userInfo;

    private String instance; // If null, connect without any context
    private String processor; // If null, use default processor for that instance

    /**
     * Switch context to a new instance, using a specific processor.
     */
    public ContextSwitcher(String instance, String processor) {
        yamcsClient = Objects.requireNonNull(YamcsPlugin.getYamcsClient());
        serverInfo = Objects.requireNonNull(YamcsPlugin.getServerInfo());
        userInfo = Objects.requireNonNull(YamcsPlugin.getUser());
        this.instance = instance;
        this.processor = processor;
    }

    /**
     * Switch context to a new instance, using the default processor for that instance.
     */
    public ContextSwitcher(String instance) {
        this(instance, null);
    }

    /**
     * Remove any current context (no instance, and no processor)
     */
    public ContextSwitcher() {
        this(null, null);
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            if (instance == null) {
                monitor.beginTask("Clearing context", IProgressMonitor.UNKNOWN);
                runBefore(monitor);
                if (!monitor.isCanceled()) {
                    clearContext(monitor);
                }
            } else {
                monitor.beginTask("Switching context", IProgressMonitor.UNKNOWN);
                runBefore(monitor);
                if (!monitor.isCanceled()) {
                    switchContext(monitor);
                }
            }
        } catch (CancellationException e) {
            log.fine("Job cancelled");
        } catch (java.util.concurrent.ExecutionException e) {
            if (e.getCause() instanceof ClientException) {
                var clientException = (ClientException) e.getCause();
                var detail = clientException.getDetail();
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Could not switch context", detail.getMessage());
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Could not switch context", e.getMessage());
                });
            }
        }
        monitor.done();
    }

    /**
     * Extension hook to run actions before switching contexts
     */
    protected void runBefore(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException {
    }

    private void clearContext(IProgressMonitor monitor) {
        var holder = new RemoteEntityHolder();
        holder.yamcsClient = yamcsClient;
        holder.serverInfo = serverInfo;
        holder.userInfo = userInfo;
        log.info("Clearing context");
        YamcsPlugin.updateEntities(holder);
    }

    private void switchContext(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException, CancellationException {
        var processorInfo = findProcessorInfo(monitor);
        var missionDatabase = loadMissionDatabase(monitor);

        var holder = new RemoteEntityHolder();
        holder.yamcsClient = yamcsClient;
        holder.serverInfo = serverInfo;
        holder.userInfo = userInfo;
        holder.missionDatabase = missionDatabase;
        holder.instance = processorInfo.getInstance();
        holder.processor = processorInfo;
        log.info(String.format("Switching to '%s' processor (instance: %s)", processorInfo.getName(),
                processorInfo.getInstance()));
        YamcsPlugin.updateEntities(holder);
    }

    private ProcessorInfo findProcessorInfo(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException, CancellationException {
        monitor.subTask("Determining processor");
        var processorsFuture = yamcsClient.listProcessors(instance);
        RCPUtils.monitorCancellableFuture(monitor, processorsFuture);
        var processors = processorsFuture.get();

        if (processor != null) {
            return processors.stream().filter(processor -> processor.getName().equals(this.processor)).findFirst()
                    .get();
        } else {
            return processors.stream().filter(processor -> processor.getPersistent() && !processor.getReplay())
                    .findFirst().get();
        }
    }

    private MissionDatabase loadMissionDatabase(IProgressMonitor monitor)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        monitor.subTask("Loading mission database");
        var missionDatabase = new MissionDatabase();

        var mdbClient = yamcsClient.createMissionDatabaseClient(instance);
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
    }
}
