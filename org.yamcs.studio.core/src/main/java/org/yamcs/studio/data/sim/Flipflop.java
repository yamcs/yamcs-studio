package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVBoolean;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import org.yamcs.studio.data.vtype.VBoolean;

/**
 * Function to simulate a boolean signal that turns on and off.
 */
public class Flipflop extends SimFunction<VBoolean> {

    private boolean value = true;

    /**
     * Creates a flipflop that changes every 500 ms.
     */
    public Flipflop() {
        this(0.5);
    }

    /**
     * Creates a signal that turns on and off every interval.
     *
     * @param interval
     *            interval between samples in seconds
     */
    public Flipflop(Double interval) {
        super(interval);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
    }

    @Override
    VBoolean nextValue() {
        value = !value;
        return newVBoolean(value, alarmNone(), timeNow());
    }
}
