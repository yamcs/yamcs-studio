package org.yamcs.studio.core.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Archive.StreamData;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.WebSocketRegistrar;

public class StreamCatalogue implements Catalogue {

    private Set<StreamListener> streamListeners = new CopyOnWriteArraySet<>();

    public static StreamCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(StreamCatalogue.class);
    }

    public void addStreamListener(StreamListener listener) {
        streamListeners.add(listener);
    }

    public void removeStreamListener(StreamListener listener) {
        streamListeners.remove(listener);
    }

    @Override
    public void onStudioConnect() {
        // Install websocket
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("esim", "subscribe"));
    }

    @Override
    public void onStudioDisconnect() {

    }

    public void processStreamData(StreamData data) {
        streamListeners.forEach(l -> l.processStreamData(data));
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {

    }
}
