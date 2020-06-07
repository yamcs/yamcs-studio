package org.yamcs.studio.core.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import org.yamcs.client.ConnectionListener;
import org.yamcs.client.YamcsClient;
import org.yamcs.studio.connect.YamcsConfiguration;

public class YamcsStudioClient {

    private static final Logger log = Logger.getLogger(YamcsStudioClient.class.getName());

    private YamcsConfiguration yprops;

    private YamcsClient yamcsClient;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();

    public void addConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    /*@Override
    public void connecting(String url) {
        for (ConnectionListener cl : connectionListeners) {
            cl.connecting(null);
        }
    }
    
    @Override
    public void connected(String url) {
        log.info("Connected to " + yprops);
        for (ConnectionListener listener : connectionListeners) {
            listener.connected(null);
        }
    }
    
    @Override
    public void disconnected() {
        if (isConnected()) {
            log.warning("Connection to " + yprops + " lost");
        }
        for (ConnectionListener listener : connectionListeners) {
            listener.disconnected();
        }
    }
    
    public void disconnect() {
        log.info("Disconnecting from " + yprops);
        if (yamcsClient != null) {
            yamcsClient.close();
        }
    }
    
    public boolean isConnected() {
        return yamcsClient != null && yamcsClient.isConnected();
    }
    
    public boolean isConnecting() {
        return false;
        // return yamcsClient != null && yamcsClient.isConnecting();
    }*/

    public YamcsConfiguration getYamcsConfiguration() {
        return yprops;
    }

    public YamcsClient getYamcsClient() {
        return yamcsClient;
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        if (yamcsClient != null) {
            yamcsClient.close();
        }
        executor.shutdown();
    }
}
