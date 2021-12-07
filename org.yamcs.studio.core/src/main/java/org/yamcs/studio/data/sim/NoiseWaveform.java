package org.yamcs.studio.data.sim;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newDisplay;
import static org.yamcs.studio.data.vtype.ValueFactory.newTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Random;

import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.VDoubleArray;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Function to simulate a waveform containing a uniformly distributed random data.
 */
public class NoiseWaveform extends SimFunction<VDoubleArray> {

    private static final NumberFormat DOUBLE_FORMAT = new DecimalFormat();

    private Random rand = new Random();
    private double min;
    private double max;
    private int nSamples;
    private double range;
    private VDoubleArray lastValue;

    /**
     * Creates a waveform with samples from a uniform distribution from -5 to 5, updating every second.
     */
    public NoiseWaveform() {
        this(-5.0, 5.0, 1.0);
    }

    /**
     * Creates a gaussian waveform signal with a gaussian distribution, updating at the rate specified.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param interval
     *            time between samples in seconds
     */
    public NoiseWaveform(Double min, Double max, Double interval) {
        this(min, max, 100.0, interval);
    }

    /**
     * Creates a gaussian waveform signal with a gaussian distribution, updating at the rate specified.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param nSamples
     *            number of elements in the waveform
     * @param interval
     *            time between samples in seconds
     */
    public NoiseWaveform(Double min, Double max, Double nSamples, Double interval) {
        super(interval);
        this.min = min;
        this.max = max;
        range = this.max - this.min;
        this.nSamples = nSamples.intValue();
        if (this.nSamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    private double[] generateNewValue() {
        var newArray = new double[nSamples];
        for (var i = 0; i < newArray.length; i++) {
            newArray[i] = min + rand.nextDouble() * (max - min);
        }
        return newArray;
    }

    @Override
    VDoubleArray nextValue() {
        if (lastTime == null) {
            lastTime = Instant.now();
        }
        return ValueFactory.newVDoubleArray(new ArrayDouble(generateNewValue()), alarmNone(), newTime(lastTime),
                newDisplay(min, min + range * 0.1, min + range * 0.2, "x", DOUBLE_FORMAT, min + range * 0.8,
                        min + range * 0.9, max, min, max));
    }
}
