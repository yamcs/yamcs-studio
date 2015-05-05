package org.yamcs.studio.core.eventlog;

public class PastEventParams {
    boolean ok;
    long start, stop;

    public PastEventParams(long start, long stop) {
        super();
        this.start = start;
        this.stop = stop;
    }
}
