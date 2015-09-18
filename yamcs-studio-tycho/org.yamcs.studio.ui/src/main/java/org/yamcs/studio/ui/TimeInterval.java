package org.yamcs.studio.ui;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.utils.TimeEncoding;

/**
 * Time interval where both ends can be open
 */
public class TimeInterval implements Serializable {
    private static final long serialVersionUID = 1L;

    private long start;
    private long stop;
    private boolean hasStart = false;
    private boolean hasStop = false;

    public TimeInterval(long start, long stop) {
        setStart(start);
        setStop(stop);
    }

    /**
     * Creates a TimeInterval with no ends
     */
    public TimeInterval() {
    }

    public static TimeInterval starting(long start) {
        TimeInterval range = new TimeInterval();
        range.setStart(start);
        return range;
    }

    public boolean hasStart() {
        return hasStart;
    }

    public boolean hasStop() {
        return hasStop;
    }

    public void setStart(long start) {
        hasStart = true;
        this.start = start;
    }

    public long getStart() {
        return start;
    }

    public void setStop(long stop) {
        hasStop = true;
        this.stop = stop;
    }

    public long getStop() {
        return stop;
    }

    public long calculateStart() {
        if (hasStart)
            return start;
        else {
            Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return TimeEncoding.fromCalendar(cal);
        }
    }

    public long calculateStop() {
        if (hasStop)
            return stop;
        else {
            Calendar cal = TimeCatalogue.getInstance().getMissionTimeAsCalendar(true);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return TimeEncoding.fromCalendar(cal);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeInterval))
            return false;
        TimeInterval other = (TimeInterval) obj;
        return Objects.equals(start, other.start)
                && Objects.equals(stop, other.stop)
                && Objects.equals(hasStart, other.hasStart)
                && Objects.equals(hasStop, other.hasStop);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (hasStart)
            sb.append(start);
        sb.append(",");
        if (hasStop)
            sb.append(stop);
        sb.append(")");
        return sb.toString();
    }
}
