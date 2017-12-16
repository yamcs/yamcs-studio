package org.yamcs.studio.core.web;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.api.rest.RestClient;
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
import com.google.protobuf.MessageLite;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Wraps the yamcs-core REST client with extra functionality, such as job monitoring.
 * <p>
 * Intended to in the future also provide passage to the websocket api
 */
public class YamcsClient implements WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(YamcsClient.class.getName());

    private final RestClient restClient;
    private WebSocketClient wsclient;

    // Order all subscribe/unsubscribe events
    private final BlockingQueue<WebSocketRequest> pendingRequests = new LinkedBlockingQueue<>();

    private final Thread requestSender;

    // Keep track of ongoing jobs, to respond to user cancellation requests.
    private ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();
    private Map<IProgressMonitor, CompletableFuture<?>> cancellableJobs = new ConcurrentHashMap<>();

    public YamcsClient(YamcsConnectionProperties yprops) {
        restClient = new RestClient(yprops);
        restClient.setAutoclose(false);

        wsclient = new WebSocketClient(yprops, this);
        wsclient.setConnectionTimeoutMs(3000);
        wsclient.enableReconnection(false);
        wsclient.setUserAgent(Platform.getProduct() + "v" + Platform.getProduct().getDefiningBundle().getVersion());
        requestSender = new Thread(() -> {
            try {
                sendMergedRequests();
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "OOPS, got interrupted", e);
            }
        });

        canceller.scheduleWithFixedDelay(() -> {
            cancellableJobs.forEach((monitor, future) -> {
                if (monitor.isCanceled()) {
                    future.cancel(false);
                    cancellableJobs.remove(monitor);
                } else if (future.isDone()) {
                    cancellableJobs.remove(monitor);
                }
            });
        }, 2000, 1000, TimeUnit.MILLISECONDS);
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

    public ChannelFuture connect() {
        return wsclient.connect();
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

                while (pendingRequests.peek() != null && evt instanceof MergeableWebSocketRequest
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

    @Override
    public void onInvalidIdentification(NamedObjectId id) {
        ParameterCatalogue.getInstance().processInvalidIdentification(id);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData data) {

        // Stop processing messages on shutdown
        YamcsPlugin plugin = YamcsPlugin.getDefault();
        if (plugin == null) {
            return;
        }

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

    public CompletableFuture<byte[]> get(String uri, MessageLite msg) {
        return requestAsync(HttpMethod.GET, uri, msg);
    }

    public CompletableFuture<Void> streamGet(String uri, MessageLite msg, BulkRestDataReceiver receiver) {
        return doRequestWithDelimitedResponse(HttpMethod.GET, uri, msg, receiver);
    }

    public CompletableFuture<byte[]> post(String uri, MessageLite msg) {
        return requestAsync(HttpMethod.POST, uri, msg);
    }

    public CompletableFuture<byte[]> patch(String uri, MessageLite msg) {
        return requestAsync(HttpMethod.PATCH, uri, msg);
    }

    public CompletableFuture<byte[]> put(String uri, MessageLite msg) {
        return requestAsync(HttpMethod.PUT, uri, msg);
    }

    public CompletableFuture<byte[]> delete(String uri, MessageLite msg) {
        return requestAsync(HttpMethod.DELETE, uri, msg);
    }

    private <S extends MessageLite> CompletableFuture<byte[]> requestAsync(HttpMethod method, String uri,
            MessageLite requestBody) {
        CompletableFuture<byte[]> cf;
        if (requestBody != null) {
            cf = restClient.doRequest(uri, method, requestBody.toByteArray());
        } else {
            cf = restClient.doRequest(uri, method);
        }

        String jobName = method + " /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    private <S extends MessageLite> CompletableFuture<Void> doRequestWithDelimitedResponse(HttpMethod method,
            String uri, MessageLite requestBody, BulkRestDataReceiver receiver) {
        CompletableFuture<Void> cf;
        if (requestBody != null) {
            cf = restClient.doBulkGetRequest(uri, requestBody.toByteArray(), receiver);
        } else {
            cf = restClient.doBulkGetRequest(uri, receiver);
        }

        String jobName = method + " /api" + uri;
        scheduleAsJob(jobName, cf, Job.LONG);
        return cf;
    }

    private void scheduleAsJob(String jobName, CompletableFuture<?> cf, int priority) {
        Job job = Job.create(jobName, monitor -> {
            cancellableJobs.put(monitor, cf);

            try {
                cf.get();
                return Status.OK_STATUS;
            } catch (CancellationException | InterruptedException e) {
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                log.log(Level.SEVERE, "Exception while executing job '" + jobName + "'", e.getCause());
                return Status.OK_STATUS;
            }
        });
        job.setPriority(Job.LONG);
        job.schedule();
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        restClient.close(); // Shuts down the thread pool
        // wsclient.disconnect();
        wsclient.shutdown();
        canceller.shutdown();
    }
}
