package org.yamcs.studio.ui.eventlog;

public class PastEventParams {
    boolean ok;
    long start, stop;

    public PastEventParams(long start, long stop) {
        super();
        this.start = start;
        this.stop = stop;
    }
}
