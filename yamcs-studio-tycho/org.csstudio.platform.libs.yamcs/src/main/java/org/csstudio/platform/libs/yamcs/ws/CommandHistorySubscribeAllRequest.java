package org.csstudio.platform.libs.yamcs.ws;


public class CommandHistorySubscribeAllRequest extends WebSocketRequest {

    @Override
    public String getRequestType() {
        return "cmdhistory";
    }

    @Override
    public String getRequestName() {
        return "subscribe";
    }
}
