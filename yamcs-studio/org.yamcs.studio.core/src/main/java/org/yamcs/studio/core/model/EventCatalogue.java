package org.yamcs.studio.core.model;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Rest.ListEventsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.WebSocketRegistrar;
import org.yamcs.studio.core.web.YamcsClient;
import org.yamcs.utils.TimeEncoding;

public class EventCatalogue implements Catalogue {

    private Set<EventListener> eventListeners = new CopyOnWriteArraySet<>();

    public static EventCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(EventCatalogue.class);
    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    @Override
    public void onStudioConnect() {
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("events", "subscribe"));
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // Don't assume anything. Let listeners choose whether they want
        // to register as an instance listeners.
    }

    @Override
    public void onStudioDisconnect() {
    }

    public void processEvent(Event event) {
        eventListeners.forEach(l -> l.processEvent(event));
    }

    public CompletableFuture<byte[]> fetchLatestEvents(String instance) {
        String resource = "/archive/" + instance + "/events";
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.get(resource, null, ListEventsResponse.newBuilder());
    }

    public CompletableFuture<Void> downloadEvents(long start, long stop, BulkRestDataReceiver receiver) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        String resource = "/archive/" + instance + "/downloads/events";
        if (start != TimeEncoding.INVALID_INSTANT) {
            resource += "?start=" + start;
            if (stop != TimeEncoding.INVALID_INSTANT) {
                resource += "&stop=" + stop;
            }
        } else if (stop != TimeEncoding.INVALID_INSTANT) {
            resource += "?stop=" + stop;
        }
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.streamGet(resource, null, receiver);
    }
}
