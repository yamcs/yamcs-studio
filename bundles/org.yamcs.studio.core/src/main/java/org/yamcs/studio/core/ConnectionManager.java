package org.yamcs.studio.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;
import org.yamcs.YamcsException;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.ws.ConnectionListener;
import org.yamcs.studio.core.client.YamcsClient;
import org.yamcs.studio.core.security.YamcsAuthorizations;

/**
 * Handles external connections and its related state.
 *
 * @todo don't really like the use of synchronized here, we may be blocking the gui thread
 */
public class ConnectionManager implements ConnectionListener {

    private static final Logger log = Logger.getLogger(ConnectionManager.class.getName());

    private Set<StudioConnectionListener> studioConnectionListeners = new HashSet<>();

    private YamcsClient yamcsClient;

    public ConnectionManager() {
        String productName = Platform.getProduct().getName();
        Version productVersion = Platform.getProduct().getDefiningBundle().getVersion();
        yamcsClient = new YamcsClient(productName + " v" + productVersion);
        yamcsClient.addConnectionListener(this);
    }

    public static ConnectionManager getInstance() {
        YamcsPlugin plugin = YamcsPlugin.getDefault(); // null when workbench is closing
        return (plugin != null) ? plugin.getConnectionManager() : null;
    }

    public void addStudioConnectionListener(StudioConnectionListener listener) {
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.add(listener);
            if (yamcsClient.isConnected()) {
                listener.onStudioConnect();
            }
        }
    }

    public void removeStudioConnectionListener(StudioConnectionListener listener) {
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.remove(listener);
        }
    }

    public boolean isPrivilegesEnabled() {
        // TODO we should probably control this from the server, rather than here. Just because
        // the creds are null, does not really mean anything. We could also send creds to an
        // unsecured yamcs server. It would just ignore it, and then our client state would
        // be wrong
        YamcsConnectionProperties yprops = yamcsClient.getYamcsConnectionProperties();
        return (yprops == null) ? false : yprops.getAuthenticationToken() != null;
    }

    public YamcsClient getYamcsClient() {
        return yamcsClient;
    }

    public void shutdown() {
        if (yamcsClient != null) {
            yamcsClient.shutdown();
        }
    }

    @Override
    public void connecting(String url) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connected(String url) {
        log.fine("WebSocket connected");
        YamcsAuthorizations.getInstance().loadAuthorizations().thenRun(() -> {
            synchronized (studioConnectionListeners) {
                studioConnectionListeners.forEach(l -> l.onStudioConnect());
            }
        });
    }

    @Override
    public void connectionFailed(String url, YamcsException exception) {
        log.severe("Could not connect: " + exception.getMessage());
        synchronized (studioConnectionListeners) {
            studioConnectionListeners.forEach(l -> l.onStudioConnectionFailure(exception));
        }
    }

    @Override
    public void disconnected() {
        log.fine("Notify downstream components of Studio disconnect");
        synchronized (studioConnectionListeners) {
            for (StudioConnectionListener l : studioConnectionListeners) {
                log.fine(String.format(" -> Inform %s", l.getClass().getSimpleName()));
                try {
                    l.onStudioDisconnect();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to disconnect listener " + l, e);
                }
            }
        }
    }

    @Override
    public void log(String message) {
        System.out.println("log message: " + message);
    }
}
