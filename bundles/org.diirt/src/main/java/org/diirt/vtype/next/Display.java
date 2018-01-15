/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.text.NumberFormat;
import java.util.Objects;

import org.diirt.util.NumberFormats;
import org.diirt.util.Range;
import org.diirt.util.Ranges;

/**
 * Limit and unit information needed for display and control.
 * <p>
 * The numeric limits are given in double precision no matter which numeric
 * type. The unit is a simple String, which can be empty if no unit information
 * is provided. The number format can be used to convert the value to a String.
 *
 * @author carcassi
 */
public abstract class Display {

    /**
     * The range for the value when displayed.
     *
     * @return the display range; can be null
     */
    public abstract Range getDisplayRange();

    /**
     * The range for the alarm associated to the value.
     *
     * @return the alarm range; can be null
     */
    public abstract Range getAlarmRange();

    /**
     * The range for the warning associated to the value.
     *
     * @return the warning range; can be null
     */
    public abstract Range getWarningRange();

    /**
     * The range used for changing the value.
     *
     * @return the control range; can be null
     */
    public abstract Range getControlRange();

    /**
     * String representation of the unit using for all values.
     * Never null. If not available, returns the empty String.
     *
     * @return unit
     */
    public abstract String getUnit();

    /**
     * Returns a NumberFormat that creates a String with just the value (no units).
     * Format is locale independent and should be used for all values (values and
     * min/max of the ranges). Never null.
     *
     * @return the default format for all values
     */
    public abstract NumberFormat getFormat();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Display) {
            Display other = (Display) obj;

            return Objects.equals(getFormat(), other.getFormat()) &&
                Objects.equals(getUnit(), other.getUnit()) &&
                Objects.equals(getDisplayRange(), other.getDisplayRange()) &&
                Objects.equals(getAlarmRange(), other.getAlarmRange()) &&
                Objects.equals(getWarningRange(), other.getWarningRange()) &&
                Objects.equals(getControlRange(), other.getControlRange());
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(getFormat());
        hash = 59 * hash + Objects.hashCode(getUnit());
        hash = 59 * hash + Objects.hashCode(getDisplayRange());
        hash = 59 * hash + Objects.hashCode(getAlarmRange());
        hash = 59 * hash + Objects.hashCode(getWarningRange());
        hash = 59 * hash + Objects.hashCode(getControlRange());
        return hash;
    }

    /**
     * Creates a new display
     *
     * @param lowerDisplayLimit lower display limit
     * @param lowerAlarmLimit lower alarm limit
     * @param lowerWarningLimit lower warning limit
     * @param units the units
     * @param numberFormat the formatter
     * @param upperWarningLimit the upper warning limit
     * @param upperAlarmLimit the upper alarm limit
     * @param upperDisplayLimit the upper display limit
     * @param lowerCtrlLimit the lower control limit
     * @param upperCtrlLimit the upper control limit
     * @return the new display
     */
    public static Display create(final Double lowerDisplayLimit, final Double lowerAlarmLimit, final Double lowerWarningLimit,
            final String units, final NumberFormat numberFormat, final Double upperWarningLimit,
            final Double upperAlarmLimit, final Double upperDisplayLimit,
            final Double lowerCtrlLimit, final Double upperCtrlLimit) {
        return new IDisplay(Ranges.range(lowerDisplayLimit, upperDisplayLimit),
                Ranges.range(lowerWarningLimit, upperWarningLimit),
                Ranges.range(lowerAlarmLimit, upperAlarmLimit),
                Ranges.range(lowerCtrlLimit, upperCtrlLimit), units, numberFormat);
    }

    private static final Display displayNone = create(Double.NaN, Double.NaN,
            Double.NaN, "", NumberFormats.toStringFormat(), Double.NaN, Double.NaN,
            Double.NaN, Double.NaN, Double.NaN);

    /**
     * Empty display information.
     *
     * @return no display
     */
    public static Display none() {
        return displayNone;
    }
}
