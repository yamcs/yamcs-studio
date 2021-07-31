package org.yamcs.studio.archive;

import java.time.OffsetDateTime;

public class ViewportChangeEvent {

    private OffsetDateTime start;
    private OffsetDateTime stop;

    public ViewportChangeEvent(OffsetDateTime start, OffsetDateTime stop) {
        this.start = start;
        this.stop = stop;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getStop() {
        return stop;
    }
}
