package org.yamcs.studio.core.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.WebSocketRegistrar;

public class EventCatalogue implements Catalogue {

    private Set<EventListener> eventListeners = new CopyOnWriteArraySet<>();

    public static EventCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(EventCatalogue.class);
    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, WebSocketRegistrar webSocketClient) {
        webSocketClient.sendMessage(new WebSocketRequest("events", "subscribe"));
    }

    public void processEvent(Event event) {
        eventListeners.forEach(l -> l.processEvent(event));
    }

    @Override
    public void onStudioDisconnect() {
    }
}
