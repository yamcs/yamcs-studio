package org.csstudio.platform.libs.yamcs.ws;

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
