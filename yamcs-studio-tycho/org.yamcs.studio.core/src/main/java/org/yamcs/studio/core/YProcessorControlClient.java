package org.yamcs.studio.core;

import static org.yamcs.api.Protocol.YPROCESSOR_INFO_ADDRESS;
import static org.yamcs.api.Protocol.YPROCESSOR_STATISTICS_ADDRESS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientMessage;
import org.yamcs.YamcsException;
import org.yamcs.api.ConnectionListener;
import org.yamcs.api.Protocol;
import org.yamcs.api.YamcsApiException;
import org.yamcs.api.YamcsClient;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.YamcsConnector;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.web.RestClient;

/**
 * controls yprocessors in yamcs server via hornetq TODO move this to websocket instead
 *
 * @author nm
 *
 */
public class YProcessorControlClient implements StudioConnectionListener, ConnectionListener {

    private static final Logger log = Logger.getLogger(YProcessorControlClient.class.getName());

    private YamcsConnector yconnector;
    private YamcsClient yclient;
    private Set<ProcessorListener> listeners = new HashSet<>();

    private Map<String, ProcessorInfo> processorInfoByName = new ConcurrentHashMap<>();
    private Map<Integer, ClientInfo> clientInfoById = new ConcurrentHashMap<>();

    public YProcessorControlClient() {
        yconnector = new YamcsConnector();
        yconnector.addConnectionListener(this);
        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    public void addProcessorListener(ProcessorListener l) {
        listeners.add(l);

        // Inform listeners of the current model
        processorInfoByName.forEach((k, v) -> l.processorUpdated(v));
        clientInfoById.forEach((k, v) -> l.clientUpdated(v));
    }

    /**
     * Called when we get green light from YamcsPlugin
     */
    @Override
    public void processConnectionInfo(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps,
            RestClient restClient, WebSocketRegistrar webSocketClient) {
        yconnector.connect(hornetqProps);
    }

    /**
     * Called when YamcsPlugin wants this connection to stop (might be resumed latter with
     * processConnectionInfo)
     */
    @Override
    public void disconnect() {
        yconnector.disconnect();
        listeners.forEach(l -> l.clientDisconnected(null));
    }

    @Override
    public void connecting(String url) {
    }

    @Override
    public void connected(String url) {
        try {
            YamcsClient browser = yconnector.getSession().newClientBuilder().setDataConsumer(YPROCESSOR_INFO_ADDRESS, YPROCESSOR_INFO_ADDRESS)
                    .setBrowseOnly(true).build();
            yclient = yconnector.getSession().newClientBuilder()
                    .setRpc(true).setDataConsumer(YPROCESSOR_INFO_ADDRESS, null).build();

            ClientMessage m1;
            while ((m1 = browser.dataConsumer.receiveImmediate()) != null) {//send all the messages from the queue first
                processUpdate(m1);
            }
            browser.close();

            yclient.dataConsumer.setMessageHandler(msg -> processUpdate(msg));
            YamcsClient yclientStats = yconnector.getSession().newClientBuilder().setDataConsumer(YPROCESSOR_STATISTICS_ADDRESS, null).build();
            yclientStats.dataConsumer.setMessageHandler(msg -> sendStatistics(msg));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not retrieve link info", e);
        }
    }

    private void processUpdate(ClientMessage msg) {
        try {
            String eventName = msg.getStringProperty(Protocol.HDR_EVENT_NAME);
            if ("yprocUpdated".equals(eventName)) {
                ProcessorInfo processorInfo = (ProcessorInfo) Protocol.decode(msg, ProcessorInfo.newBuilder());
                processorInfoByName.put(processorInfo.getName(), processorInfo);
                listeners.forEach(l -> l.processorUpdated(processorInfo));
            } else if ("yprocClosed".equals(eventName)) {
                ProcessorInfo processorInfo = (ProcessorInfo) Protocol.decode(msg, ProcessorInfo.newBuilder());
                processorInfoByName.remove(processorInfo.getName());
                listeners.forEach(l -> l.yProcessorClosed(processorInfo));
            } else if ("clientUpdated".equals(eventName)) {
                ClientInfo clientInfo = (ClientInfo) Protocol.decode(msg, ClientInfo.newBuilder());
                clientInfoById.put(clientInfo.getId(), clientInfo);
                listeners.forEach(l -> l.clientUpdated(clientInfo));
            } else if ("clientDisconnected".equals(eventName)) {
                ClientInfo clientInfo = (ClientInfo) Protocol.decode(msg, ClientInfo.newBuilder());
                clientInfoById.remove(clientInfo.getId());
                listeners.forEach(l -> l.clientDisconnected(clientInfo));
            } else {
                log.warning("Received unknown message '" + eventName + "'");
            }
        } catch (YamcsApiException e) {
            log.log(Level.SEVERE, "Error when decoding message", e);
        }
    }

    private void sendStatistics(ClientMessage msg) {
        try {
            Statistics s = (Statistics) Protocol.decode(msg, Statistics.newBuilder());
            listeners.forEach(l -> l.updateStatistics(s));
        } catch (YamcsApiException e) {
            log.log(Level.SEVERE, "Error when decoding message", e);
        }
    }

    @Override
    public void connectionFailed(String url, YamcsException exception) {
    }

    @Override
    public void disconnected() {
        yclient = null;
    }

    @Override
    public void log(String message) {
    }

    public ProcessorInfo getProcessorInfo(String processorName) {
        return processorInfoByName.get(processorName);
    }

    public ClientInfo getClientInfo(int clientId) {
        return clientInfoById.get(clientId);
    }

    public void close() throws HornetQException {
        if (yclient != null)
            yclient.close();
    }

}
