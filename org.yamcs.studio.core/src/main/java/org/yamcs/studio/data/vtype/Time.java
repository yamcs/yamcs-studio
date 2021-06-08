package org.yamcs.studio.data.vtype;

import java.time.Instant;

/**
 * Time information.
 */
public interface Time {

    /**
     * The time instant of the value, typically indicating when it was generated. If never connected, it returns the
     * time when it was last determined that no connection was made.
     */
    Instant getTimestamp();

    /**
     * Returns a user defined tag, that can be used to store extra time information, such as beam shot.
     */
    Integer getTimeUserTag();

    /**
     * Returns a data source specific flag to indicate whether the time information should be trusted. Typical cases are
     * when records were not processes and the timestamp has a zero time.
     *
     * @return true if the time information is valid
     */
    boolean isTimeValid();
}
