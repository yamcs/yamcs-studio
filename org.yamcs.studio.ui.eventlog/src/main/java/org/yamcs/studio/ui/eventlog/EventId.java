package org.yamcs.studio.ui.eventlog;

import java.util.Objects;

import org.yamcs.protobuf.Yamcs;

/**
 * Identity holder for Yamcs event pk.
 */
public class EventId {

    public final long generationTime;
    public final String source;
    public final int sequenceNumber;

    public EventId(long generationTime, String source, int sequenceNumber) {
        this.generationTime = generationTime;
        this.source = source;
        this.sequenceNumber = sequenceNumber;
    }

    public EventId(Yamcs.Event event) {
        this(event.getGenerationTime(), event.getSource(), event.getSeqNumber());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventId) || obj == null) {
            return false;
        }
        EventId other = (EventId) obj;
        return sequenceNumber == other.sequenceNumber
                && generationTime == other.generationTime
                && source.equals(other.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generationTime, source, sequenceNumber);
    }
}
