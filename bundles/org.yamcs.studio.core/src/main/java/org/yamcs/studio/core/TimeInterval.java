package org.yamcs.studio.core;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Time interval where both ends can be open
 */
public class TimeInterval implements Serializable {
    private static final long serialVersionUID = 1L;

    private Instant start;
    private Instant stop;

    public TimeInterval(Instant start, Instant stop) {
        setStart(start);
        setStop(stop);
    }

    /**
     * Creates a TimeInterval with no ends
     */
    public TimeInterval() {
    }

    public static TimeInterval starting(Instant start) {
        TimeInterval range = new TimeInterval();
        range.setStart(start);
        return range;
    }

    public boolean hasStart() {
        return start != null;
    }

    public boolean hasStop() {
        return stop != null;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getStart() {
        return start;
    }

    public void setStop(Instant stop) {
        this.stop = stop;
    }

    public Instant getStop() {
        return stop;
    }

    public Instant calculateStart() {
        if (start != null) {
            return start;
        } else {
            return YamcsPlugin.getMissionTime(true)
                    .truncatedTo(ChronoUnit.DAYS);
        }
    }

    public Instant calculateStop() {
        if (stop != null) {
            return stop;
        } else {
            return YamcsPlugin.getMissionTime(true)
                    .plus(1, ChronoUnit.MONTHS)
                    .truncatedTo(ChronoUnit.DAYS);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeInterval)) {
            return false;
        }
        TimeInterval other = (TimeInterval) obj;
        return Objects.equals(start, other.start) && Objects.equals(stop, other.stop);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (start != null) {
            sb.append(start);
        }
        sb.append(",");
        if (stop != null) {
            sb.append(stop);
        }
        sb.append(")");
        return sb.toString();
    }
}
