package org.yamcs.studio.core.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Rest.ListEventsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.NotConnectedException;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.WebSocketRegistrar;
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

    public void fetchLatestEvents(String instance, ResponseHandler responseHandler) {
        String resource = "/archive/" + instance + "/events";
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.get(resource, null, ListEventsResponse.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }

    public void downloadEvents(long start, long stop, ResponseHandler responseHandler) {
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
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient != null) {
            restClient.streamGet(resource, null, () -> Event.newBuilder(), responseHandler);
        } else {
            responseHandler.onException(new NotConnectedException());
        }
    }
}
