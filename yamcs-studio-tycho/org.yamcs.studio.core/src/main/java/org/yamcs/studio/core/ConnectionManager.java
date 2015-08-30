package org.yamcs.studio.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
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
    private YamcsConnectionProperties primaryProps;
    private YamcsConnectionProperties failoverProps;

    private Mode mode;
    private ConnectionStatus connectionStatus;

    private RestClient restClient;
    private WebSocketRegistrar webSocketClient;

    public static ConnectionManager getInstance() {
        return YamcsPlugin.getDefault().getConnectionManager();
    }

    public boolean isConnected() {
        return connectionStatus == ConnectionStatus.Connected;
    }

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.add(listener);
        // TODO this if should probably include 'whether we are currently connected'. ConnectionStatus ?
        if (restClient != null && webSocketClient != null)
            listener.onStudioConnect(getWebProperties(), getHornetqProperties(), restClient, webSocketClient);
    }

    public void removeStudioConnectionListener(StudioConnectionListener listener) {
        studioConnectionListeners.remove(listener);
    }

    /**
     * Updates the current connection info, without actually (re)connecting.
     */
    public void setConnectionInfo(YamcsConnectionProperties primaryConnection, YamcsConnectionProperties failoverConnection, YamcsCredentials creds) {
        this.primaryProps = primaryConnection;
        this.failoverProps = failoverConnection;
        this.creds = creds;
        mode = Mode.PRIMARY;
    }

    public void setYamcsCredentials(YamcsCredentials creds) {
        this.creds = creds;
    }

    public void connect() {
        log.info("Connecting to " + mode + " Yamcs server");
        setConnectionStatus(ConnectionStatus.Connecting);

        // (re)establish the connections to the yamcs server

        // common properties
        YamcsConnectionProperties webProps = getWebProperties();

        // Create a new REST Client. This doesn't make a connection, but it does
        // start a new thread pool. We could improve this in the future so that
        // we stick to just one instance while updating its conn. properties.
        restClient = new RestClient(webProps, creds);

        // WebSocket
        webSocketClient = new WebSocketRegistrar(webProps, creds);
        YamcsPlugin.getDefault().addMdbListener(webSocketClient);

        // We start other clients as well
        new Thread() {
            @Override
            public void run() {
                webSocketClient.connect(() -> setupConnections());
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
    void setupConnections() {
        // Need to improve this code. Currently doesn't support changing connections
        //boolean doSetup = (this.clientInfo == null);
        YamcsAuthorizations.getInstance().getAuthorizations();
        YamcsPlugin.getDefault().loadParameters();
        YamcsPlugin.getDefault().loadCommands();

        studioConnectionListeners.forEach(l -> {
            l.onStudioConnect(getWebProperties(), getHornetqProperties(), restClient, webSocketClient);
        });
        setConnectionStatus(ConnectionStatus.Connected);
    }

    public void connectionFailure(String errorMessage) {
        Display.getDefault().asyncExec(() -> {
            askSwitchNode(errorMessage);
        });
    }

    private void askSwitchNode(String errorMessage) {
        String message = "Connection error with " + mode + " Yamcs Server.";
        if (errorMessage != null && errorMessage != "") {
            message += "\nDetails:" + errorMessage;
        }
        Mode nextMode = (mode == Mode.PRIMARY) ? Mode.FAILOVER : Mode.PRIMARY;
        message += "\n\n" + "Would you like to switch connection to the " + nextMode + " Yamcs Server now?";
        MessageDialog dialog = new MessageDialog(null, "Connection Error", null, message,
                MessageDialog.QUESTION, new String[] { "Yes", "No" }, 0);
        if (dialog.open() == Dialog.OK) {
            Display.getDefault().asyncExec(() -> {
                disconnect();
                try {
                    switchNode();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Could not switch node", e);
                    notifyConnectionFailure(e.getMessage());
                }
            });
        } else {
            abortSwitchNode();
        }
    }

    public void switchNode() {
        if (mode == Mode.PRIMARY) {
            log.info("Switching to failover server");
            mode = Mode.FAILOVER;
        } else {
            log.info("Switching back to primary server");
            mode = Mode.PRIMARY;
        }

        disconnect();
        connect();
    }

    public void notifyConnectionFailure(String errorMessage) {
        synchronized (this) {
            if (connectionStatus != ConnectionStatus.Connected && connectionStatus != ConnectionStatus.Connecting)
                return;
            setConnectionStatus(ConnectionStatus.ConnectionFailure);
        }
        disconnect();
        connectionFailure(errorMessage);
    }

    public void notifyUnauthorized() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Connect", "Unauthorized");
    }

    private void setConnectionStatus(ConnectionStatus connectionStatus) {
        log.info("Current connection status: " + connectionStatus);
        this.connectionStatus = connectionStatus;
    }

    public boolean isPrivilegesEnabled() {
        // TODO we should probably control this from the server, rather than here. Just because
        // the creds are null, does not really mean anything. We could also send creds to an
        // unsecured yamcs server. It would just ignore it, and then our client state would
        // be wrong
        return creds != null;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public WebSocketRegistrar getWebSocketClient() {
        return webSocketClient;
    }

    public YamcsConnectionProperties getWebProperties() {
        if (mode == Mode.PRIMARY) {
            return primaryProps;
        } else {
            return failoverProps;
        }
    }

    private YamcsConnectData getHornetqProperties() {
        YamcsConnectionProperties yprops = getWebProperties();
        YamcsConnectData hornetqProps = new YamcsConnectData();
        hornetqProps.host = yprops.getHost();
        hornetqProps.port = 5445; // Hardcoded, we need to get rid of hornetq anyway
        hornetqProps.instance = yprops.getInstance();
        if (creds != null) {
            hornetqProps.username = creds.getUsername();
            hornetqProps.password = creds.getPasswordS();
            hornetqProps.ssl = true;
        }
        return hornetqProps;
    }

    private void abortSwitchNode() {
        if (connectionStatus == ConnectionStatus.ConnectionFailure)
            connectionStatus = ConnectionStatus.Disconnected;
    }

    public void shutdown() {
        if (restClient != null)
            restClient.shutdown();
        if (webSocketClient != null)
            webSocketClient.shutdown();
    }
}
