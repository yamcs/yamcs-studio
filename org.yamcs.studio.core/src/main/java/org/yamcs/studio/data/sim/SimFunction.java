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

import static org.yamcs.studio.data.vtype.ValueFactory.newVDouble;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.yamcs.studio.data.vtype.VDouble;
import org.yamcs.studio.data.vtype.VType;

/**
 * Base class for all simulated functions. It provide constant rate data generation facilities.
 */
public abstract class SimFunction<T> {

    public volatile Instant lastTime;
    private Duration timeBetweenSamples;

    /**
     * Creates a new simulation function.
     *
     * @param secondsBetweenSamples
     *            seconds between each samples
     * @param classToken
     *            simulated class
     */
    SimFunction(double secondsBetweenSamples) {
        if (secondsBetweenSamples <= 0.0) {
            throw new IllegalArgumentException(
                    "Interval must be greater than zero (was " + secondsBetweenSamples + ")");
        }

        if (secondsBetweenSamples < 0.000001) {
            throw new IllegalArgumentException("Interval must be greater than 0.000001 - no faster than 100KHz (was "
                    + secondsBetweenSamples + ")");
        }

        timeBetweenSamples = Duration.ofNanos((long) (secondsBetweenSamples * 1000000000));
    }

    /**
     * Calculates and returns the next value.
     *
     * @return the next value
     */
    abstract VType nextValue();

    /**
     * Computes all the new values in the given time slice by calling nextValue() appropriately.
     *
     * @param interval
     *            the interval where the data should be generated
     * @return the new values
     */
    public List<VType> createValues(TimeInterval interval) {
        List<VType> values = new ArrayList<>();
        Instant newTime;
        if (lastTime != null) {
            newTime = lastTime.plus(timeBetweenSamples);
        } else {
            newTime = Instant.now();
        }

        while (interval.contains(newTime)) {
            lastTime = newTime;
            values.add(nextValue());
            newTime = lastTime.plus(timeBetweenSamples);
        }

        return values;
    }

    /**
     * Creating new value based on the metadata from the old value.
     *
     * @param value
     *            new numeric value
     * @param oldValue
     *            old VDouble
     * @return new VDouble
     */
    VDouble newValue(double value, VDouble oldValue) {
        if (lastTime == null) {
            lastTime = Instant.now();
        }

        return newVDouble(value, timeNow(), oldValue);
    }

    /**
     * Returns the time between each sample.
     *
     * @return a time duration
     */
    public Duration getTimeBetweenSamples() {
        return timeBetweenSamples;
    }
}
