package org.yamcs.studio.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    private static YamcsPlugin plugin;

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    private TimeCatalogue timeCatalogue;
    private ManagementCatalogue managementCatalogue;
    private CommandingCatalogue commandingCatalogue;
    private ConnectionManager connectionManager;

    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private List<RestParameter> parameters = Collections.emptyList();

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

        commandingCatalogue = new CommandingCatalogue();
        connectionManager.addStudioConnectionListener(commandingCatalogue);
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

    public CommandingCatalogue getCommandingCatalogue() {
        return commandingCatalogue;
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
}
