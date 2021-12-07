/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newDisplay;
import static org.yamcs.studio.data.vtype.ValueFactory.newVDouble;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.yamcs.studio.data.vtype.VDouble;

/**
 * Function to simulate a signal that increases constantly within a range (saw-tooth shape). The warning limits are set
 * at 80% of the range and the alarm at 90% the range. All values are going to have no alarm status, with the timestamp
 * set at the moment the sample was generated.
 */
public class Ramp extends SimFunction<VDouble> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private double min;
    private double max;
    private double currentValue;
    private double step;
    private double range;
    private VDouble lastValue;

    /**
     * Creates a ramp shaped signal between min and max, updating a step amount every interval seconds.
     *
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param step
     *            increment for each sample
     * @param interval
     *            interval between samples in seconds
     */
    public Ramp(Double min, Double max, Double step, Double interval) {
        super(interval);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
        this.min = min;
        this.max = max;
        if (step >= 0) {
            this.currentValue = min - step;
        } else {
            this.currentValue = max - step;
        }
        this.step = step;
        range = max - min;
        lastValue = newVDouble(currentValue, alarmNone(), timeNow(), newDisplay(min, min + range * 0.1,
                min + range * 0.2, "x", DOUBLE_FORMAT, min + range * 0.8, min + range * 0.9, max, min, max));
    }

    /**
     * Creates a ramp shaped signal between min and max, incrementing 1 every interval seconds.
     *
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param interval
     *            interval between samples in seconds
     */
    public Ramp(Double min, Double max, Double interval) {
        this(min, max, 1.0, interval);
    }

    /**
     * Creates a ramp shaped signal between -5 and +5, incrementing 1 every second.
     */
    public Ramp() {
        this(-5.0, 5.0, 1.0);
    }

    @Override
    VDouble nextValue() {
        currentValue = currentValue + step;
        if (currentValue > max) {
            currentValue = min;
        }
        if (currentValue < min) {
            currentValue = max;
        }

        return newValue(currentValue, lastValue);
    }
}
