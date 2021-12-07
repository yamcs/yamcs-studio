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

public class IVStatistics extends IVNumeric implements VStatistics {

    private Double average;
    private Double stdDev;
    private Double min;
    private Double max;
    private Integer nSamples;

    public IVStatistics(Double average, Double stdDev, Double min, Double max, Integer nSamples, Alarm alarm, Time time,
            Display display) {
        super(alarm, time, display);
        this.average = average;
        this.stdDev = stdDev;
        this.min = min;
        this.max = max;
        this.nSamples = nSamples;
    }

    @Override
    public Double getAverage() {
        return average;
    }

    @Override
    public Double getStdDev() {
        return stdDev;
    }

    @Override
    public Double getMin() {
        return min;
    }

    @Override
    public Double getMax() {
        return max;
    }

    @Override
    public Integer getNSamples() {
        return nSamples;
    }
}
