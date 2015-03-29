package org.csstudio.platform.libs.yamcs.ws;

import io.protostuff.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.NamedObjectList;

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
    public Message<?> getRequestData() {
        NamedObjectList idList = new NamedObjectList();
        idList.setListList(new ArrayList<>(ids));
        return idList;
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
