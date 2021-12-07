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
 * Function to simulate a signal shaped like a sine. The warning limits are set at 80% of the range and the alarm at 90%
 * the range. All values are going to have no alarm status, with the timestamp set at the moment the sample was
 * generated.
 */
public class Sine extends SimFunction<VDouble> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private double min;
    private double max;
    private long currentValue;
    private double samplesPerCycle;
    private double range;
    private VDouble lastValue;

    /**
     * Creates a sine shaped signal between min and max, updating every interval seconds with samplesPerCycles samples
     * every full sine cycle.
     *
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param samplesPerCycle
     *            number of samples for each full cycle (each 2 Pi)
     * @param secondsBetweenSamples
     *            interval between samples in seconds
     */
    public Sine(Double min, Double max, Double samplesPerCycle, Double secondsBetweenSamples) {
        super(secondsBetweenSamples);
        this.min = min;
        this.max = max;
        this.currentValue = 0;
        this.samplesPerCycle = samplesPerCycle;
        range = this.max - this.min;
        lastValue = newVDouble(0.0, alarmNone(), timeNow(), newDisplay(min, min + range * 0.1, min + range * 0.2, "x",
                DOUBLE_FORMAT, min + range * 0.8, min + range * 0.9, max, min, max));
    }

    /**
     * Creates a sine shaped signal between min and max, updating every interval seconds with 10 samples every full sine
     * cycle.
     *
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     * @param secondsBeetwenSamples
     *            interval between samples in seconds
     */
    public Sine(Double min, Double max, Double secondsBeetwenSamples) {
        this(min, max, 10.0, secondsBeetwenSamples);
    }

    /**
     * Creates a sine shaped signal between -5 and 5, updating every second with 10 samples every full sine cycle.
     */
    public Sine() {
        this(-5.0, 5.0, 1.0);
    }

    @Override
    VDouble nextValue() {
        var value = Math.sin(currentValue * 2 * Math.PI / samplesPerCycle) * range / 2 + min + (range / 2);
        currentValue++;

        return newValue(value, lastValue);
    }
}
