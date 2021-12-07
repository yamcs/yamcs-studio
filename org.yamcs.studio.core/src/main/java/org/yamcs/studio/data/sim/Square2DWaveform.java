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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.VDoubleArray;
import org.yamcs.studio.data.vtype.ValueFactory;
import org.yamcs.studio.data.vtype.ValueUtil;

/**
 * Function to simulate a 2D waveform containing a sine wave.
 */
public class Square2DWaveform extends SimFunction<VDoubleArray> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private double periodInSeconds;
    private double wavelengthInSamples;
    private final int xSamples;
    private final int ySamples;
    private double angle;
    private Instant initialReference;

    /**
     * Creates sine wave of 100 samples, with period of 1 second, wavelength of 100 samples along the x axis, updating
     * at 10 Hz.
     */
    public Square2DWaveform() {
        this(1.0, 100.0, 0.1);
    }

    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of 100 samples along the x axis,
     * updating at given rate.
     *
     * @param periodInSeconds
     *            the period measured in seconds
     * @param wavelengthInSamples
     *            the wavelength measured in samples
     * @param updateRateInSeconds
     *            the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 100.0, updateRateInSeconds);
    }

    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of given number of samples along the x
     * axis, updating at given rate.
     *
     * @param periodInSeconds
     *            the period measured in seconds
     * @param wavelengthInSamples
     *            the wavelength measured in samples
     * @param nSamples
     *            the number of samples
     * @param updateRateInSeconds
     *            the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double nSamples,
            Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 0.0, nSamples, nSamples, updateRateInSeconds);
    }

    /**
     * Creates sine wave with given parameters.
     *
     * @param periodInSeconds
     *            the period measured in seconds
     * @param wavelengthInSamples
     *            the wavelength measured in samples
     * @param angle
     *            the direction of propagation for the wave
     * @param xSamples
     *            number of samples on the x direction
     * @param ySamples
     *            number of samples on the y direction
     * @param updateRateInSeconds
     *            the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double angle, Double xSamples,
            Double ySamples, Double updateRateInSeconds) {
        super(updateRateInSeconds);
        this.periodInSeconds = periodInSeconds;
        this.wavelengthInSamples = wavelengthInSamples;
        this.xSamples = xSamples.intValue();
        this.ySamples = ySamples.intValue();
        this.angle = angle;
        if (this.xSamples <= 0 || this.ySamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    private ListDouble generateNewValue(double omega, double t, double k) {
        var kx = Math.cos(angle * Math.PI / 180.0) * k;
        var ky = Math.sin(angle * Math.PI / 180.0) * k;
        return new ListDouble() {

            @Override
            public double getDouble(int index) {
                var x = index % xSamples;
                var y = index / xSamples;
                var length = (omega * t + kx * x + ky * y) / (2 * Math.PI);
                var normalizedPositionInPeriod = length - (double) (long) length;
                if (normalizedPositionInPeriod < 0.5) {
                    return 1.0;
                } else if (normalizedPositionInPeriod < 1.0) {
                    return -1.0;
                } else {
                    return 1.0;
                }
            }

            @Override
            public int size() {
                return xSamples * ySamples;
            }
        };
    }

    @Override
    VDoubleArray nextValue() {
        if (initialReference == null) {
            initialReference = lastTime;
        }
        double t = initialReference.until(lastTime, ChronoUnit.SECONDS);
        var omega = 2 * Math.PI / periodInSeconds;
        var k = 2 * Math.PI / wavelengthInSamples;
        var min = -1.0;
        var max = 1.0;
        var range = 0.0;
        return (VDoubleArray) ValueFactory.newVNumberArray(generateNewValue(omega, t, k),
                new ArrayInt(ySamples, xSamples), ValueUtil.defaultArrayDisplay(new ArrayInt(ySamples, xSamples)),
                alarmNone(), newTime(lastTime), newDisplay(min, min + range * 0.1, min + range * 0.2, "", DOUBLE_FORMAT,
                        min + range * 0.8, min + range * 0.9, max, min, max));
    }
}
