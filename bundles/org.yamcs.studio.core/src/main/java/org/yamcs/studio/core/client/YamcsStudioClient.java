package org.yamcs.studio.core.client;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.client.BulkRestDataReceiver;
import org.yamcs.client.ConnectionListener;
import org.yamcs.client.WebSocketClient;
import org.yamcs.client.WebSocketClientCallback;
import org.yamcs.client.WebSocketRequest;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ConnectionInfo;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketReplyData;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketSubscriptionData;

import com.google.protobuf.MessageLite;

/**
 * Provides passage to Yamcs. This covers both the REST and WebSocket API.
 * 
 * TODO currently reconnection can only be cancelled on initial connect. We should also make it cancellable (via Job UI)
 * on auto reconnect.
 */
public class YamcsStudioClient implements WebSocketClientCallback {

    private static final Logger log = Logger.getLogger(YamcsStudioClient.class.getName());

    private YamcsConnectionProperties yprops;
    private String caCertFile;
    private String application;

    private YamcsClient yamcsClient;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();

    private ParameterSubscriptionBundler parameterSubscriptionBundler;

    // Keep track of ongoing jobs, to respond to user cancellation requests.
    private ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();
    private Map<IProgressMonitor, Future<?>> cancellableJobs = new ConcurrentHashMap<>();

    public YamcsStudioClient(String application) {
        this.application = application;

        canceller.scheduleWithFixedDelay(() -> {
            cancellableJobs.forEach((monitor, future) -> {
                if (monitor.isCanceled()) {
                    future.cancel(true);
                    cancellableJobs.remove(monitor);
                } else if (future.isDone()) {
                    cancellableJobs.remove(monitor);
                }
            });
        }, 2000, 1000, TimeUnit.MILLISECONDS);

        parameterSubscriptionBundler = new ParameterSubscriptionBundler(this);
        executor.scheduleWithFixedDelay(parameterSubscriptionBundler, 200, 400, TimeUnit.MILLISECONDS);
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        this.connectionListeners.add(connectionListener);
    }

    private FutureTask<ConnectionInfo> doConnect() {
        if (yamcsClient != null) {
            yamcsClient.close();
        }

        YamcsClient.Builder yamcsClientBuilder = YamcsClient.newBuilder(yprops.getHost(), yprops.getPort())
                // .withConnectionAttempts(10) // This works, but is not very useful unless reconnecting
                .withTls(yprops.isTls())
                .withVerifyTls(false)
                .withUserAgent(application)
                .withInitialInstance(yprops.getInstance(), false, true);

        if (caCertFile != null) {
            yamcsClientBuilder.withCaCertFile(Paths.get(caCertFile));
        }

        yamcsClient = yamcsClientBuilder.build();
        yamcsClient.addWebSocketListener(this);

        FutureTask<ConnectionInfo> future = new FutureTask<>(() -> {
            log.info("Connecting to " + yprops);
            if (yprops.getUsername() == null) {
                return yamcsClient.connectAnonymously();
            } else {
                return yamcsClient.connect(yprops.getUsername(), yprops.getPassword());
            }
        });
        executor.submit(future);

        // Add Progress indicator in status bar
        String jobName = "Connecting to " + yprops;
        scheduleAsJob(jobName, future, Job.SHORT);

        return future;
    }

    @Override
    public void connecting() {
        for (ConnectionListener cl : connectionListeners) {
            cl.connecting(null);
        }
    }

    @Override
    public void connected() {
        log.info("Connected to " + yprops);
        parameterSubscriptionBundler.clearQueue();
        for (ConnectionListener listener : connectionListeners) {
            listener.connected(null);
        }
    }

    @Override
    public void disconnected() {
        if (isConnected()) {
            log.warning("Connection to " + yprops + " lost");
        }
        for (ConnectionListener listener : connectionListeners) {
            listener.disconnected();
        }
    }

    @Override
    public void onMessage(WebSocketSubscriptionData data) {
        // NOP
    }

