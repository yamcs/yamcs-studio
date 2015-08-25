package org.yamcs.studio.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestDumpRawMdbResponse;
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

    private YProcessorControlClient processorControlClient;
    private RestClient restClient;
    private WebSocketRegistrar webSocketClient;

    private ClientInfo clientInfo;
    //YamcsCredentials testcredentials = new YamcsCredentials("operator", "password");
    private YamcsCredentials currentCredentials;

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();
    private Set<MDBContextListener> mdbListeners = new HashSet<>();

    private XtceDb mdb;
    private List<RestParameter> parameters = Collections.emptyList();
    private Collection<MetaCommand> commands = Collections.emptyList();

    private List<ConnectionFailureListener> connectionFailureListeners = new LinkedList<ConnectionFailureListener>();

    // Reset for every application restart
    private static AtomicInteger cmdClientId = new AtomicInteger(1);

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        TimeEncoding.setUp();
        processorControlClient = new YProcessorControlClient();
        log.info("Yamcs Studio v." + getBundle().getVersion().toString());
    }

    public enum ConnectionStatus
    {
        Disconnected, // no clients (REST, WebSocket, HornetQ)are connected to Yamcs server
        Connecting,
        Connected, // all clients are connected
        Disconnecting,
        ConnectionFailure,
    }

    private ConnectionStatus connectionStatus;

    private void setConnectionStatus(ConnectionStatus connectionStatus)
    {
        log.info("Current connection status: " + connectionStatus);
        this.connectionStatus = connectionStatus;
    }

    public ConnectionStatus getConnectionSatus()
    {
        return connectionStatus;
    }

    public void connect(YamcsCredentials yamcsCredentials)
    {
        log.info("Connecting to Yamcs server, node " + getCurrentNode());
        setConnectionStatus(ConnectionStatus.Connecting);

        // store the current credential
        currentCredentials = yamcsCredentials;

        // (re)establish the connections to the yamcs server

        // common properties
        YamcsConnectionProperties webProps = getWebProperties();

        // REST
        restClient = new RestClient(webProps, yamcsCredentials);

        // WebSocket
        webSocketClient = new WebSocketRegistrar(webProps, yamcsCredentials);
        addMdbListener(webSocketClient);

        // We start other clients as well
        webSocketClient.addClientInfoListener(clientInfo -> setupConnections(clientInfo, currentCredentials));
        webSocketClient.connect();
    }

    public void disconnect()
    {
        synchronized (this) {
            if (connectionStatus == ConnectionStatus.Disconnected
                    || connectionStatus == ConnectionStatus.Disconnecting)
                return;

            if (connectionStatus != ConnectionStatus.ConnectionFailure)
                setConnectionStatus(ConnectionStatus.Disconnecting);
        }
        log.fine("Disconnecting...");

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

        // Notify all studio connection listeners of disconnect
        for (StudioConnectionListener scl : studioConnectionListeners) {
            try {
                scl.onStudioDisconnect();
            } catch (Exception e)
            {
                log.log(Level.SEVERE, "Unable to disconnect listener " + scl + ".", e);
            }
        }

        if (connectionStatus != ConnectionStatus.ConnectionFailure)
            setConnectionStatus(ConnectionStatus.Disconnected);
    }

    // Likely not on the swt thread
    private void setupConnections(ClientInfo clientInfo, YamcsCredentials credentials) {
        log.fine(String.format("Got back clientInfo %s", clientInfo));
        // Need to improve this code. Currently doesn't support changing connections
        //boolean doSetup = (this.clientInfo == null);
        this.clientInfo = clientInfo;
        YamcsAuthorizations.getInstance().getAuthorizations();
        loadParameters();
        loadCommands();

        studioConnectionListeners.forEach(l -> {
            l.onStudioConnect(clientInfo, getWebProperties(), getHornetqProperties(credentials), restClient, webSocketClient);
        });
        setConnectionStatus(ConnectionStatus.Connected);
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
        hornetqProps.port = getHornetQPort();
        hornetqProps.instance = getInstance();
        if (credentials != null) {
            hornetqProps.username = credentials.getUsername();
            hornetqProps.password = credentials.getPasswordS();
            hornetqProps.ssl = true;
        }
        return hornetqProps;
    }

    public int getNumberOfNodes()
    {
        return getPreferenceStore().getInt("number_of_nodes");
    }

    public int getCurrentNode()
    {
        return getPreferenceStore().getInt("current_node");
    }

    private void setCurrentNode(int currentNode)
    {
        getPreferenceStore().setValue("current_node", currentNode);
    }

    public void switchNode(int nodeNumber)
    {
        if (nodeNumber < 1 || nodeNumber > getNumberOfNodes())
        {
            log.severe("Request to switch to node " + nodeNumber + " but ony " + getNumberOfNodes() + " nodes are configured. Switching to node 1...");
            nodeNumber = 1;
        }
        // if (currentNode != nodeNumber)
        {
            log.info("switching from node " + getCurrentNode() + " to node " + nodeNumber);
            setCurrentNode(nodeNumber);
            disconnect();
            connect(currentCredentials);
        }
    }

    public void abortSwitchNode()
    {
        if (connectionStatus == ConnectionStatus.ConnectionFailure)
            connectionStatus = ConnectionStatus.Disconnected;
    }

    public String getInstance() {
        String node = "node" + getCurrentNode() + ".";
        return getPreferenceStore().getString(node + "yamcs_instance");
    }

    public String getHost() {
        String node = "node" + getCurrentNode() + ".";
        return getPreferenceStore().getString(node + "yamcs_host");
    }

    public int getHornetQPort() {
        String node = "node" + getCurrentNode() + ".";
        return getPreferenceStore().getInt(node + "yamcs_hornetqport");
    }

    public int getWebPort() {
        String node = "node" + getCurrentNode() + ".";
        return getPreferenceStore().getInt(node + "yamcs_port");
    }

    public boolean getPrivilegesEnabled() {
        String node = "node" + getCurrentNode() + ".";
        return getPreferenceStore().getBoolean(node + "yamcs_privileges");
    }

    public String getOrigin() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    private void loadParameters() {
        log.fine("Fetching available parameters");
        RestListAvailableParametersRequest.Builder req = RestListAvailableParametersRequest.newBuilder();
        restClient.listAvailableParameters(req.build(), new ResponseHandler() {
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

    private void loadCommands() {
        log.fine("Fetching available commands");
        restClient.dumpRawMdb(new ResponseHandler() {
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
                                        + "This usually happens when Yamcs Studio is not, or no longer "
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

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.add(listener);
        if (clientInfo != null && restClient != null && webSocketClient != null)
            listener.onStudioConnect(clientInfo, getWebProperties(), getHornetqProperties(currentCredentials), restClient, webSocketClient);
    }

    public void removeStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.remove(listener);
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

    /**
     * Returns the rest client associated with the current connection. Useful for dialogs and such,
     * that just needs this sporadically.
     */
    public RestClient getRestClient() {
        return restClient;
    }

    public XtceDb getMdb() {
        return mdb;
    }

    /**
     * Would prefer to get updates to this from the web socket client, instead of needing this
     * method
     */
    public void refreshClientInfo() {
        webSocketClient.updateClientinfo();
    }

    public void addConnectionFailureListener(ConnectionFailureListener cfl)
    {
        connectionFailureListeners.add(cfl);
    }

    public void notifyConnectionFailure(String errorMessage) {
        log.info("");
        synchronized (this) {
            if (connectionStatus != ConnectionStatus.Connected && connectionStatus != ConnectionStatus.Connecting)
                return;
            this.connectionStatus = ConnectionStatus.ConnectionFailure;
        }
        this.disconnect();
        int currentNode = getCurrentNode();
        final int nextNode = currentNode + 1 > getNumberOfNodes() ? 1 : currentNode + 1;
        connectionFailureListeners.forEach(c -> c.connectionFailure(currentNode, nextNode, errorMessage));
    }

    public void notifyUnauthorized() {
        connectionFailureListeners.forEach(c -> c.unauthorized());
    }
}
