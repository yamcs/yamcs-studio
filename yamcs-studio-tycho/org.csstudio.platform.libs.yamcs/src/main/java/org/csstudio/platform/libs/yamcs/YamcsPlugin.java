package org.csstudio.platform.libs.yamcs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.web.ResponseHandler;
import org.csstudio.platform.libs.yamcs.web.RestClient;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestDumpRawMdbRequest;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.XtceDb;

import com.google.protobuf.MessageLite;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.csstudio.utility.platform.libs.yamcs";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    // The shared instance
    private static YamcsPlugin plugin;
    private BundleListener bundleListener;

    private RestClient restClient;

    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private List<NamedObjectId> parameterIds = Collections.emptyList();
    private CountDownLatch parametersLoaded = new CountDownLatch(1);

    private Collection<MetaCommand> commands = Collections.emptyList();
    private CountDownLatch commandsLoaded = new CountDownLatch(1);

    public RestClient getRestClient() {
        return restClient;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
        int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
        String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
        restClient = new RestClient(new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance));

        // Only load MDB once bundle has been fully started
        bundleListener = event -> {
            if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
                // Extra check, bundle may have been shut down between the
                // time this event was queued and now
                if (getBundle().getState() == Bundle.ACTIVE) {
                    log.info("loading mdb async?..");
                    fetchInitialMdbAsync();
                    log.info("after fetch async");
                }
            }
        };
        context.addBundleListener(bundleListener);

        System.out.println("At least we got to the end of the start()");
    }

    /**
     * Returns the MDB namespace as defined in the user preferences.
     */
    public String getMdbNamespace() {
        return YamcsPlugin.getDefault().getPreferenceStore().getString("mdb_namespace");
    }

    private void fetchInitialMdbAsync() {
        // Load list of parameters
        RestListAvailableParametersRequest.Builder req = RestListAvailableParametersRequest.newBuilder();
        req.addNamespaces(getMdbNamespace());
        restClient.listAvailableParameters(req.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                if (responseMsg instanceof RestExceptionMessage) {
                    log.log(Level.WARNING, "Exception returned by server: " + responseMsg);
                } else {
                    RestListAvailableParametersResponse response = (RestListAvailableParametersResponse) responseMsg;
                    Display.getDefault().asyncExec(() -> {
                        parameterIds = response.getIdsList();
                        for (MDBContextListener l : mdbListeners) {
                            l.onParametersChanged(parameterIds);
                        }
                        parametersLoaded.countDown();
                    });
                }
            }

            @Override
            public void onFault(Throwable t) {
                log.log(Level.SEVERE, "Could not fetch available yamcs parameters", t);
            }
        });

        System.out.println("parameters.done, continuing to raw dump");

        // Load commands
        RestDumpRawMdbRequest.Builder dumpRequest = RestDumpRawMdbRequest.newBuilder();
        restClient.dumpRawMdb(dumpRequest.build(), new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                if (responseMsg instanceof RestExceptionMessage) {
                    log.log(Level.WARNING, "Exception returned by server: " + responseMsg);
                } else {
                    RestDumpRawMdbResponse response = (RestDumpRawMdbResponse) responseMsg;
                    try (ObjectInputStream oin = new ObjectInputStream(response.getRawMdb().newInput())) {
                        XtceDb mdb = (XtceDb) oin.readObject();
                        Display.getDefault().asyncExec(() -> {
                            commands = mdb.getMetaCommands();
                            for (MDBContextListener l : mdbListeners) {
                                l.onCommandsChanged(commands);
                            }
                            commandsLoaded.countDown();
                        });
                    } catch (IOException | ClassNotFoundException e) {
                        log.log(Level.SEVERE, "Could not deserialize mdb", e);
                    }
                }
            }

            @Override
            public void onFault(Throwable t) {
                log.log(Level.SEVERE, "Could not fetch available yamcs commands", t);
            }
        });

        System.out.println("raw dump done, continuing to commands");
    }

    public void addMdbListener(MDBContextListener listener) {
        mdbListeners.add(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            if (bundleListener != null) {
                context.removeBundleListener(bundleListener);
            }
            plugin = null;
            restClient.shutdown();
            YamcsWebSocketRegistrar.getInstance().shutdown();
        } finally {
            super.stop(context);
        }
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }

    /**
     * Return the available parameters
     */
    public List<NamedObjectId> getParameterIds() {
        return parameterIds;
    }

    public Collection<MetaCommand> getCommands() {
        return commands;
    }
}
