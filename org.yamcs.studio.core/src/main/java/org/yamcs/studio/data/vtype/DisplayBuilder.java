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

import java.text.NumberFormat;

/**
 * Allows to build a Display object which can then be re-used to generate values with the same metadata.
 */
public class DisplayBuilder {

    private Double lowerDisplayLimit = Double.NaN;
    private Double lowerCtrlLimit = Double.NaN;
    private Double lowerAlarmLimit = Double.NaN;
    private Double lowerWarningLimit = Double.NaN;
    private String units = "";
    private NumberFormat format = NumberFormats.TO_STRING_FORMAT;
    private Double upperWarningLimit = Double.NaN;
    private Double upperAlarmLimit = Double.NaN;
    private Double upperCtrlLimit = Double.NaN;
    private Double upperDisplayLimit = Double.NaN;

    DisplayBuilder(Double lowerDisplayLimit, Double lowerCtrlLimit, Double lowerAlarmLimit, Double lowerWarningLimit,
            String units, NumberFormat format, Double upperWarningLimit, Double upperAlarmLimit, Double upperCtrlLimit,
            Double upperDisplayLimit) {
        this.lowerDisplayLimit = lowerDisplayLimit;
        this.lowerCtrlLimit = lowerCtrlLimit;
        this.lowerAlarmLimit = lowerAlarmLimit;
        this.lowerWarningLimit = lowerWarningLimit;
        this.units = units;
        this.format = format;
        this.upperWarningLimit = upperWarningLimit;
        this.upperAlarmLimit = upperAlarmLimit;
        this.upperCtrlLimit = upperCtrlLimit;
        this.upperDisplayLimit = upperDisplayLimit;
    }

    public DisplayBuilder() {
    }

    public DisplayBuilder lowerDisplayLimit(Double lowerDisplayLimit) {
        this.lowerDisplayLimit = lowerDisplayLimit;
        return this;
    }

    public DisplayBuilder lowerCtrlLimit(Double lowerCtrlLimit) {
        this.lowerCtrlLimit = lowerCtrlLimit;
        return this;
    }

    public DisplayBuilder lowerAlarmLimit(Double lowerAlarmLimit) {
        this.lowerAlarmLimit = lowerAlarmLimit;
        return this;
    }

    public DisplayBuilder lowerWarningLimit(Double lowerWarningLimit) {
        this.lowerWarningLimit = lowerWarningLimit;
        return this;
    }

    public DisplayBuilder upperWarningLimit(Double upperWarningLimit) {
        this.upperWarningLimit = upperWarningLimit;
        return this;
    }

    public DisplayBuilder upperAlarmLimit(Double upperAlarmLimit) {
        this.upperAlarmLimit = upperAlarmLimit;
        return this;
    }

    public DisplayBuilder upperCtrlLimit(Double upperCtrlLimit) {
        this.upperCtrlLimit = upperCtrlLimit;
        return this;
    }

    public DisplayBuilder upperDisplayLimit(Double upperDisplayLimit) {
        this.upperDisplayLimit = upperDisplayLimit;
        return this;
    }

    public DisplayBuilder units(String units) {
        this.units = units;
        return this;
    }

    public DisplayBuilder format(NumberFormat format) {
        this.format = format;
        return this;
    }

    public Display build() {
        return ValueFactory.newDisplay(lowerDisplayLimit, lowerAlarmLimit, lowerWarningLimit, units, format,
                upperWarningLimit, upperAlarmLimit, upperDisplayLimit, lowerCtrlLimit, upperCtrlLimit);
    }
}
