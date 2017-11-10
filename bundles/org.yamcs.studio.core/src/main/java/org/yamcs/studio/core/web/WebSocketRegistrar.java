package org.yamcs.studio.core.web;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.ws.WebSocketClient;
import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Alarms.AlarmData;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Commanding.CommandQueueEvent;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Web.ConnectionInfo;
import org.yamcs.protobuf.Web.WebSocketExtensionData;
import org.yamcs.protobuf.Web.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.LinkEvent;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.AlarmCatalogue;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.ExtensionCatalogue;
import org.yamcs.studio.core.model.LinkCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;

import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.channel.ChannelFuture;

/**
 * Acts as the single gateway for yamcs-studio to yamcs WebSocketClient.
 */
public class WebSocketRegistrar implements WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(WebSocketRegistrar.class.getName());

    private WebSocketClient wsclient;

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private final Thread requestSender;

    public WebSocketRegistrar(YamcsConnectionProperties yprops) {
        wsclient = new WebSocketClient(yprops, this);
        wsclient.setConnectionTimeoutMs(3000);
        wsclient.setUserAgent(YamcsPlugin.getDefault().getProductIdentifier());
        requestSender = new Thread(() -> {
            try {
                sendMergedRequests();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        });
    }

    public ChannelFuture connect() {
        return wsclient.connect();
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

    @Override
    public void disconnected() {
        log.info("WebSocket disconnected. Inform ConnectionManager");
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        if (connectionManager != null) // null when workbench is closing
            connectionManager.disconnect(true /* lost */);
    }

    public void sendMessage(WebSocketRequest req) {
        pendingRequests.offer(req);
    }

    private void sendMergedRequests() throws InterruptedException {
        while (true) {
            WebSocketRequest evt = pendingRequests.take();

            // We now have at least one event to handle
            Thread.sleep(100); // Wait for more events, before going into synchronized block
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
        //wsclient.disconnect();
        wsclient.shutdown();
    }

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        ParameterCatalogue.getInstance().processInvalidIdentification(id);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData data) {
        switch (data.getType()) {
        case CONNECTION_INFO:
            ConnectionInfo connectionInfo = data.getConnectionInfo();
            ManagementCatalogue.getInstance().processConnectionInfo(connectionInfo);
            break;
        case TIME_INFO:
            TimeInfo timeInfo = data.getTimeInfo();
            TimeCatalogue.getInstance().processTimeInfo(timeInfo);
            break;
        case PARAMETER:
            ParameterData pdata = data.getParameterData();
            ParameterCatalogue.getInstance().processParameterData(pdata);
            break;
        case CLIENT_INFO:
            ClientInfo clientInfo = data.getClientInfo();
            ManagementCatalogue.getInstance().processClientInfo(clientInfo);
            break;
        case PROCESSOR_INFO:
            ProcessorInfo processorInfo = data.getProcessorInfo();
            ManagementCatalogue.getInstance().processProcessorInfo(processorInfo);
            break;
        case CMD_HISTORY:
            CommandHistoryEntry cmdhistEntry = data.getCommand();
            CommandingCatalogue.getInstance().processCommandHistoryEntry(cmdhistEntry);
            break;
        case PROCESSING_STATISTICS:
            Statistics statistics = data.getStatistics();
            ManagementCatalogue.getInstance().processStatistics(statistics);
            break;
        case EVENT:
            Event event = data.getEvent();
            EventCatalogue.getInstance().processEvent(event);
            break;
        case ALARM_DATA:
            AlarmData alarm = data.getAlarmData();
            AlarmCatalogue.getInstance().processAlarmData(alarm);
            break;
        case LINK_EVENT:
            LinkEvent linkEvent = data.getLinkEvent();
            LinkCatalogue.getInstance().processLinkEvent(linkEvent);
            break;
        case COMMAND_QUEUE_INFO:
            CommandQueueInfo queueInfo = data.getCommandQueueInfo();
            CommandingCatalogue.getInstance().processCommandQueueInfo(queueInfo);
            break;
        case COMMAND_QUEUE_EVENT:
            CommandQueueEvent queueEvent = data.getCommandQueueEvent();
            CommandingCatalogue.getInstance().processCommandQueueEvent(queueEvent);
            break;
        case EXTENSION_DATA:
            WebSocketExtensionData extData = data.getExtensionData();
            YamcsPlugin plugin = YamcsPlugin.getDefault();
            int extType = extData.getType();
            ExtensionCatalogue catalogue = plugin.getExtensionCatalogue(extType);
            if (catalogue != null) {
                try {
                    catalogue.processMessage(extType, extData.getData());
                } catch (InvalidProtocolBufferException e) {
                    log.log(Level.SEVERE, "Invalid message", e);
                }
            } else {
                log.warning("Unexpected message of extension type " + extType);
            }
            break;
        default:
            throw new IllegalArgumentException("Unexpected data type " + data.getType());
        }
    }
}
