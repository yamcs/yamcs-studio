package org.yamcs.studio.core.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketRequest;
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

    @Override
    public void onStudioConnect() {
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("events", "subscribe"));
    }

    public void processEvent(Event event) {
        eventListeners.forEach(l -> l.processEvent(event));
    }

    public void downloadEvents(long start, long stop, ResponseHandler responseHandler) {
        String instance = ConnectionManager.getInstance().getYamcsInstance();
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

    @Override
    public void onStudioDisconnect() {
    }
}
