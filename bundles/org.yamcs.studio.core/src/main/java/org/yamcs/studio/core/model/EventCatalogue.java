package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.YamcsApiException;
import org.yamcs.api.rest.BulkRestDataReceiver;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.protobuf.Yamcs.EventOrBuilder;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.WebSocketRegistrar;
import org.yamcs.studio.core.web.YamcsClient;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.InvalidProtocolBufferException;


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
        return restClient.get(resource, null);
    }

    /**
     * Downloads a batch of events in the specified time range. These events are not
     * distributed to registered listeners, but only to the provided listener.
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
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        EventBatchGenerator batchGenerator = new EventBatchGenerator(listener);
        return restClient.streamGet(resource, null, batchGenerator).whenComplete((data, exc) -> {
            if (!batchGenerator.events.isEmpty()) {
                listener.processEvents(new ArrayList<>(batchGenerator.events));
            }
        });
    }


    public CompletableFuture<byte[]> createEvent(String source, int sequenceNumber, String message, long generationTime, long receptionTime, EventSeverity severity)
    {
    	String instance = ManagementCatalogue.getCurrentYamcsInstance();
        String resource = "/archive/" + instance + "/events/";
        
        Event event= Event.newBuilder().setSource(source).setSeqNumber(sequenceNumber)
        		.setMessage(message).setGenerationTime(generationTime)
        		.setReceptionTime(receptionTime).setSeverity(severity).build();
        
        YamcsClient restClient = ConnectionManager.requireYamcsClient();
        return restClient.post(resource, event);
    }

    private static class EventBatchGenerator implements BulkRestDataReceiver {

        private BulkEventListener listener;
        private List<Event> events = new ArrayList<>();

        public EventBatchGenerator(BulkEventListener listener) {
            this.listener = listener;
        }

        @Override
        public void receiveData(byte[] data) throws YamcsApiException {
            try {
                events.add(Event.parseFrom(data));
            } catch (InvalidProtocolBufferException e) {
                throw new YamcsApiException("Failed to decode server response", e);
            }
            if (events.size() >= 500) {
                listener.processEvents(new ArrayList<>(events));
                events.clear();
            }
        }
    }
}
