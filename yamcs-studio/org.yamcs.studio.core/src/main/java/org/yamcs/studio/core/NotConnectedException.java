package org.yamcs.studio.core;

public class NotConnectedException extends Exception {

    private static final long serialVersionUID = 1L;

    public NotConnectedException() {
        super("Not connected to Yamcs");
    }
}
