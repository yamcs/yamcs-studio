package org.yamcs.studio.client;

import org.yamcs.protobuf.Rest.RestParameter;

/**
 * Bundles info required by pv readers to determine the connection state
 */
public class PVConnectionInfo {

    /**
     * Whether the websocket is open or not
     */
    public boolean webSocketOpen;

    /**
     * The parameter matching the default namespace in combination with the pvname. (this
     * information comes from a rest call)
     */
    public RestParameter parameter;

    public PVConnectionInfo(boolean webSocketOpen, RestParameter parameter) {
        this.webSocketOpen = webSocketOpen;
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return String.format("[conn: %s, parameter: %s", webSocketOpen, parameter);
    }
}
