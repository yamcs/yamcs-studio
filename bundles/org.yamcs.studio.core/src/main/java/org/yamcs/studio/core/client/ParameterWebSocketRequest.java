package org.yamcs.studio.core.client;

import java.util.HashSet;
import java.util.Set;

import org.yamcs.client.WebSocketRequest;
import org.yamcs.protobuf.ParameterSubscriptionRequest;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.NamedObjectList;

import com.google.protobuf.Message;

/**
 * Wraps a yamcs web socket request and provides a mechanism to combine multiple requests together. The purpose of this
 * is to act against request bursts that could occur when opening for example a display with many parameters on it.
 */
public class ParameterWebSocketRequest extends WebSocketRequest {

    private Set<NamedObjectId> ids = new HashSet<>();

    public ParameterWebSocketRequest(String operation, Message requestData) {
        super("parameter", operation);
        ids.addAll(((NamedObjectList) requestData).getListList());
    }

    /**
     * Override so that we can dynamically build (merged) request data
     */
    @Override
    public Message getRequestData() {
        if ("subscribe".equals(getOperation())) {
            return ParameterSubscriptionRequest.newBuilder()
                    .setAbortOnInvalid(false)
                    .setSendFromCache(true)
                    .setUpdateOnExpiration(true)
                    .addAllId(ids)
                    .build();
        } else {
            return ParameterSubscriptionRequest.newBuilder()
                    .addAllId(ids)
                    .build();
        }
    }

    public void merge(ParameterWebSocketRequest other) {
        ids.addAll(other.ids);
    }
}