    public void disconnect() {
        log.info("Disconnecting from " + yprops);
        if (yamcsClient != null) {
            yamcsClient.close();
        }
    }

    public boolean isConnected() {
        return yamcsClient != null && yamcsClient.isConnected();
    }

    public boolean isConnecting() {
        return yamcsClient != null && yamcsClient.isConnecting();
    }

    public Future<ConnectionInfo> connect(YamcsConnectionProperties yprops) {
        this.yprops = yprops;
        return doConnect();
    }

    public YamcsConnectionProperties getYamcsConnectionProperties() {
        return yprops;
    }

    public ConnectionInfo getConnectionInfo() {
        return yamcsClient.getConnectionInfo();
    }

    public CompletableFuture<WebSocketReplyData> subscribe(WebSocketRequest req,
            WebSocketClientCallback messageHandler) {
        yamcsClient.addWebSocketListener(messageHandler);
        if (req instanceof ParameterWebSocketRequest) {
            parameterSubscriptionBundler.queue((ParameterWebSocketRequest) req);
            return null; // TODO ?
        } else {
            return yamcsClient.getWebSocketClient().sendRequest(req);
        }
    }

    public CompletableFuture<WebSocketReplyData> sendWebSocketMessage(WebSocketRequest req) {
        if (req instanceof ParameterWebSocketRequest) {
            parameterSubscriptionBundler.queue((ParameterWebSocketRequest) req);
            return null; // TODO ?
        } else {
            return yamcsClient.getWebSocketClient().sendRequest(req);
        }
    }

    public CompletableFuture<byte[]> get(String uri, MessageLite msg) {
        CompletableFuture<byte[]> cf = yamcsClient.get(uri, msg);
        String jobName = "GET /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    public CompletableFuture<Void> streamGet(String uri, MessageLite msg, BulkRestDataReceiver receiver) {
        CompletableFuture<Void> cf = yamcsClient.streamGet(uri, msg, receiver);
        String jobName = "GET /api" + uri;
        scheduleAsJob(jobName, cf, Job.LONG);
        return cf;
    }

    public CompletableFuture<Void> streamPost(String uri, MessageLite msg, BulkRestDataReceiver receiver) {
        CompletableFuture<Void> cf = yamcsClient.streamPost(uri, msg, receiver);
        String jobName = "POST /api" + uri;
        scheduleAsJob(jobName, cf, Job.LONG);
        return cf;
    }

    public CompletableFuture<byte[]> post(String uri, MessageLite msg) {
        CompletableFuture<byte[]> cf = yamcsClient.post(uri, msg);
        String jobName = "POST /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    public CompletableFuture<byte[]> patch(String uri, MessageLite msg) {
        CompletableFuture<byte[]> cf = yamcsClient.patch(uri, msg);
        String jobName = "PATCH /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    public CompletableFuture<byte[]> put(String uri, MessageLite msg) {
        CompletableFuture<byte[]> cf = yamcsClient.put(uri, msg);
        String jobName = "PUT /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    public CompletableFuture<byte[]> delete(String uri, MessageLite msg) {
        CompletableFuture<byte[]> cf = yamcsClient.delete(uri, msg);
        String jobName = "DELETE /api" + uri;
        scheduleAsJob(jobName, cf, Job.SHORT);
        return cf;
    }

    private void scheduleAsJob(String jobName, Future<?> cf, int priority) {
        Job job = Job.create(jobName, monitor -> {
            cancellableJobs.put(monitor, cf);

            try {
                cf.get();
                return Status.OK_STATUS;
            } catch (CancellationException | InterruptedException e) {
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                log.log(Level.SEVERE, "Exception while executing job '" + jobName + "': " + cause.getMessage(),
                        cause);
                return Status.OK_STATUS;
            }
        });
        job.setPriority(Job.LONG);
        job.schedule();
    }

    public WebSocketClient getWebSocketClient() {
        return yamcsClient.getWebSocketClient();
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        if (yamcsClient != null) {
            yamcsClient.close();
        }
        executor.shutdown();
        canceller.shutdown();
    }
}
