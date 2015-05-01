package org.yamcs.studio.core.archive;

import javax.swing.JPanel;

import org.yamcs.utils.TimeEncoding;

public final class DatePicker {
    //MyDatePicker start, end;

    /**
     * Returns the starting date.
     */
    public long getStartTimestamp() {
        return 0;
    }

    /**
     * Returns the ending date.
     */
    public long getEndTimestamp() {
        return TimeEncoding.currentInstant();
    }

    public TimeInterval getInterval() {
        TimeInterval ti = new TimeInterval();
        ti.setStart(getStartTimestamp());
        ti.setStop(getEndTimestamp());
        return ti;
    }

    public void setStartTimestamp(long t) {
        //start.setTime(TimeEncoding.toCalendar(t));
    }

    public void setEndTimestamp(long t) {
        //end.setTime(TimeEncoding.toCalendar(t));
    }
}
