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
import org.yamcs.api.YamcsConnectionProperties;
import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.api.rest.RestClient;

import com.google.protobuf.MessageLite;

import io.netty.handler.codec.http.HttpMethod;

/**
 * Wraps the yamcs-core REST client with extra functionality, such as job
 * monitoring.
 * <p>
 * Intended to in the future also provide passage to the websocket api
 */
public class YamcsClient {

    private static final Logger log = Logger.getLogger(YamcsClient.class.getName());
    private final RestClient restClient;

    // Keep track of ongoing jobs, to respond to user cancellation requests.
    private ScheduledExecutorService canceller = Executors.newSingleThreadScheduledExecutor();
    private Map<IProgressMonitor, CompletableFuture<?>> cancellableJobs = new ConcurrentHashMap<>();

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

    public CompletableFuture<byte[]> get(String uri, MessageLite msg, MessageLite.Builder target) {
        return requestAsync(HttpMethod.GET, uri, msg, target);
    }

    public CompletableFuture<Void> streamGet(String uri, MessageLite msg, BulkRestDataReceiver receiver) {
        return doRequestWithDelimitedResponse(HttpMethod.GET, uri, msg, receiver);
    }

    public CompletableFuture<byte[]> post(String uri, MessageLite msg, MessageLite.Builder target) {
        return requestAsync(HttpMethod.POST, uri, msg, target);
    }

    public CompletableFuture<byte[]> patch(String uri, MessageLite msg, MessageLite.Builder target) {
        return requestAsync(HttpMethod.PATCH, uri, msg, target);
    }

    public CompletableFuture<byte[]> put(String uri, MessageLite msg, MessageLite.Builder target) {
        return requestAsync(HttpMethod.PUT, uri, msg, target);
    }

    public CompletableFuture<byte[]> delete(String uri, MessageLite msg, MessageLite.Builder target) {
        return requestAsync(HttpMethod.DELETE, uri, msg, target);
    }

    private <S extends MessageLite> CompletableFuture<byte[]> requestAsync(HttpMethod method, String uri, MessageLite requestBody, MessageLite.Builder target) {
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

    private <S extends MessageLite> CompletableFuture<Void> doRequestWithDelimitedResponse(HttpMethod method, String uri, MessageLite requestBody, BulkRestDataReceiver receiver) {
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
        canceller.shutdown();
    }
}
