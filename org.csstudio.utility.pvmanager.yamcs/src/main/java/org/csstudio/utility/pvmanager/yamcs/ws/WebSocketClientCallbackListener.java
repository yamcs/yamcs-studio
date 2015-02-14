package org.csstudio.utility.pvmanager.yamcs.ws;

import org.yamcs.protobuf.NamedObjectId;
import org.yamcs.protobuf.ParameterData;

public interface WebSocketClientCallbackListener {
    void onConnect();
    void onDisconnect();
    void onInvalidIdentification(NamedObjectId id);
    void onParameterData(ParameterData pdata);
}
