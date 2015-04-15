package org.csstudio.platform.libs.yamcs.ws;

import java.util.HashSet;
import java.util.Set;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

import com.google.protobuf.Message;

public class ParameterUnsubscribeRequest extends WebSocketRequest {

    private Set<NamedObjectId> ids = new HashSet<>();

    public ParameterUnsubscribeRequest(NamedObjectList idList) {
        ids.addAll(idList.getListList());
    }

    @Override
    public String getResource() {
        return "parameter";
    }

    @Override
    public String getOperation() {
        return "unsubscribe";
    }

    @Override
    public Message getRequestData() {
        return NamedObjectList.newBuilder().addAllList(ids).build();
    }

    @Override
    public boolean canMergeWith(WebSocketRequest otherEvent) {
        return otherEvent instanceof ParameterUnsubscribeRequest;
    }

    @Override
    public WebSocketRequest mergeWith(WebSocketRequest otherEvent) {
        Set<NamedObjectId> otherIds = ((ParameterUnsubscribeRequest) otherEvent).ids;
        ids.addAll(otherIds);
        return this;
    }
}
