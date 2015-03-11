package org.csstudio.platform.libs.yamcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.ws.ParameterSubscribeEvent;
import org.csstudio.platform.libs.yamcs.ws.ParameterUnsubscribeEvent;
import org.csstudio.platform.libs.yamcs.ws.SubscribeAllCommandHistoryRequest;
import org.csstudio.platform.libs.yamcs.ws.WebSocketClient;
import org.csstudio.platform.libs.yamcs.ws.WebSocketClientCallbackListener;
import org.yamcs.protostuff.CommandHistoryEntry;
import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.NamedObjectList;
import org.yamcs.protostuff.ParameterData;
import org.yamcs.protostuff.ParameterValue;

/**
 * Combines state accross the many-to-one relation from yamcs:// datasources to
 * the WebSocketClient. Everything yamcs is still in WebSocketClient to keep
 * things a bit clean (and potentially reusable).
 * <p>
 * Now also handles live subscription of command history. Maybe should clean up a bit here to
 * extract out all the pvreader logic, because it's starting to do a bit too much.
 * <p>
 * All methods are asynchronous, with any responses or incoming data being sent
 * to the provided callback listener.
 */
public class YRegistrar implements WebSocketClientCallbackListener {
    
    private static final String USER_AGENT = "yamcs-studio/" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(YRegistrar.class.getName());
    private static YRegistrar INSTANCE;
    
    // Store pvreaders while connection is not established
    private Map<String, YPVReader> pvReadersByName = new LinkedHashMap<>();
    private List<CommandHistoryListener> cmdhistListeners = new ArrayList<CommandHistoryListener>();
    
    private boolean connectionInitialized = false;
    private WebSocketClient wsclient;
    
    private YRegistrar(YamcsConnectionProperties yprops) {
        wsclient = new WebSocketClient(yprops, this);
        wsclient.setUserAgent(USER_AGENT);
    }
    
    public static synchronized YRegistrar getInstance() {
        if (INSTANCE == null) {
            String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
            int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
            String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
            INSTANCE = new YRegistrar(new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance));
        }
        return INSTANCE;
    }
    
    public synchronized void connectPVReader(YPVReader pvReader) {
        if (!connectionInitialized) {
            wsclient.connect();
            connectionInitialized = true;
        }
        pvReadersByName.put(pvReader.getPVName(), pvReader);
        NamedObjectList idList = wrapAsNamedObjectList(pvReader.getPVName());
        wsclient.sendRequest(new ParameterSubscribeEvent(idList));
    }
    
    public synchronized void disconnectPVReader(YPVReader pvReader) {
        if (!connectionInitialized) { // TODO Possible?
            return;
        }
        pvReadersByName.remove(pvReader);
        NamedObjectList idList = wrapAsNamedObjectList(pvReader.getPVName());
        wsclient.sendRequest(new ParameterUnsubscribeEvent(idList));
    }
    
    public synchronized void addCommandHistoryListener(CommandHistoryListener listener) {
        if (!connectionInitialized) {
            wsclient.connect();
            connectionInitialized = true;
        }
        cmdhistListeners.add(listener);
        wsclient.sendRequest(new SubscribeAllCommandHistoryRequest()); // TODO don't need to do this for every listener
    }
    
    private static NamedObjectList wrapAsNamedObjectList(String pvName) {
        NamedObjectList idList = new NamedObjectList();
        NamedObjectId id = new NamedObjectId(pvName);
        if (!pvName.startsWith("/")) {
            id.setNamespace("MDB:OPS Name");
        }
        idList.setListList(Arrays.asList(id));
        return idList;
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
        log.info("Web socket established. Notifying listeners");
        // TODO we should trigger this instead on when the subscription was confirmed
        pvReadersByName.forEach((name, pvReader) -> pvReader.signalYamcsConnected());
        cmdhistListeners.forEach(l -> l.signalYamcsConnected());
    }

    @Override
    public void onDisconnect() { // When the web socket connection state changed
        log.info("Web socket disconnected. Notifying listeners");
        pvReadersByName.forEach((name, pvReader) -> pvReader.signalYamcsDisconnected());
        cmdhistListeners.forEach(l -> l.signalYamcsDisconnected());
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        pvReadersByName.get(id.getName()).reportException(new InvalidIdentification(id));
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YPVReader pvReader = pvReadersByName.get(pval.getId().getName());
            if (pvReader != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("request to update pvreader "+pvReader.getPVName()+" to val "+pval.getEngValue());
                }
                pvReader.processParameterValue(pval);
            } else {
                log.warning("No pvreader for incoming update of " + pval.getId().getName());
            }
        }
    }
    
    @Override
    public void onCommandHistoryData(CommandHistoryEntry cmdhistEntry) {
        cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
    }
}
