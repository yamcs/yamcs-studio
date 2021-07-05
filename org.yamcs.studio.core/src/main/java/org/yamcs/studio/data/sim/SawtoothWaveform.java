package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newDisplay;
import static org.yamcs.studio.data.vtype.ValueFactory.newTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.VDoubleArray;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Function to simulate a waveform containing a triangle wave.
 */
public class SawtoothWaveform extends SimFunction<VDoubleArray> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private double periodInSeconds;
    private double wavelengthInSamples;
    private int nSamples;
    private Instant initialRefernce;

    /**
     * Creates a triangle wave of 100 samples, with period of 1 second, wavelength of 100 samples, updating at 10 Hz.
     */
    public SawtoothWaveform() {
        this(1.0, 100.0, 0.1);
    }

    /**
     * Creates a triangle wave of 100 samples, with given period and given wavelength of 100 samples, updating at given
     * rate.
     *
     * @param periodInSeconds
     *            the period measured in seconds
     * @param wavelengthInSamples
     *            the wavelength measured in samples
     * @param updateRateInSeconds
     *            the update rate in seconds
     */
    public SawtoothWaveform(Double periodInSeconds, Double wavelengthInSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 100.0, updateRateInSeconds);
    }

    /**
     * Creates a triangle wave of 100 samples, with given period and given wavelength of given number of samples,
     * updating at given rate.
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
    public SawtoothWaveform(Double periodInSeconds, Double wavelengthInSamples, Double nSamples,
            Double updateRateInSeconds) {
        super(updateRateInSeconds);
        this.periodInSeconds = periodInSeconds;
        this.wavelengthInSamples = wavelengthInSamples;
        this.nSamples = nSamples.intValue();
        if (this.nSamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    private ListDouble generateNewValue(final double omega, final double t, double k) {
        double[] newArray = new double[nSamples];
        for (int i = 0; i < newArray.length; i++) {
            double x = (omega * t + k * i) / (2 * Math.PI);
            double normalizedPositionInPeriod = x - (double) (long) x;
            newArray[i] = -1.0 + 2 * normalizedPositionInPeriod;
        }
        return new ArrayDouble(newArray);
    }

    @Override
    VDoubleArray nextValue() {
        if (initialRefernce == null) {
            initialRefernce = lastTime;
        }
        double t = initialRefernce.until(lastTime, ChronoUnit.SECONDS);
        double omega = 2 * Math.PI / periodInSeconds;
        double k = 2 * Math.PI / wavelengthInSamples;
        double min = 1.0;
        double max = -1.0;
        double range = 0.0;
        return ValueFactory.newVDoubleArray(generateNewValue(omega, t, k), alarmNone(),
                newTime(lastTime), newDisplay(min, min + range * 0.1, min + range * 0.2, "", DOUBLE_FORMAT,
                        min + range * 0.8, min + range * 0.9, max, min, max));
    }
}
