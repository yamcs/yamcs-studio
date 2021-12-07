/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.formula;

/**
 * The statistics of a given set of numbers.
 * <p>
 * For the purpose of statistics calculation, NaNs should be skipped. That is, they should not appear as minimum,
 * maximum, average or stdDev, and shouldn't even be included in the count. The number of elements (including NaNs) will
 * be available from the number set used to create the statistics. This can be useful to determine whether the set
 * actually contained any valid values and therefore if there is anything to do.
 * <p>
 * The appropriate Statistics instance for an unknown set, or for a set of NaN values, is null.
 */
public abstract class Statistics {

    /**
     * The range of the values.
     *
     * @return the range
     */
    public abstract Range getRange();

    /**
     * The number of values (excluding NaN) included in the set.
     *
     * @return the number of values
     */
    public abstract int getCount();

    /**
     * The average value.
     *
     * @return the average value
     */
    public abstract double getAverage();

    /**
     * The standard deviation.
     *
     * @return the standard deviation
     */
    public abstract double getStdDev();
}
