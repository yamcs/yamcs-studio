package org.csstudio.platform.libs.yamcs.ws;

import org.yamcs.protostuff.CommandHistoryEntry;
import org.yamcs.protostuff.NamedObjectId;
import org.yamcs.protostuff.ParameterData;

public interface WebSocketClientCallbackListener {
    void onConnect();
    void onDisconnect();
    void onInvalidIdentification(NamedObjectId id);
    void onParameterData(ParameterData pdata);
    void onCommandHistoryData(CommandHistoryEntry cmdhistData);
}
