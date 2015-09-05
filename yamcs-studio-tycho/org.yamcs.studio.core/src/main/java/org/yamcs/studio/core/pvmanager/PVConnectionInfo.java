package org.yamcs.studio.core.pvmanager;

import org.yamcs.protobuf.Rest.RestParameter;
import org.yamcs.studio.core.ConnectionManager;

/**
 * Bundles info required by pv readers to determine the connection state
 */
public class PVConnectionInfo {

    public boolean connected;

    /**
     * The parameter matching the default namespace in combination with the pvname. (this
     * information comes from a rest call)
     */
    public RestParameter parameter;

    public PVConnectionInfo(RestParameter parameter) {
        this.connected = ConnectionManager.getInstance().isConnected();
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return String.format("[conn: %s, parameter: %s", connected, parameter);
    }
}
