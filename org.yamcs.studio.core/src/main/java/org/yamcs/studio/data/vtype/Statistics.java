/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

/**
 * Basic type for statistical information of numeric types. The methods never return null, even if no connection was
 * ever made. One <b>must always look</b> at the alarm severity to be able to correctly interpret the value.
 * <p>
 * This type can be used regardless of the method used to calculate the average (instances:
 * &Sigma;<i>x<sub>i</sub>/N</i>, time: &Sigma;<i>x<sub>i</sub>&Delta;t<sub>i</sub>/&Delta;t</i>, time with linear
 * interpolation, exponential backoff, ...).
 * <p>
 * No integer statistics, since averages are not integer in general.
 */
public interface Statistics {

    /**
     * The average. Never null.
     *
     * @return the average
     */
    Double getAverage();

    /**
     * The standard deviation. Never null.
     *
     * @return the standard deviation
     */
    Double getStdDev();

    /**
     * The minimum value.
     *
     * @return the minimum
     */
    Double getMin();

    /**
     * The maximum value.
     *
     * @return the maximum
     */
    Double getMax();

    /**
     * The number of samples.
     *
     * @return the number of samples
     */
    Integer getNSamples();
}
