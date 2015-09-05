package org.yamcs.studio.core.web;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketClient;
import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Alarms.Alarm;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Websocket.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
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
public class WebSocketRegistrar implements WebSocketClientCallback {

    private static final String USER_AGENT = "Yamcs Studio v" + YamcsPlugin.getDefault().getBundle().getVersion().toString();
    private static final Logger log = Logger.getLogger(WebSocketRegistrar.class.getName());

    private WebSocketClient wsclient;

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

    public void connect() {
        wsclient.connect();
    }

    @Override
    public void connectionFailed(Throwable t) {
        log.fine("Connection Failed. " + t.getMessage());
        ConnectionManager.getInstance().onWebSocketConnectionFailed(t);
    }

    @Override
    public void connected() {
        log.fine("WebSocket established. Notifying listeners");
        ConnectionManager.getInstance().onWebSocketConnected();
        requestSender.start(); // Go over pending subscription requests
    }

    public void disconnect() {
        wsclient.disconnect();
    }

    @Override
    public void disconnected() {
        log.info("WebSocket disconnected. Inform ConnectionManager");
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        if (connectionManager != null) // null when workbench is closing
            connectionManager.onWebSocketDisconnected();
    }

    public void sendMessage(WebSocketRequest req) {
        pendingRequests.offer(req);
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
    public void onInvalidIdentification(NamedObjectId id) {
        YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class).processInvalidIdentification(id);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData data) {
        switch (data.getType()) {
        case TIME_INFO:
            TimeInfo timeInfo = data.getTimeInfo();
            YamcsPlugin.getDefault().getCatalogue(TimeCatalogue.class).processTimeInfo(timeInfo);
            break;
        case PARAMETER:
            ParameterData pdata = data.getParameterData();
            YamcsPlugin.getDefault().getCatalogue(ParameterCatalogue.class).processParameterData(pdata);
            break;
        case CLIENT_INFO:
            ClientInfo clientInfo = data.getClientInfo();
            YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processClientInfo(clientInfo);
            break;
        case PROCESSOR_INFO:
            ProcessorInfo processorInfo = data.getProcessorInfo();
            YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processProcessorInfo(processorInfo);
            break;
        case CMD_HISTORY:
            CommandHistoryEntry cmdhistEntry = data.getCommand();
            YamcsPlugin.getDefault().getCatalogue(CommandingCatalogue.class).processCommandHistoryEntry(cmdhistEntry);
            break;
        case PROCESSING_STATISTICS:
            Statistics statistics = data.getStatistics();
            YamcsPlugin.getDefault().getCatalogue(ManagementCatalogue.class).processStatistics(statistics);
            break;
        case EVENT:
            Event event = data.getEvent();
            YamcsPlugin.getDefault().getCatalogue(EventCatalogue.class).processEvent(event);
            break;
        case ALARM:
            Alarm alarm = data.getAlarm();
            YamcsPlugin.getDefault().getCatalogue(AlarmCatalogue.class).processAlarm(alarm);
            break;
        default:
            throw new IllegalArgumentException("Unexpected data type " + data.getType());
        }
    }
}
