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
import static org.yamcs.studio.data.vtype.ValueFactory.newTime;
import static org.yamcs.studio.data.vtype.ValueFactory.newVDoubleArray;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.VDoubleArray;

/**
 * Function to simulate a waveform containing a gaussian that moves to the left.
 */
public class GaussianWaveform extends SimFunction<VDoubleArray> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private Random rand = new Random();
    private double[] buffer;
    private double periodInSeconds;
    private VDoubleArray lastValue;
    private Instant initialRefernce;

    /**
     * Creates a gaussian wave of 100 samples, with period of 1 second, standard deviation of 100 samples, updating
     * every 100ms (10Hz).
     */
    public GaussianWaveform() {
        this(1.0, 100.0, 100.0, 0.1);
    }

    /**
     * Creates a gaussian wave of given number of samples, with given period and standard, updating at the given rate
     *
     * @param periodInSeconds
     *            the period measured in seconds
     * @param stdDev
     *            standard deviation of the gaussian distribution
     * @param nSamples
     *            number of elements in the waveform
     * @param updateRateInSeconds
     *            time between samples in seconds
     */
    public GaussianWaveform(Double periodInSeconds, Double stdDev, Double nSamples, Double updateRateInSeconds) {
        super(updateRateInSeconds);
        var size = nSamples.intValue();
        this.periodInSeconds = periodInSeconds;
        buffer = new double[size];
        populateGaussian(buffer, stdDev);
    }

    static void populateGaussian(double[] array, double stdDev) {
        for (var i = 0; i < array.length; i++) {
            array[i] = gaussian(i, array.length / 2.0, stdDev);
        }
    }

    private double[] generateNewValue(double omega, double t) {
        var x = t * omega / (2 * Math.PI);
        var normalizedX = x - (double) (long) x;
        var offset = (int) (normalizedX * buffer.length);
        if (offset == buffer.length) {
            offset = 0;
        }
        var localCounter = offset;
        var newArray = new double[buffer.length];
        for (var i = 0; i < newArray.length; i++) {
            newArray[i] = buffer[localCounter];
            localCounter++;
            if (localCounter >= buffer.length) {
                localCounter -= buffer.length;
            }
        }

        return newArray;
    }

    /**
     * 1D gaussian, centered on centerX and with the specified width.
     *
     * @param x
     *            coordinate x
     * @param centerX
     *            center of the gaussian on x
     * @param width
     *            width of the gaussian in all directions
     * @return the value of the function at the given coordinates
     */
    public static double gaussian(double x, double centerX, double width) {
        return Math.exp((-Math.pow((x - centerX), 2.0)) / width);
    }

    @Override
    VDoubleArray nextValue() {
        if (lastTime == null) {
            lastTime = Instant.now();
        }
        if (initialRefernce == null) {
            initialRefernce = lastTime;
        }
        double t = initialRefernce.until(lastTime, ChronoUnit.SECONDS);
        var omega = 2 * Math.PI / periodInSeconds;
        return newVDoubleArray(new ArrayDouble(generateNewValue(omega, t)), alarmNone(), newTime(lastTime),
                newDisplay(0.0, 0.0, 0.0, "x", DOUBLE_FORMAT, 1.0, 1.0, 1.0, 0.0, 1.0));
    }
}
