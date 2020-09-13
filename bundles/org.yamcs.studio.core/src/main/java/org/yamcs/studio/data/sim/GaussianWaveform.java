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
    private final double periodInSeconds;
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
        super(updateRateInSeconds, VDoubleArray.class);
        int size = nSamples.intValue();
        this.periodInSeconds = periodInSeconds;
        buffer = new double[size];
        populateGaussian(buffer, stdDev);
    }

    static void populateGaussian(double[] array, double stdDev) {
        for (int i = 0; i < array.length; i++) {
            array[i] = gaussian(i, array.length / 2.0, stdDev);
        }
    }

    private double[] generateNewValue(double omega, double t) {
        double x = t * omega / (2 * Math.PI);
        double normalizedX = x - (double) (long) x;
        int offset = (int) (normalizedX * buffer.length);
        if (offset == buffer.length) {
            offset = 0;
        }
        int localCounter = offset;
        double[] newArray = new double[buffer.length];
        for (int i = 0; i < newArray.length; i++) {
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
        double omega = 2 * Math.PI / periodInSeconds;
        return newVDoubleArray(new ArrayDouble(generateNewValue(omega, t)), alarmNone(),
                newTime(lastTime), newDisplay(0.0, 0.0, 0.0, "x", DOUBLE_FORMAT,
                        1.0, 1.0, 1.0, 0.0, 1.0));
    }
}
