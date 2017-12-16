package org.yamcs.studio.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.ConfigurationException;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.GetApiOverviewResponse;
import org.yamcs.studio.core.client.YamcsClient;
import org.yamcs.studio.core.security.YamcsAuthorizations;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * Handles external connections and its related state.
 *
 * @todo don't really like the use of synchronized here, we may be blocking the gui thread
 */
public class ConnectionManager {

    private static final Logger log = Logger.getLogger(ConnectionManager.class.getName());

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();

    private ConnectionInfo connectionInfo;
    private ConnectionMode mode;
    private ConnectionStatus connectionStatus;

    // Below are not-null after connect, null again after disconnect
    private YamcsClient yamcsClient;
    private String serverId;
    private String serverVersion;

    public static ConnectionManager getInstance() {
        YamcsPlugin plugin = YamcsPlugin.getDefault(); // null when workbench is closing
        return (plugin != null) ? plugin.getConnectionManager() : null;
    }

    public boolean isConnected() {
        return connectionStatus == ConnectionStatus.Connected;
    }

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.add(listener);
            if (isConnected()) {
                listener.onStudioConnect();
            }
        }
    }

    public void removeStudioConnectionListener(StudioConnectionListener listener) {
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.remove(listener);
        }
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        if (mode == null) {
            mode = ConnectionMode.PRIMARY;
        }
    }

    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public ConnectionMode getConnectionMode() {
        return mode;
    }

    public CompletableFuture<byte[]> requestAuthenticatedUser() {
        return getYamcsClient().get("/user", null);
    }

    /**
     * This returns the Yamcs instance that was used to set up the connection. You probably don't need this in any
     * views. Instead use {@link ManagementCatalogue.getCurrentYamcsInstance()} instead which reflects instance changes
     * within the same connection.
     */
    public String getInitialYamcsInstance() {
        if (connectionInfo != null) {
            return connectionInfo.getConnection(mode).getInstance();
        } else {
            return null;
        }
    }

    public String getUsername() {
        YamcsConnectionProperties yprops = connectionInfo.getConnection(mode);
        if (yprops.getAuthenticationToken() != null) {
            return "" + yprops.getAuthenticationToken().getPrincipal();
        } else {
            return null;
        }
    }

    public String getServerId() {
        return serverId;
    }

    public CompletableFuture<Void> connect() {
        return connect(mode);
    }

    /**
     * (re)establish the connection to yamcs
     */
    public CompletableFuture<Void> connect(ConnectionMode mode) {
        disconnectIfConnected();
        this.mode = mode;

        setConnectionStatus(ConnectionStatus.Connecting);

        YamcsConnectionProperties yprops = getConnectionProperties();
        yamcsClient = new YamcsClient(yprops);

        // Future covering both the initial rest call, and the ws conn attempt
        CompletableFuture<Void> cf = new CompletableFuture<>();

        // This 'defaultInstance' should really be moved to Server,
        // but for that we require more work on the websocket api,
        // which currently requires an instance to work with
        log.fine("Retrieving server information for " + yprops.getUrl());
        yamcsClient.get("", null).whenComplete((data, exc) -> {
            if (exc == null) {
                GetApiOverviewResponse response;
                try {
                    response = GetApiOverviewResponse.parseFrom(data);
                } catch (InvalidProtocolBufferException e) {
                    log.log(Level.SEVERE, "Failed to decode server response", e);
                    cf.completeExceptionally(e);
                    onWebSocketConnectionFailed(e);
                    return;
                }
                serverId = response.getServerId();
                serverVersion = response.getYamcsVersion();

                log.fine(String.format("Detected Yamcs Server v%s (id: '%s')", serverVersion, serverId));
                if (yprops.getInstance() == null || "".equals(yprops.getInstance())) {
                    if (response.hasDefaultYamcsInstance()) {
                        yprops.setInstance(response.getDefaultYamcsInstance());
                    } else {
                        Exception ex = new ConfigurationException(
                                "No 'instance' was specified, and no "
                                        + "default instance was configured on Yamcs Server.");

                        cf.completeExceptionally(ex);
                        // TODO get rid of this, use only future
                        onWebSocketConnectionFailed(ex);
                        return;
                    }
                }

                log.info("Connecting to " + yprops.getUrl());
                yamcsClient.connect().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            cf.complete(null);
                        } else {
                            cf.completeExceptionally(future.cause());
                        }
                    }
                });
            } else {
                cf.completeExceptionally(exc);
                onWebSocketConnectionFailed(exc);
            }
        });

        return cf;
    }

    public void disconnectIfConnected() {
        boolean doDisconnect = false;
        synchronized (this) {
            doDisconnect = (connectionStatus != null)
                    && (connectionStatus != ConnectionStatus.Disconnected);
        }
        if (doDisconnect)
            disconnect(false);
    }

    /**
     * Clean up state after a disconnect.
     */
    public void disconnect(boolean connectionLost) {
        log.fine("Start disconnect procedure (current state: " + connectionStatus + ")");
        synchronized (this) {
            if (connectionStatus == ConnectionStatus.Disconnected
                    || connectionStatus == ConnectionStatus.Disconnecting
                    || connectionStatus == ConnectionStatus.ConnectionFailure)
                return;

            setConnectionStatus(ConnectionStatus.Disconnecting);
        }

        log.fine("Shutting down Yamcs client");
        if (yamcsClient != null) {
            yamcsClient.shutdown();
        }
        yamcsClient = null;

        log.fine("Notify downstream components of Studio disconnect");
        synchronized (studioConnectionListeners) {
            for (StudioConnectionListener scl : studioConnectionListeners) {
                log.fine(String.format(" -> Inform %s", scl.getClass().getSimpleName()));
                try {
                    scl.onStudioDisconnect();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to disconnect listener " + scl + ".", e);
                }
            }
        }

        serverId = null;
        serverVersion = null;
        setConnectionStatus(ConnectionStatus.Disconnected);

        if (connectionLost) {
            synchronized (studioConnectionListeners) {
                studioConnectionListeners.forEach(l -> l.onStudioConnectionLost());
            }
        }
    }

    public void onWebSocketConnected() {
        log.fine("WebSocket connected");
        YamcsAuthorizations.getInstance().loadAuthorizations().thenRun(() -> {
            setConnectionStatus(ConnectionStatus.Connected);
            synchronized (studioConnectionListeners) {
                studioConnectionListeners.forEach(l -> l.onStudioConnect());
            }
        });
    }

    public void onWebSocketConnectionFailed(Throwable t) {
        log.severe("Could not connect: " + t.getMessage());
        synchronized (this) {
            setConnectionStatus(ConnectionStatus.ConnectionFailure);
        }
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.forEach(l -> l.onStudioConnectionFailure(t));
        }
    }

    public boolean isPrivilegesEnabled() {
        // TODO we should probably control this from the server, rather than here. Just because
        // the creds are null, does not really mean anything. We could also send creds to an
        // unsecured yamcs server. It would just ignore it, and then our client state would
        // be wrong
        YamcsConnectionProperties yprops = getConnectionProperties();
        return (yprops == null) ? false : yprops.getAuthenticationToken() != null;
    }

    public YamcsClient getYamcsClient() {
        return yamcsClient;
    }

    public YamcsConnectionProperties getConnectionProperties() {
        return (connectionInfo != null) ? connectionInfo.getConnection(mode) : null;
    }

    private void setConnectionStatus(ConnectionStatus connectionStatus) {
        log.fine(String.format("[%s] %s", mode, connectionStatus));
        this.connectionStatus = connectionStatus;
    }

    public void shutdown() {
        if (yamcsClient != null) {
            yamcsClient.shutdown();
        }
    }
}
