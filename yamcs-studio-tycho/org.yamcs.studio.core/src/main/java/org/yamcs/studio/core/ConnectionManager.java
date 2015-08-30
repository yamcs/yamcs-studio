package org.yamcs.studio.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.web.RestClient;

/**
 * Handles external connections and its related state. This logic was originally in YamcsPlugin, but
 * in an attempt to improve readability, they were bundled up here.
 *
 * TODO this should eventually move into UI, or the UI-parts should be filtered out of it.
 */
public class ConnectionManager {

    private static final Logger log = Logger.getLogger(ConnectionManager.class.getName());

    public enum Mode {
        PRIMARY,
        FAILOVER
    }

    public enum ConnectionStatus {
        Disconnected, // no clients (WebSocket, HornetQ) are connected to Yamcs server
        Connecting,
        Connected, // all clients are connected
        Disconnecting,
        ConnectionFailure,
    }

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();

    private YamcsCredentials creds;
    private YamcsConnectionProperties primaryConnection;
    private YamcsConnectionProperties failoverConnection;

    private Mode mode;
    private ConnectionStatus connectionStatus;

    private YProcessorControlClient processorControlClient;
    private RestClient restClient;
    private WebSocketRegistrar webSocketClient;

    public static ConnectionManager getInstance() {
        return YamcsPlugin.getDefault().getConnectionManager();
    }

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.add(listener);
        if (clientInfo != null && restClient != null && webSocketClient != null)
            listener.onStudioConnect(clientInfo, getWebProperties(), getHornetqProperties(currentCredentials), restClient, webSocketClient);
    }

    public void removeStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.remove(listener);
    }

    /**
     * Updates the current connection info, without actually (re)connecting.
     */
    public void setConnectionInfo(YamcsCredentials creds, YamcsConnectionProperties primaryConnection, YamcsConnectionProperties failoverConnection) {
        this.creds = creds;
        this.primaryConnection = primaryConnection;
        this.failoverConnection = failoverConnection;
        mode = Mode.PRIMARY;
    }

    public void createClients() {
        processorControlClient = new YProcessorControlClient();
    }

    public void connect(YamcsCredentials yamcsCredentials) {
        log.info("Connecting to Yamcs server, node " + YamcsPlugin.getDefault().getCurrentNode());
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
        YamcsPlugin.getDefault().addMdbListener(webSocketClient);

        // We start other clients as well
        webSocketClient.addClientInfoListener(clientInfo -> setupConnections(clientInfo, currentCredentials));
        new Thread() {
            @Override
            public void run() {
                webSocketClient.connect();
            }
        }.start();
    }

    public void disconnect() {
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
            YamcsPlugin.getDefault().removeMdbListener(webSocketClient);
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
            } catch (Exception e) {
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

    public void connectionFailure(int currentNode, int nextNode, String errorMessage) {
        Display.getDefault().asyncExec(() -> {
            askSwitchNode(currentNode, nextNode, errorMessage);
        });
    }

    private void askSwitchNode(int currentNode, int nextNode, String errorMessage) {
        String message = "Connection error with Yamcs Server node " + currentNode + ".";
        if (errorMessage != null && errorMessage != "") {
            message += "\nDetails:" + errorMessage;
        }
        message += "\n\n" + "Would you like to switch connection to node " + nextNode + " now?";
        MessageDialog dialog = new MessageDialog(null, "Connection Error", null, message,
                MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
        if (dialog.open() == Dialog.OK) {
            Display.getDefault().asyncExec(() -> {
                disconnect();
                try {
                    switchNode(nextNode);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Could not switch node", e);
                    notifyConnectionFailure(e.getMessage());
                }
            });
        } else {
            abortSwitchNode();
        }
    }

    public void notifyConnectionFailure(String errorMessage) {
        synchronized (this) {
            if (connectionStatus != ConnectionStatus.Connected && connectionStatus != ConnectionStatus.Connecting)
                return;
            this.connectionStatus = ConnectionStatus.ConnectionFailure;
        }
        disconnect();
        int currentNode = YamcsPlugin.getDefault().getCurrentNode();
        final int nextNode = currentNode + 1 > YamcsPlugin.getDefault().getNumberOfNodes() ? 1 : currentNode + 1;
        connectionFailure(currentNode, nextNode, errorMessage);
    }

    public void notifyUnauthorized() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Connect", "Unauthorized");
    }

    private void setConnectionStatus(ConnectionStatus connectionStatus) {
        log.info("Current connection status: " + connectionStatus);
        this.connectionStatus = connectionStatus;
    }

    public ConnectionStatus getConnectionSatus() {
        return connectionStatus;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public WebSocketRegistrar getWebSocketClient() {
        return webSocketClient;
    }

    public void switchNode(int nodeNumber) {
        if (nodeNumber < 1 || nodeNumber > getNumberOfNodes()) {
            log.severe(
                    "Request to switch to node " + nodeNumber + " but ony " + getNumberOfNodes() + " nodes are configured. Switching to node 1...");
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

    public void abortSwitchNode() {
        if (connectionStatus == ConnectionStatus.ConnectionFailure)
            connectionStatus = ConnectionStatus.Disconnected;
    }

    public void shutdown() {
        if (processorControlClient != null)
            processorControlClient.close();
        if (restClient != null)
            restClient.shutdown();
        if (webSocketClient != null)
            webSocketClient.shutdown();
    }
}
