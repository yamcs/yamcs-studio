package org.yamcs.studio.client;

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
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

/**
 * Acts as the single gateway for yamcs-studio to yamcs WebSocketClient. Combines state accross the
 * many-to-one relation from yamcs datasources.
 * <p>
 * Now also handles live subscription of command history. Maybe should clean up a bit here to
 * extract out all the pvreader logic, because it's starting to do a bit too much.
 * <p>
 * All methods are asynchronous, with any responses or incoming data being sent to the provided
 * callback listener.
 */
public class WebSocketRegistrar extends MDBContextListener implements WebSocketClientCallbackListener {

    private static final String USER_AGENT = "yamcs-studio/" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(WebSocketRegistrar.class.getName());

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<String, YamcsPVReader> pvReadersByName = new LinkedHashMap<>();
    private Map<String, RestParameter> availableParametersByName = new LinkedHashMap<>();
    private List<CommandHistoryListener> cmdhistListeners = new ArrayList<>();

    private WebSocketClient wsclient;

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private final Thread requestSender;

    public WebSocketRegistrar(YamcsConnectionProperties yprops) {
        wsclient = new WebSocketClient(yprops, this);
        wsclient.setUserAgent(USER_AGENT);
        requestSender = new Thread(() -> {
            try {
                sendMergedRequests();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        });
    }

    public void connect() {
        wsclient.connect();
    }

    @Override
    public void onConnect() { // When the web socket was successfully established
        log.info("Web socket established. Notifying listeners");
        reportConnectionState();
        cmdhistListeners.forEach(l -> l.signalYamcsConnected());
        requestSender.start(); // Go over pending subscription requests
    }

    public void disconnect() {
        wsclient.disconnect();
    }

    private void sendMergedRequests() throws InterruptedException {
        while (true) {
            WebSocketRequest evt = pendingRequests.take();
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
            wsclient.sendRequest(evt);
        }
    }

    public synchronized void register(YamcsPVReader pvReader) {
        pvReadersByName.put(pvReader.getPVName(), pvReader);
        // Report current connection state
        RestParameter p = availableParametersByName.get(pvReader.getPVName());
        pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), p));
        // Register (pending) websocket request
        NamedObjectList idList = YamcsUtils.toNamedObjectList(pvReader.getPVName());
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "subscribe", idList));
    }

    public synchronized void unregister(YamcsPVReader pvReader) {
        pvReadersByName.remove(pvReader);
        NamedObjectList idList = YamcsUtils.toNamedObjectList(pvReader.getPVName());
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "unsubscribe", idList));
    }

    @Override
    public synchronized void onParametersChanged(List<RestParameter> parameters) {
        log.info("Refreshing all pv readers");
        for (RestParameter p : parameters) {
            availableParametersByName.put(p.getId().getName(), p);
        }
        pvReadersByName.forEach((name, pvReader) -> {
            RestParameter parameter = availableParametersByName.get(name);
            System.out.println("signaling " + name + ", " + parameter);
            pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), parameter));
        });
    }

    public synchronized void addCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.add(listener);
        pendingRequests.offer(new WebSocketRequest("cmdhistory", "subscribe")); // TODO don't need to do this for every listener
    }

    public void shutdown() {
        disconnect();
        wsclient.shutdown();
    }

    @Override
    public void onDisconnect() { // When the web socket connection state changed
        log.info("Web socket disconnected. Notifying listeners");
        reportConnectionState();
        cmdhistListeners.forEach(l -> l.signalYamcsDisconnected());
    }

    private void reportConnectionState() {
        pvReadersByName.forEach((name, pvReader) -> {
            RestParameter p = availableParametersByName.get(name);
            pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), p));
        });
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
                log.fine(String.format("Request to update pvreader %s to %s", pvReader.getPVName(), pval.getEngValue()));
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
