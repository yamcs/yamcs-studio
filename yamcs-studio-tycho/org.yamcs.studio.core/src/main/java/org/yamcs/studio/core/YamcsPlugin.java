package org.yamcs.studio.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.utils.TimeEncoding;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.XtceDb;

import com.google.protobuf.MessageLite;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    private static YamcsPlugin plugin;

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    private TimeCatalogue timeCatalogue;
    private ManagementCatalogue managementCatalogue;
    private ConnectionManager connectionManager;

    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private XtceDb mdb;
    private List<RestParameter> parameters = Collections.emptyList();
    private Collection<MetaCommand> commands = Collections.emptyList();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        log.info("Yamcs Studio v." + getBundle().getVersion().toString());
        TimeEncoding.setUp();
        connectionManager = new ConnectionManager();

        timeCatalogue = new TimeCatalogue();
        connectionManager.addStudioConnectionListener(timeCatalogue);

        managementCatalogue = new ManagementCatalogue();
        connectionManager.addStudioConnectionListener(managementCatalogue);
    }

    public static int getNextCommandClientId() {
        return cmdClientId.incrementAndGet();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public ManagementCatalogue getManagementCatalogue() {
        return managementCatalogue;
    }

    public TimeCatalogue getTimeCatalogue() {
        return timeCatalogue;
    }

    public String getOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    public void addMdbListener(MDBContextListener listener) {
        mdbListeners.add(listener);
    }

    public void removeMdbListener(MDBContextListener listener) {
        mdbListeners.remove(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            plugin = null;
            connectionManager.shutdown();
        } finally {
            super.stop(context);
        }
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }

    public List<RestParameter> getParameters() {
        return parameters;
    }

    public Collection<MetaCommand> getCommands() {
        return commands;
    }

    public XtceDb getMdb() {
        return mdb;
    }

    void loadParameters() {
        log.fine("Fetching available parameters");
        RestListAvailableParametersRequest.Builder req = RestListAvailableParametersRequest.newBuilder();
        connectionManager.getRestClient().listAvailableParameters(req.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                RestListAvailableParametersResponse response = (RestListAvailableParametersResponse) responseMsg;
                Display.getDefault().asyncExec(() -> {
                    parameters = response.getParametersList();
                    mdbListeners.forEach(l -> l.onParametersChanged(parameters));
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs parameters", e);
            }
        });
    }

    void loadCommands() {
        log.fine("Fetching available commands");
        connectionManager.getRestClient().dumpRawMdb(new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                RestDumpRawMdbResponse response = (RestDumpRawMdbResponse) responseMsg;
                try (ObjectInputStream oin = new ObjectInputStream(response.getRawMdb().newInput())) {
                    XtceDb newMdb = (XtceDb) oin.readObject();
                    Display.getDefault().asyncExec(() -> {
                        mdb = newMdb;
                        commands = mdb.getMetaCommands();
                        mdbListeners.forEach(l -> l.onCommandsChanged(commands));
                    });
                } catch (IOException | ClassNotFoundException e) {
                    log.log(Level.SEVERE, "Could not deserialize mdb", e);
                    Display.getDefault().asyncExec(() -> {
                        MessageDialog.openError(Display.getDefault().getActiveShell(),
                                "Incompatible Yamcs Server", "Could not interpret Mission Database. "
                                        + "This usually happens when Yamcs Studio is not, or no longer, "
                                        + "compatible with Yamcs Server.");
                    });
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs commands", e);
            }
        });
    }
}
