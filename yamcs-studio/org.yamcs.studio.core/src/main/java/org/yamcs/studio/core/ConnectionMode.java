package org.yamcs.studio.core;

public enum ConnectionMode {

    PRIMARY("Primary"),
    FAILOVER("Failover");

    private String prettyName;

    private ConnectionMode(String prettyName) {
        this.prettyName = prettyName;
    }

    public String getPrettyName() {
        return prettyName;
    }
}
