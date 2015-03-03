package org.csstudio.platform.libs.yamcs;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.ws.WebSocketClient;
import org.csstudio.platform.libs.yamcs.ws.WebSocketClientCallbackListener;
import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.NamedObjectList;
import org.yamcs.protostuff.ParameterData;
import org.yamcs.protostuff.ParameterValue;

/**
 * Combines state accross the many-to-one relation from yamcs:// datasources to
 * the WebSocketClient. Everything yamcs is still in WebSocketClient to keep
 * things a bit clean (and potentially reusable).
 * <p>
 * All methods are asynchronous, with any responses or incoming data being sent
 * to the provided callback listener.
 */
public class YRegistrar implements WebSocketClientCallbackListener {
    
    private static final String USER_AGENT = "yamcs-studio"; //Activator.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(YRegistrar.class.getName());
    private static YRegistrar INSTANCE;
    
    // Store listeners/handlers while connection is not established
    private Map<String, YPVListener> listenersByName = new LinkedHashMap<>();
    
    private boolean connectionInitialized = false;
    private WebSocketClient wsclient;
    
    private YRegistrar(YamcsConnectionProperties yprops) {
        wsclient = new WebSocketClient(yprops, this);
        wsclient.setUserAgent(USER_AGENT);
    }
    
    // TODO connection props shouldn't come from outside since we consider this a singleton
    public static synchronized YRegistrar getInstance(YamcsConnectionProperties yprops) {
        if (INSTANCE == null) {
            INSTANCE = new YRegistrar(yprops);
        }
        return INSTANCE;
    }
    
    public synchronized void connectChannelHandler(YPVListener channelHandler) {
        if (!connectionInitialized) {
            wsclient.connect();
            connectionInitialized = true;
        }
        listenersByName.put(channelHandler.getPVName(), channelHandler);
        NamedObjectList idList = new NamedObjectList();
        NamedObjectId id = new NamedObjectId(channelHandler.getPVName());
        if (!channelHandler.getPVName().startsWith("/")) {
            id.setNamespace("MDB:OPS Name");
        }
        idList.setListList(Arrays.asList(id));
        wsclient.subscribe(idList);
    }
    
    public synchronized void disconnectChannelHandler(YPVListener listener) {
        // TODO
    }
    
    public void disconnect() {
        connectionInitialized = false;
        wsclient.disconnect();
    }
    
    public void shutdown() {
        disconnect();
        wsclient.shutdown();
    }

    @Override
    public void onConnect() { // When the web socket was successfully established
        log.info("Web socket established. Notifying channel-handlers");
        // TODO we should trigger this instead on when the subscription was confirmed
        listenersByName.forEach((name, handler) -> {
            handler.signalYamcsConnected();
        });
    }

    @Override
    public void onDisconnect() { // When the web socket connection state changed
        log.info("Web socket disconnected. Notifying channel-handlers");
        listenersByName.forEach((name, handler) -> handler.signalYamcsDisconnected());
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        listenersByName.get(id.getName()).reportException(new InvalidIdentification(id));
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YPVListener handler = listenersByName.get(pval.getId().getName());
            if (handler != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("request to update channel "+handler.getPVName()+" to val "+pval.getEngValue());
                }
                handler.processParameterValue(pval);
            } else {
                log.warning("No handler for incoming update of " + pval.getId().getName());
            }
        }
    }
}
