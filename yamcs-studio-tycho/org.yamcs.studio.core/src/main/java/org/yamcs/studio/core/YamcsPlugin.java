package org.yamcs.studio.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestDumpRawMdbRequest;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestListAvailableParametersRequest;
import org.yamcs.protobuf.Rest.RestListAvailableParametersResponse;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;
import org.yamcs.xtce.MetaCommand;
import org.yamcs.xtce.XtceDb;

import com.google.protobuf.MessageLite;

public class YamcsPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.core";
    private static final Logger log = Logger.getLogger(YamcsPlugin.class.getName());

    // The shared instance
    private static YamcsPlugin plugin;
    private BundleListener bundleListener;

    private YProcessorControlClient processorControlClient;
    private RestClient restClient;
    private WebSocketRegistrar webSocketClient;

    private ClientInfo clientInfo;

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();
    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private XtceDb mdb;
    private List<RestParameter> parameters = Collections.emptyList();
    private Collection<MetaCommand> commands = Collections.emptyList();

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();

        // This functionality only available in HornetQ for now. Gives us processor state. And updates on clients
        processorControlClient = new YProcessorControlClient();

        YamcsConnectionProperties webProps = getWebProperties();
        restClient = new RestClient(webProps);
        webSocketClient = new WebSocketRegistrar(webProps);
        addMdbListener(webSocketClient);

        // Only connect once bundle has been fully started
        bundleListener = event -> {
            if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
                // Bundle may have been shut down between the time this event was queued and now
                if (getBundle().getState() == Bundle.ACTIVE) {
                    // We use this one response as a signal, after which we start other clients as well
                    webSocketClient.addClientInfoListener(clientInfo -> setupConnections(clientInfo));
                    webSocketClient.connect();
                }
            }
        };
        context.addBundleListener(bundleListener);
    }

    // Likely not on the swt thread
    private void setupConnections(ClientInfo clientInfo) {
        log.fine(String.format("Got back clientInfo %s", clientInfo));
        // Need to improve this code. Currently doesn't support changing connections
        boolean doSetup = (this.clientInfo == null);
        this.clientInfo = clientInfo;
        if (doSetup) {
            loadParameters();
            loadCommands();

            studioConnectionListeners.forEach(l -> {
                l.processConnectionInfo(clientInfo, getWebProperties(), getHornetqProperties());
            });
        }
        ///processorListeners.forEach(l -> l.onProcessorSwitch(clientInfo.getProcessorName()));
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public WebSocketRegistrar getWebSocketClient() {
        return webSocketClient;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public ProcessorInfo getProcessorInfo(String processorName) {
        return processorControlClient.getProcessorInfo(processorName);
    }

    private YamcsConnectionProperties getWebProperties() {
        return new YamcsConnectionProperties(getHost(), getWebPort(), getInstance());
    }

    private YamcsConnectData getHornetqProperties() {
        YamcsConnectData hornetqProps = new YamcsConnectData();
        hornetqProps.host = getHost();
        hornetqProps.port = 5445;
        hornetqProps.instance = getInstance();
        return hornetqProps;
    }

    public String getInstance() {
        return YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
    }

    public String getHost() {
        return YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
    }

    public int getWebPort() {
        return YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
    }

    public String getMdbNamespace() {
        return YamcsPlugin.getDefault().getPreferenceStore().getString("mdb_namespace");
    }

    private void loadParameters() {
        log.fine("Fetching available parameters");
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
                    });
                }
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fetch available yamcs parameters", e);
            }
        });
    }

    private void loadCommands() {
        log.fine("Fetching available commands");
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

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.add(listener);
        if (clientInfo != null)
            listener.processConnectionInfo(clientInfo, getWebProperties(), getHornetqProperties());
    }

    public void addProcessorListener(ProcessorListener listener) {
        processorControlClient.addProcessorListener(listener);
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
            //studioConnectionListeners.clear();
            //mdbListeners.clear();
            processorControlClient.close();
            restClient.shutdown();
            webSocketClient.shutdown();
        } finally {
            super.stop(context);
        }
    }

    public static YamcsPlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        Bundle bundle = FrameworkUtil.getBundle(YamcsPlugin.class);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null));
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

    public void refreshClientInfo() {
        webSocketClient.updateClientinfo();
    }
}
