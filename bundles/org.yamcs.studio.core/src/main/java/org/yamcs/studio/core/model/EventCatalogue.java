package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.client.BulkRestDataReceiver;
import org.yamcs.client.ClientException;
import org.yamcs.client.WebSocketClientCallback;
import org.yamcs.client.WebSocketRequest;
import org.yamcs.protobuf.CreateEventRequest;
import org.yamcs.protobuf.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.InvalidProtocolBufferException;

public class EventCatalogue implements Catalogue, WebSocketClientCallback {

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
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("events", "subscribe"), this);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasEvent()) {
            processEvent(msg.getEvent());
        }
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // Don't assume anything. Let listeners choose whether they want
        // to register as an instance listeners.

        // but make sure to subscribe to the events of the new instance
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("events", "subscribe"), this);
    }

    @Override
    public void onYamcsDisconnected() {
    }

    public void processEvent(Event event) {
        eventListeners.forEach(l -> l.processEvent(event));
    }

    public CompletableFuture<byte[]> fetchLatestEvents(String instance) {
        String resource = "/archive/" + instance + "/events";

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.get(resource, null);
    }

    /**
     * Downloads a batch of events in the specified time range. These events are not distributed to registered
     * listeners, but only to the provided listener.
     */
    public CompletableFuture<Void> downloadEvents(long start, long stop, BulkEventListener listener) {
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
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        EventBatchGenerator batchGenerator = new EventBatchGenerator(listener);
        return yamcsClient.streamGet(resource, null, batchGenerator).whenComplete((data, exc) -> {
            if (!batchGenerator.events.isEmpty()) {
                listener.processEvents(new ArrayList<>(batchGenerator.events));
            }
        });
    }

    public CompletableFuture<byte[]> createEvent(String message, Date time, EventSeverity severity) {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        String resource = "/archive/" + instance + "/events/";

        CreateEventRequest.Builder requestb = CreateEventRequest.newBuilder();
        requestb.setMessage(message);
        requestb.setSeverity(severity.toString());
        long instant = TimeEncoding.fromDate(time);
        String isoString = TimeEncoding.toString(instant);
        requestb.setTime(isoString);

        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        return yamcsClient.post(resource, requestb.build());
    }

    private static class EventBatchGenerator implements BulkRestDataReceiver {

        private BulkEventListener listener;
        private List<Event> events = new ArrayList<>();

        public EventBatchGenerator(BulkEventListener listener) {
            this.listener = listener;
        }

        @Override
        public void receiveData(byte[] data) throws ClientException {
            try {
                events.add(Event.parseFrom(data));
            } catch (InvalidProtocolBufferException e) {
                throw new ClientException("Failed to decode server response", e);
            }
            if (events.size() >= 500) {
                listener.processEvents(new ArrayList<>(events));
                events.clear();
            }
        }
    }
}
