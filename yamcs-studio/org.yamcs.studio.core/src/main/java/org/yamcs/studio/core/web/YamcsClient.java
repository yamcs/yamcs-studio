package org.yamcs.studio.core.web;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.yamcs.api.YamcsApiException;
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.api.rest.RestClient;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Wraps the yamcs-core REST client with extra functionality, such as job
 * monitoring.
 * <p>
 * Intended to in the future also provide passage to the websocket api
 */
public class YamcsClient {

    private final RestClient restClient;

    // Keep track of ongoing jobs, to respond to user cancellation requests.
    private ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();
    private Map<IProgressMonitor, CompletableFuture<?>> cancellableJobs = new ConcurrentHashMap<>();

    public static final ResponseHandler NULL_RESPONSE_HANDLER = new ResponseHandler() {

        @Override
        public void onMessage(MessageLite responseMsg) {
            // NOP
        }

        @Override
        public void onException(Exception e) {
            Logger.getLogger(YamcsClient.class.getName()).log(Level.SEVERE, "Error while processing request", e);
        }
    };

    public YamcsClient(YamcsConnectionProperties yprops) {
        this.restClient = new RestClient(yprops);
        restClient.setAutoclose(false);

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

    public CompletableFuture<byte[]> get(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        return requestAsync(HttpMethod.GET, uri, msg, target, handler);
    }

    public CompletableFuture<Void> streamGet(String uri, MessageLite msg, BuilderGenerator generator, ResponseHandler handler) {
        return doRequestWithDelimitedResponse(HttpMethod.GET, uri, msg, generator, handler);
    }

    public CompletableFuture<byte[]> post(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        return requestAsync(HttpMethod.POST, uri, msg, target, handler);
    }

    public CompletableFuture<byte[]> patch(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        return requestAsync(HttpMethod.PATCH, uri, msg, target, handler);
    }

    public CompletableFuture<byte[]> put(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        return requestAsync(HttpMethod.PUT, uri, msg, target, handler);
    }

    public CompletableFuture<byte[]> delete(String uri, MessageLite msg, MessageLite.Builder target, ResponseHandler handler) {
        return requestAsync(HttpMethod.DELETE, uri, msg, target, handler);
    }

    private <S extends MessageLite> CompletableFuture<byte[]> requestAsync(HttpMethod method, String uri, MessageLite requestBody, MessageLite.Builder target, ResponseHandler handler) {
        CompletableFuture<byte[]> cf;
        if (requestBody != null) {
            cf = restClient.doRequest(uri, method, requestBody.toByteArray());
        } else {
            cf = restClient.doRequest(uri, method);
        }

        Job job = Job.create(method + " /api" + uri, monitor -> {
            cancellableJobs.put(monitor, cf);
            try {
                byte[] response = cf.get();
                if (target != null) {
                    try {
                        MessageLite responseMsg = target.mergeFrom(response).build();
                        handler.onMessage(responseMsg);
                    } catch (InvalidProtocolBufferException e) {
                        handler.onException(e);
                    }
                } else {
                    handler.onMessage(null);
                }
                return Status.OK_STATUS;
            } catch (CancellationException | InterruptedException e) {
                ///handler.onException(e);
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                handler.onException(e);
                return Status.OK_STATUS;
            }
        });
        job.setPriority(Job.SHORT);
        job.schedule();
        return cf;
    }

    private <S extends MessageLite> CompletableFuture<Void> doRequestWithDelimitedResponse(HttpMethod method, String uri, MessageLite requestBody, BuilderGenerator builderGenerator, ResponseHandler handler) {
        BulkRestDataReceiver receiver = new BulkRestDataReceiver() {

            @Override
            public void receiveData(byte[] data) throws YamcsApiException {
                try {
                    MessageLite proto = builderGenerator.newBuilder().mergeFrom(data).build();
                    handler.onMessage(proto);
                } catch (InvalidProtocolBufferException e) {
                    throw new YamcsApiException("Failed to decode protobuf message", e);
                }
            }
        };

        CompletableFuture<Void> cf;
        if (requestBody != null) {
            cf = restClient.doBulkGetRequest(uri, requestBody.toByteArray(), receiver);
        } else {
            cf = restClient.doBulkGetRequest(uri, receiver);
        }

        Job job = Job.create(method + " /api" + uri, monitor -> {
            cancellableJobs.put(monitor, cf);

            try {
                cf.get();
                handler.onMessage(null);
                return Status.OK_STATUS;
            } catch (CancellationException | InterruptedException e) {
                ///handler.onException(e);
                return Status.CANCEL_STATUS;
            } catch (ExecutionException e) {
                handler.onException(e);
                return Status.OK_STATUS;
            }
        });
        job.setPriority(Job.LONG);
        job.schedule();
        return cf;
    }

    /**
     * Performs an orderly shutdown of this service
     */
    public void shutdown() {
        restClient.close(); // Shuts down the thread pool
        canceller.shutdown();
    }

    /**
     * Used to bypass protobuf restriction of only being able to make builders from an existing
     * message. Perhaps there's a better way.
     */
    public static interface BuilderGenerator {
        MessageLite.Builder newBuilder();
    }
}
