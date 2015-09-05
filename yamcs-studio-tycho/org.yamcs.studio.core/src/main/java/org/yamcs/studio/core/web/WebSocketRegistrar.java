package org.yamcs.studio.core.web;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketClient;
import org.yamcs.api.ws.WebSocketClientCallbackListener;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Alarms.Alarm;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;
import org.yamcs.protobuf.Yamcs.StreamData;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.AlarmListener;
import org.yamcs.studio.core.CommandHistoryListener;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.EventListener;
import org.yamcs.studio.core.InvalidIdentification;
import org.yamcs.studio.core.LosTracker;
import org.yamcs.studio.core.MDBContextListener;
import org.yamcs.studio.core.MergeableWebSocketRequest;
import org.yamcs.studio.core.PVConnectionInfo;
import org.yamcs.studio.core.YamcsCredentials;
import org.yamcs.studio.core.YamcsPVReader;
import org.yamcs.studio.core.YamcsPlugin;

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

    private static final String USER_AGENT = "Yamcs Studio v" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(WebSocketRegistrar.class.getName());

    // Store pvreaders while connection is not established
    // Assumes that all names for all yamcs schemes are sharing a same namespace (which they should be)
    private Map<NamedObjectId, YamcsPVReader> pvReadersById = new LinkedHashMap<>();
    private Map<NamedObjectId, RestParameter> availableParametersById = new LinkedHashMap<>();
    private Set<AlarmListener> alarmListeners = new HashSet<>();
    private Set<EventListener> eventListeners = new HashSet<>();
    private LosTracker losTracker = new LosTracker();

    private Set<CommandHistoryListener> cmdhistListeners = new HashSet<>();

    private WebSocketClient wsclient;
    private Runnable onConnectCallback; // FIXME ugly

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private final Thread requestSender;

    public WebSocketRegistrar(YamcsConnectionProperties yprops, YamcsCredentials credentials) {
        wsclient = new WebSocketClient(yprops, this, credentials != null ? credentials.getUsername() : null,
                credentials != null ? credentials.getPasswordS() : null);
        wsclient.setUserAgent(USER_AGENT);
        requestSender = new Thread(() -> {
            try {
                sendMergedRequests();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        });
    }

    public void connect(Runnable onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
        wsclient.connect(); // FIXME this currently blocks. It should have a callback api instead
        // FIXME Always have these subscriptions running
        pendingRequests.offer(new WebSocketRequest("cmdhistory", "subscribe"));
        pendingRequests.offer(new WebSocketRequest("alarms", "subscribe"));
        pendingRequests.offer(new WebSocketRequest("events", "subscribe"));
    }

    public void subscribeToManagementInfo() {
        // Always have this subscription running FIXME
        pendingRequests.offer(new WebSocketRequest("management", "subscribe"));
    }

    public void subscribeToTimeInfo() {
        // Always have this subscription running FIXME
        pendingRequests.offer(new WebSocketRequest("time", "subscribe"));
    }

    @Override
    public void onConnect() { // When the web socket was successfully established
        log.fine("WebSocket established. Notifying listeners");
        onConnectCallback.run(); // FIXME ugly hack
        reportConnectionState();
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
                        && pendingRequests.peek() instanceof MergeableWebSocketRequest
                        && evt.getResource().equals(pendingRequests.peek().getResource())
                        && evt.getOperation().equals(pendingRequests.peek().getOperation())) {
                    MergeableWebSocketRequest otherEvt = (MergeableWebSocketRequest) pendingRequests.poll();
                    evt = ((MergeableWebSocketRequest) evt).mergeWith(otherEvt); // This is to counter bursts.
                }
            }

            // Good, send the merged result
            log.fine(String.format("Sending request %s", evt));
            wsclient.sendRequest(evt);
        }
    }

    public synchronized void register(YamcsPVReader pvReader) {
        pvReadersById.put(pvReader.getId(), pvReader);
        // Report current connection state
        RestParameter p = availableParametersById.get(pvReader.getId());
        pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), p));
        // Register (pending) websocket request
        NamedObjectList idList = pvReader.toNamedObjectList();
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "subscribe", idList));
    }

    public synchronized void unregister(YamcsPVReader pvReader) {
        pvReadersById.remove(pvReader);
        NamedObjectList idList = pvReader.toNamedObjectList();
        pendingRequests.offer(new MergeableWebSocketRequest("parameter", "unsubscribe", idList));
    }

    @Override
    public synchronized void onParametersChanged(List<RestParameter> parameters) {
        log.fine("Refreshing all pv readers");
        for (RestParameter p : parameters)
            availableParametersById.put(p.getId(), p);

        pvReadersById.forEach((id, pvReader) -> {
            RestParameter parameter = availableParametersById.get(id);
            log.finer(String.format("Signaling %s --> %s", id, parameter));
            pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), parameter));
        });
    }

    public synchronized void addAlarmListener(AlarmListener listener) {
        alarmListeners.add(listener);
    }

    public synchronized void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    // TODO we should probably move this somewhere else. This class is too bloated
    public synchronized void addCommandHistoryListener(CommandHistoryListener listener) {
        cmdhistListeners.add(listener);
    }

    public void shutdown() {
        disconnect();
        wsclient.shutdown();
    }

    @Override
    public void onException(Throwable t) {
        ConnectionManager.getInstance().notifyException(t);
    }

    @Override
    public void onDisconnect() { // When the web socket connection state changed
        log.info("WebSocket disconnected. Notifying listeners");
        reportConnectionState();
        YamcsPlugin plugin = YamcsPlugin.getDefault();
        if (plugin != null) // This can be null when the workbench is closing
            plugin.getConnectionManager().notifyConnectionFailure(null);
    }

    private void reportConnectionState() {
        pvReadersById.forEach((id, pvReader) -> {
            RestParameter p = availableParametersById.get(id);
            pvReader.processConnectionInfo(new PVConnectionInfo(wsclient.isConnected(), p));
        });
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        pvReadersById.get(id).reportException(new InvalidIdentification(id));
    }

    @Override
    public void onTimeInfo(TimeInfo timeInfo) {
        YamcsPlugin.getDefault().getTimeCatalogue().processTimeInfo(timeInfo);
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        for (ParameterValue pval : pdata.getParameterList()) {
            YamcsPVReader pvReader = pvReadersById.get(pval.getId());
            if (pvReader != null) {
                log.fine(String.format("Request to update pvreader %s to %s", pvReader.getId().getName(), pval.getEngValue()));
                losTracker.updatePv(pvReader, pval);
                pvReader.processParameterValue(pval);
            } else {
                log.warning("No pvreader for incoming update of " + pval.getId().getName());
            }
        }
    }

    @Override
    public void onClientInfoData(ClientInfo clientInfo) {
        YamcsPlugin.getDefault().getManagementCatalogue().processClientInfo(clientInfo);
    }

    @Override
    public void onProcessorInfoData(ProcessorInfo processorInfo) {
        YamcsPlugin.getDefault().getManagementCatalogue().processProcessorInfo(processorInfo);
    }

    @Override
    public void onCommandHistoryData(CommandHistoryEntry cmdhistEntry) {
        synchronized (this) {
            cmdhistListeners.forEach(l -> l.processCommandHistoryEntry(cmdhistEntry));
        }
    }

    @Override
    public void onStatisticsData(Statistics statistics) {
        YamcsPlugin.getDefault().getManagementCatalogue().processStatistics(statistics);
    }

    @Override
    public void onStreamData(StreamData streamData) {
    }

    @Override
    public void onEvent(Event event) {
        synchronized (this) {
            eventListeners.forEach(l -> l.processEvent(event));
        }
    }

    @Override
    public void onAlarm(Alarm alarm) {
        synchronized (this) {
            alarmListeners.forEach(l -> l.processAlarm(alarm));
        }
    }
}
