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
import java.util.Random;

import org.yamcs.studio.data.vtype.VDouble;

/**
 * Function to simulate a signal that has a gaussian distribution. The warning limits are set above the standard
 * deviation and the alarm above two times the standard deviation. The total range is 4 times the standard deviation.
 * All values are going to have no alarm status, with the timestamp set at the moment the sample was generated.
 */
public class GaussianNoise extends SimFunction<VDouble> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private Random rand = new Random();
    private double average;
    private double stdDev;
    private VDouble lastValue;

    /**
     * Creates a signal with a normal distribution (average zero and standard deviation one), updating every 100ms
     * (10Hz).
     */
    public GaussianNoise() {
        this(0.0, 1.0, 0.1);
    }

    /**
     * Creates a signal with a gaussian distribution, updating at the rate specified.
     *
     * @param average
     *            average of the gaussian distribution
     * @param stdDev
     *            standard deviation of the gaussian distribution
     * @param interval
     *            time between samples in seconds
     */
    public GaussianNoise(Double average, Double stdDev, Double interval) {
        super(interval);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
        this.average = average;
        this.stdDev = stdDev;
        lastValue = newVDouble(average, alarmNone(), timeNow(),
                newDisplay(average - 4 * stdDev, average - 2 * stdDev, average - stdDev, "x", DOUBLE_FORMAT,
                        average + stdDev, average + 2 * stdDev, average + 4 * stdDev, average - 4 * stdDev,
                        average + 4 * stdDev));
    }

    @Override
    VDouble nextValue() {
        return newValue(average + rand.nextGaussian() * stdDev, lastValue);
    }
}
