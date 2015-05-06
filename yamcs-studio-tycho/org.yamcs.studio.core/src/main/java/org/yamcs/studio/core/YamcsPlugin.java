package org.yamcs.studio.core;

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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestDumpRawMdbRequest;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;
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
    private WebSocketRegistrar webSocketClient;
    private YamcsConnectData hornetqProps;
    private ClientInfo clientInfo;

    private XtceDb mdb;
    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private List<RestParameter> parameters = Collections.emptyList();
    private CountDownLatch parametersLoaded = new CountDownLatch(1);

    private Collection<MetaCommand> commands = Collections.emptyList();
    private CountDownLatch commandsLoaded = new CountDownLatch(1);

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        TimeEncoding.setUp();

        String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
        int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
        String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
        YamcsConnectionProperties yprops = new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance);
        hornetqProps = new YamcsConnectData();
        hornetqProps.host = yamcsHost;
        hornetqProps.port = 5445;
        hornetqProps.instance = yamcsInstance;

        restClient = new RestClient(yprops);
        webSocketClient = new WebSocketRegistrar(yprops);
        addMdbListener(webSocketClient);

        // Only load MDB once bundle has been fully started
        bundleListener = event -> {
            if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
                // Extra check, bundle may have been shut down between the
                // time this event was queued and now
                if (getBundle().getState() == Bundle.ACTIVE) {
                    fetchInitialMdbAsync();
                    webSocketClient.addClientInfoListener(clientInfo -> this.clientInfo = clientInfo);
                    webSocketClient.connect();
                }
            }
        };
        context.addBundleListener(bundleListener);
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public WebSocketRegistrar getWebSocketClient() {
        return webSocketClient;
    }

    public YamcsConnectData getHornetqConnectionProperties() {
        return hornetqProps;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public String getInstance() {
        return YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
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
                        parameters = response.getParametersList();
                        mdbListeners.forEach(l -> l.onParametersChanged(parameters));
                        parametersLoaded.countDown();
                    });
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs parameters", e);
            }
        });

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
                        XtceDb newMdb = (XtceDb) oin.readObject();
                        Display.getDefault().asyncExec(() -> {
                            mdb = newMdb;
                            commands = mdb.getMetaCommands();
                            mdbListeners.forEach(l -> l.onCommandsChanged(commands));
                            commandsLoaded.countDown();
                        });
                    } catch (IOException | ClassNotFoundException e) {
                        log.log(Level.SEVERE, "Could not deserialize mdb", e);
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs commands", e);
            }
        });
    }

    public void addMdbListener(MDBContextListener listener) {
        mdbListeners.add(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            if (bundleListener != null)
                context.removeBundleListener(bundleListener);
            plugin = null;
            mdbListeners.clear();
            restClient.shutdown();
            webSocketClient.shutdown();
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
    public List<RestParameter> getParameters() {
        return parameters;
    }

    public Collection<MetaCommand> getCommands() {
        return commands;
    }

    public XtceDb getMdb() {
        return mdb;
    }
}
