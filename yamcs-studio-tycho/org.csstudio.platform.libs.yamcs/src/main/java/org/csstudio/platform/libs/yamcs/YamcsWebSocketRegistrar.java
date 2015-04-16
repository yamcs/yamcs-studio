package org.csstudio.platform.libs.yamcs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketClient;
import org.yamcs.api.ws.WebSocketClientCallbackListener;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

/**
 * Combines state accross the many-to-one relation from yamcs:// datasources to the WebSocketClient.
 * <p>
 * Now also handles live subscription of command history. Maybe should clean up a bit here to
 * extract out all the pvreader logic, because it's starting to do a bit too much.
 * <p>
 * All methods are asynchronous, with any responses or incoming data being sent to the provided
 * callback listener.
 */
public class YamcsWebSocketRegistrar implements WebSocketClientCallbackListener {

    private static final String USER_AGENT = "yamcs-studio/" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(YamcsWebSocketRegistrar.class.getName());
    private static YamcsWebSocketRegistrar INSTANCE;

    // Store pvreaders while connection is not established
    private Map<String, YamcsPVReader> pvReadersByName = new LinkedHashMap<>();
    private List<CommandHistoryListener> cmdhistListeners = new ArrayList<>();

    private boolean connectionInitialized = false;
    private WebSocketClient wsclient;

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private YamcsWebSocketRegistrar(YamcsConnectionProperties yprops) {
        wsclient = new WebSocketClient(yprops, this);
        wsclient.setUserAgent(USER_AGENT);

        new Thread(() -> {
            try {
                sendMergedRequests();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        }).start();
    }

    private void sendMergedRequests() throws InterruptedException {
        WebSocketRequest evt;
        while ((evt = pendingRequests.take()) != null) {
            // We now have at least one event to handle
            Thread.sleep(500); // Wait for more events, before going into synchronized block
            synchronized (pendingRequests) {
                while (pendingRequests.peek() != null
                        && evt instanceof MergeableWebSocketRequest
                        && pendingRequests.peek() instanceof MergeableWebSocketRequest) {
                    MergeableWebSocketRequest otherEvt = (MergeableWebSocketRequest) pendingRequests.poll();
                    evt = ((MergeableWebSocketRequest) evt).mergeWith(otherEvt); // This is to counter bursts.
                }
            }

            // Good, send the merged result
            doSendRequest(evt);
        }
    }

    public static synchronized YamcsWebSocketRegistrar getInstance() {
        if (INSTANCE == null) {
            String yamcsHost = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_host");
            int yamcsPort = YamcsPlugin.getDefault().getPreferenceStore().getInt("yamcs_port");
            String yamcsInstance = YamcsPlugin.getDefault().getPreferenceStore().getString("yamcs_instance");
            INSTANCE = new YamcsWebSocketRegistrar(new YamcsConnectionProperties(yamcsHost, yamcsPort, yamcsInstance));
        }
        return INSTANCE;
    }

    public synchronized void connectPVReader(YamcsPVReader pvReader) {
        pvReadersByName.put(pvReader.getPVName(), pvReader);
        NamedObjectList idList = wrapAsNamedObjectList(pvReader.getPVName());
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "subscribe", idList));
    }

    public synchronized void disconnectPVReader(YamcsPVReader pvReader) {
        pvReadersByName.remove(pvReader);
        NamedObjectList idList = wrapAsNamedObjectList(pvReader.getPVName());
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "unsubscribe", idList));
    }

    public synchronized void addCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.add(listener);
        pendingRequests.offer(new WebSocketRequest("cmdhistory", "subscribe")); // TODO don't need to do this for every listener
    }

    private static NamedObjectList wrapAsNamedObjectList(String pvName) {
        return NamedObjectList.newBuilder().addList(NamedObjectId.newBuilder()
                .setNamespace(YamcsPlugin.getDefault().getMdbNamespace())
                .setName(pvName)).build();
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

    private void doSendRequest(WebSocketRequest request) {
        if (!connectionInitialized) {
            wsclient.connect();
            connectionInitialized = true;
        }
        wsclient.sendRequest(request);
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
            YamcsPVReader pvReader = pvReadersByName.get(pval.getId().getName());
            if (pvReader != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("request to update pvreader " + pvReader.getPVName() + " to val " + pval.getEngValue());
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
