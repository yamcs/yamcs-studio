package org.yamcs.studio.core;

import org.yamcs.api.YamcsConnectionProperties;

public class ConnectionInfo {

    private YamcsConnectionProperties primaryConnection;
    private YamcsConnectionProperties failoverConnection;

    public ConnectionInfo(YamcsConnectionProperties primaryConnection, YamcsConnectionProperties failoverConnection) {
        this.primaryConnection = primaryConnection;
        this.failoverConnection = failoverConnection;
    }

    public YamcsConnectionProperties getConnection(ConnectionMode mode) {
        return (mode == ConnectionMode.PRIMARY) ? primaryConnection : failoverConnection;
    }
}
