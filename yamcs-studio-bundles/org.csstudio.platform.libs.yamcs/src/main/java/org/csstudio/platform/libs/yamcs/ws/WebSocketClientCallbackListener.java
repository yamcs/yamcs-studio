package org.csstudio.platform.libs.yamcs.ws;

import org.yamcs.protobuf.NamedObjectId;
import org.yamcs.protobuf.ParameterData;

public interface WebSocketClientCallbackListener {
    void onConnect();
    void onDisconnect();
    void onInvalidIdentification(NamedObjectId id);
    void onParameterData(ParameterData pdata);
}
