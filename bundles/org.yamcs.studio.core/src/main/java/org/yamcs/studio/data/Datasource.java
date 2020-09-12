package org.yamcs.studio.data;

import org.yamcs.studio.data.vtype.VType;

public interface Datasource {

    boolean supportsPVName(String pvName);

    boolean isConnected(IPV pv);

    boolean isWriteAllowed(IPV pv);

    VType getValue(String pvName);

    void writeValue(IPV pv, Object value, WriteCallback callback);

    void onStarted(IPV pv);

    void onStopped(IPV pv);
}
