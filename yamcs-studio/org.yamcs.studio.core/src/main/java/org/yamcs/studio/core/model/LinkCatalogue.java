package org.yamcs.studio.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Rest.EditLinkRequest;
import org.yamcs.protobuf.YamcsManagement.LinkEvent;
import org.yamcs.protobuf.YamcsManagement.LinkInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.core.web.WebSocketRegistrar;

/**
 * Provides access to aggregated state on yamcs data link information.
 * <p>
 * There should be only one long-lived instance of this class, which goes down together with the
 * application (same lifecycle as {@link YamcsPlugin}). This catalogue deals with maintaining
 * correct state accross connection-reconnects, so listeners only need to register once.
 */
public class LinkCatalogue implements Catalogue {

    private static final Logger log = Logger.getLogger(LinkCatalogue.class.getName());

    private Set<LinkListener> linkListeners = new CopyOnWriteArraySet<>();
    private Map<LinkId, LinkInfo> linksById = new ConcurrentHashMap<>();

    public static LinkCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(LinkCatalogue.class);
    }

    @Override
    public void onStudioConnect() {
        WebSocketRegistrar webSocketClient = ConnectionManager.getInstance().getWebSocketClient();
        webSocketClient.sendMessage(new WebSocketRequest("links", "subscribe"));
    }

    @Override
    public void onStudioDisconnect() {
        // Clear everything, we'll get a fresh set upon connect
        linksById.clear();
        linkListeners.forEach(l -> l.reinitializeLinkData());
    }

    public void addLinkListener(LinkListener listener) {
        linkListeners.add(listener);

        // Inform listeners of the current model
        linksById.forEach((k, v) -> listener.linkRegistered(v));
    }

    public void processLinkEvent(LinkEvent linkEvent) {
        LinkInfo incoming = linkEvent.getLinkInfo();

        LinkId id = new LinkId(incoming);
        switch (linkEvent.getType()) {
        case REGISTERED:
            linksById.put(id, incoming);
            linkListeners.forEach(l -> l.linkRegistered(incoming));
            break;
        case UNREGISTERED:
            LinkInfo linkInfo = linksById.get(id);
            if (linkInfo == null) {
                log.warning("Request to unregister unknown link " + incoming.getInstance() + "/" + incoming.getName());
            } else {
                linksById.remove(id);
                linkListeners.forEach(l -> l.linkUnregistered(incoming));
            }
            break;
        case UPDATED:
            LinkInfo oldLinkInfo = linksById.put(id, incoming);
            if (oldLinkInfo == null) {
                log.warning("Request to update unknown link " + incoming.getInstance() + "/" + incoming.getName());
            }
            linkListeners.forEach(l -> l.linkUpdated(linkEvent.getLinkInfo()));
            break;
        default:
            log.warning("Unexpected link event " + linkEvent.getType());
        }
    }

    public void enableLink(String instance, String name, ResponseHandler responseHandler) {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        EditLinkRequest req = EditLinkRequest.newBuilder().setState("enabled").build();
        restClient.patch("/links/" + instance + "/" + name, req, null, responseHandler);
    }

    public void disableLink(String instance, String name, ResponseHandler responseHandler) {
        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        EditLinkRequest req = EditLinkRequest.newBuilder().setState("disabled").build();
        restClient.patch("/links/" + instance + "/" + name, req, null, responseHandler);
    }

    public List<LinkInfo> getLinks() {
        return new ArrayList<>(linksById.values());
    }

    private static class LinkId {
        final String instance;
        final String name;

        public LinkId(LinkInfo linkInfo) {
            this.instance = linkInfo.getInstance();
            this.name = linkInfo.getName();
        }

        @Override
        public boolean equals(Object obj) {
            LinkId other = (LinkId) obj;
            return instance.equals(other.instance) && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instance, name);
        }
    }
}
