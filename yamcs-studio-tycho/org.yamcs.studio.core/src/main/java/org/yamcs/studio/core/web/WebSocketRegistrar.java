package org.yamcs.studio.core.web;

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
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.StreamData;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.security.YamcsCredentials;

/**
 * Acts as the single gateway for yamcs-studio to yamcs WebSocketClient.
 */
public class WebSocketRegistrar implements WebSocketClientCallbackListener {

    private static final String USER_AGENT = "Yamcs Studio v" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(WebSocketRegistrar.class.getName());

    private WebSocketClient wsclient;
    private Runnable onConnectCallback; // FIXME ugly

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private final Thread requestSender;

    public WebSocketRegistrar(YamcsConnectionProperties yprops, YamcsCredentials credentials) {
        String user = credentials != null ? credentials.getUsername() : null;
        String pass = credentials != null ? credentials.getPasswordS() : null;
        wsclient = new WebSocketClient(yprops, this, user, pass);
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
    }

    public void sendMessage(WebSocketRequest req) {
        pendingRequests.offer(req);
    }

    @Override
    public void onConnect() { // When the web socket was successfully established
        log.fine("WebSocket established. Notifying listeners");
        onConnectCallback.run(); // FIXME ugly hack
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
        YamcsPlugin plugin = YamcsPlugin.getDefault();
        if (plugin != null) // This can be null when the workbench is closing
            plugin.getConnectionManager().notifyConnectionFailure(null);
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class).processInvalidIdentification(id);
    }

    @Override
    public void onTimeInfo(TimeInfo timeInfo) {
        YamcsPlugin.getDefault().getCatalogue(TimeCatalogue.class).processTimeInfo(timeInfo);
    }

    @Override
    public void onParameterData(ParameterData pdata) {
        YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class).processParameterData(pdata);
    }

    @Override
    public void onClientInfoData(ClientInfo clientInfo) {
        YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processClientInfo(clientInfo);
    }

    @Override
    public void onProcessorInfoData(ProcessorInfo processorInfo) {
        YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processProcessorInfo(processorInfo);
    }

    @Override
    public void onCommandHistoryData(CommandHistoryEntry cmdhistEntry) {
        YamcsPlugin.getDefault().getCatalogue(CommandingCatalogue.class).processCommandHistoryEntry(cmdhistEntry);
    }

    @Override
    public void onStatisticsData(Statistics statistics) {
        YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processStatistics(statistics);
    }

    @Override
    public void onStreamData(StreamData streamData) {
    }

    @Override
    public void onEvent(Event event) {
        YamcsPlugin.getDefault().getCatalogue(EventCatalogue.class).processEvent(event);
    }

    @Override
    public void onAlarm(Alarm alarm) {
        YamcsPlugin.getDefault().getCatalogue(AlarmCatalogue.class).processAlarm(alarm);
    }
}
