package org.csstudio.platform.libs.yamcs.ws;

import org.yamcs.api.ws.WebSocketRequest;

public class CommandHistorySubscribeAllRequest extends WebSocketRequest {

    @Override
    public String getResource() {
        return "cmdhistory";
    }

    @Override
    public String getOperation() {
        return "subscribe";
    }
}
