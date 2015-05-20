package org.yamcs.studio.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
    //YamcsCredentials testcredentials = new YamcsCredentials("operator", "password");
    YamcsCredentials currentCredentials = null;

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();
    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private XtceDb mdb;
    private List<RestParameter> parameters = Collections.emptyList();
    private Collection<MetaCommand> commands = Collections.emptyList();

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();
        processorControlClient = new YProcessorControlClient();
        // Only connect once bundle has been fully started
        bundleListener = event -> {
            if (event.getBundle() == getBundle() && event.getType() == BundleEvent.STARTED) {
                // Bundle may have been shut down between the time this event was queued and now
                if (getBundle().getState() == Bundle.ACTIVE) {
                    //  setWebConnections(currentCredentials);
                }
            }
        };
        context.addBundleListener(bundleListener);
    }

    public enum ConnectionStatus
    {
        Disconnected, // no clients (REST, WebSocket, HornetQ)are connected to Yamcs server
        //PartiallyConnected,
        Connecting,
        Connected, // all clients are connected
        Disconnecting
    }

    private ConnectionStatus connectionStatus;

    public void setConnectionStatus(ConnectionStatus connectionStatus)
    {
        log.info("Current connection status: " + connectionStatus);
        this.connectionStatus = connectionStatus;
    }

    public ConnectionStatus getConnectionSatus()
    {
        return connectionStatus;
    }

    private void setWebConnections(YamcsCredentials yamcsCredentials)
    {
        setConnectionStatus(ConnectionStatus.Connecting);

        // common properties
        YamcsConnectionProperties webProps = getWebProperties();

        // REST
        restClient = new RestClient(webProps, yamcsCredentials);

        // WebSocket
        webSocketClient = new WebSocketRegistrar(webProps, yamcsCredentials);
        addMdbListener(webSocketClient);

        // we start other clients as well
        webSocketClient.addClientInfoListener(clientInfo -> setupConnections(clientInfo, currentCredentials));
        webSocketClient.connect();

    }

    public void disconnect()
    {
        log.info("Disconnecting...");
        setConnectionStatus(ConnectionStatus.Disconnecting);

        // WebSocket
        if (webSocketClient != null) {
            removeMdbListener(webSocketClient);
            webSocketClient.shutdown();
        }
        webSocketClient = null;

        // REST
        if (restClient != null)
            restClient.shutdown();
        restClient = null;

        // Disconnect all studio connection listeners
        for (StudioConnectionListener scl : studioConnectionListeners) {
            try {
                scl.disconnect();
            } catch (Exception e)
            {
                log.log(Level.SEVERE, "Unable to disconnect listener " + scl + ".", e);
            }
        }

        setConnectionStatus(ConnectionStatus.Disconnected);

    }

    // Likely not on the swt thread
    private void setupConnections(ClientInfo clientInfo, YamcsCredentials credentials) {
        log.fine(String.format("Got back clientInfo %s", clientInfo));
        // Need to improve this code. Currently doesn't support changing connections
        //boolean doSetup = (this.clientInfo == null);
        this.clientInfo = clientInfo;
        if (true) {
            loadParameters();
            loadCommands();

            studioConnectionListeners.forEach(l -> {
                l.processConnectionInfo(clientInfo, getWebProperties(), getHornetqProperties(credentials), restClient, webSocketClient);
            });
        }
        setConnectionStatus(ConnectionStatus.Connected);
    }

    private RestClient getRestClient() {
        return restClient;
    }

    private WebSocketRegistrar getWebSocketClient() {
        return webSocketClient;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public static int getNextCommandClientId() {
        return cmdClientId.incrementAndGet();
    }

    public ProcessorInfo getProcessorInfo(String processorName) {
        return processorControlClient.getProcessorInfo(processorName);
    }

    public YamcsConnectionProperties getWebProperties() {
        return new YamcsConnectionProperties(getHost(), getWebPort(), getInstance());
    }

    private YamcsConnectData getHornetqProperties(YamcsCredentials credentials) {
        YamcsConnectData hornetqProps = new YamcsConnectData();
        hornetqProps.host = getHost();
        hornetqProps.port = 5445;
        hornetqProps.instance = getInstance();
        if (credentials != null) {
            hornetqProps.username = credentials.getUsername();
            hornetqProps.password = credentials.getPasswordS();
            hornetqProps.ssl = true;
        }
        return hornetqProps;
    }

    public String getInstance() {
        return getPreferenceStore().getString("yamcs_instance");
    }

    public String getHost() {
        return getPreferenceStore().getString("yamcs_host");
    }

    public int getWebPort() {
        return getPreferenceStore().getInt("yamcs_port");
    }

    public boolean getPrivilegesEnabled() {
        return getPreferenceStore().getBoolean("yamcs_privileges");
    }

    public String getMdbNamespace() {
        return getPreferenceStore().getString("mdb_namespace");
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
        if (clientInfo != null && restClient != null && webSocketClient != null)
            listener.processConnectionInfo(clientInfo, getWebProperties(), getHornetqProperties(currentCredentials), restClient, webSocketClient);
    }

    public void addProcessorListener(ProcessorListener listener) {
        processorControlClient.addProcessorListener(listener);
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
            if (bundleListener != null)
                context.removeBundleListener(bundleListener);
            plugin = null;
            if (processorControlClient != null)
                processorControlClient.close();
            if (restClient != null)
                restClient.shutdown();
            if (webSocketClient != null)
                webSocketClient.shutdown();
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

    public void refreshClientInfo() {
        webSocketClient.updateClientinfo();
    }

    public void setAuthenticatedPrincipal(YamcsCredentials yamcsCredentials) throws Exception {
        // store the current credential
        currentCredentials = yamcsCredentials;

        // (re)establish the connections to the yamcs server
        //  disconnect();
        setWebConnections(currentCredentials);
    }

}
