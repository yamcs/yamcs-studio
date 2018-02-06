package org.yamcs.studio.core.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketRequest;

/**
 * Queues and merges outgoing WebSocket messages. This is in particular useful for parameter subscriptions because each
 * individual parameter widget generates distinct sub/unsub events which easily generates request bursts.
 * <p>
 * Each execution empties the queue. By timing the interval between executions you can control the acceptable delay
 * before the queue is emptied.
 */
public class MessageBundler implements Runnable {

    private static final Logger log = Logger.getLogger(MessageBundler.class.getName());

    private YamcsClient yamcsClient;

    // Order all subscribe/unsubscribe events
    private Queue<WebSocketRequest> pendingMessages = new ConcurrentLinkedQueue<>();

    public MessageBundler(YamcsClient yamcsClient) {
        this.yamcsClient = yamcsClient;
    }

    @Override
    public void run() {
        if (!yamcsClient.isConnected()) {
            return;
        }

        WebSocketRequest a;
        while ((a = pendingMessages.poll()) != null) {

            while (pendingMessages.peek() != null && isMergeable(a, pendingMessages.peek())) {
                WebSocketRequest b = pendingMessages.poll();
                ((ParameterWebSocketRequest) a).merge((ParameterWebSocketRequest) b);
            }

            log.fine(String.format("Sending message %s", a));
            yamcsClient.getWebSocketClient().sendRequest(a);
        }
    }

    public void queue(WebSocketRequest request) {
        pendingMessages.offer(request);
    }

    public void clearQueue() {
        pendingMessages.clear();
    }

    private boolean isMergeable(WebSocketRequest a, WebSocketRequest b) {
        return a instanceof ParameterWebSocketRequest
                && b instanceof ParameterWebSocketRequest
                && a.getOperation().equals(b.getOperation())
                && (a.getOperation().equals("subscribe") || a.getOperation().equals("unsubscribe"));
    }
}
