package org.yamcs.studio.core.web;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.MessageLite;

public abstract class BulkResponseHandler<T extends MessageLite> implements ResponseHandler {

    private int bulkSize;
    private List<T> messages = new ArrayList<>();

    public BulkResponseHandler() {
        this(500);
    }

    public BulkResponseHandler(int bulkSize) {
        this.bulkSize = bulkSize;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(MessageLite responseMsg) {
        if (responseMsg != null) {
            messages.add((T) responseMsg);
            if (messages.size() == bulkSize) {
                List<T> chunk = new ArrayList<>(messages);
                onMessages(chunk);
                messages.clear();
            }
        }
        if (responseMsg == null) {
            if (!messages.isEmpty())
                onMessages(messages);
            onEndOfStream();
        }
    }

    public abstract void onMessages(List<T> messages);

    public abstract void onEndOfStream();
}
