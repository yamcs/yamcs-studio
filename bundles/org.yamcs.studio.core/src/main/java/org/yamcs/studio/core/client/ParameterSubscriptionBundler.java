package org.yamcs.studio.core.client;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketRequest;

/**
 * Queues and merges outgoing WebSocket messages related to parameter subscription (or unsubscription). This because in
 * Yamcs Studio individual parameter widget generate distinct sub/unsub events which would otherwise cause request
 * bursts.
 * <p>
 * Each execution empties the queue. By timing the interval between executions you can control the acceptable delay
 * before the queue is emptied.
 */
public class ParameterSubscriptionBundler implements Runnable {

    private static final Logger log = Logger.getLogger(ParameterSubscriptionBundler.class.getName());

    private YamcsStudioClient yamcsClient;

    // Order all subscribe/unsubscribe events
    private Queue<ParameterWebSocketRequest> pendingMessages = new ConcurrentLinkedQueue<>();

    public ParameterSubscriptionBundler(YamcsStudioClient yamcsClient) {
        this.yamcsClient = yamcsClient;
    }

    @Override
    public void run() {
        if (!yamcsClient.isConnected()) {
            return;
        }

        ParameterWebSocketRequest a;
        while ((a = pendingMessages.poll()) != null) {

            while (pendingMessages.peek() != null && isMergeable(a, pendingMessages.peek())) {
                ParameterWebSocketRequest b = pendingMessages.poll();
                a.merge(b);
            }

            CompletableFuture<Void> future = yamcsClient.getWebSocketClient().sendRequest(a);
            future.whenComplete((reply, exc) -> {
                if (exc != null) {
                    log.log(Level.SEVERE, "Server exception while subscribing to parameters", exc);
                }
            });
        }
    }

    public void queue(ParameterWebSocketRequest request) {
        pendingMessages.offer(request);
    }

    public void clearQueue() {
        pendingMessages.clear();
    }

    private boolean isMergeable(WebSocketRequest a, WebSocketRequest b) {
        return a.getOperation().equals(b.getOperation());
    }
}
