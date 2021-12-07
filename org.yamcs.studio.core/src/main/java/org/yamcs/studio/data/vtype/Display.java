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
 * Limit and unit information needed for display and control.
 * <p>
 * The numeric limits are given in double precision no matter which numeric type. The unit is a simple String, which can
 * be empty if no unit information is provided. The number format can be used to convert the value to a String.
 */
public interface Display {

    /**
     * Lowest possible value to be displayed. Never null.
     */
    Double getLowerDisplayLimit();

    /**
     * Lowest possible value (included). Never null.
     */
    Double getLowerCtrlLimit();

    /**
     * Lowest value before the alarm region. Never null.
     */
    Double getLowerAlarmLimit();

    /**
     * Lowest value before the warning region. Never null.
     */
    Double getLowerWarningLimit();

    /**
     * String representation of the units using for all values. Never null. If not available, returns the empty String.
     */
    String getUnits();

    /**
     * Returns a NumberFormat that creates a String with just the value (no units). Format is locale independent and
     * should be used for all values (values and lower/upper limits). Never null.
     *
     * @return the default format for all values
     */
    NumberFormat getFormat();

    /**
     * Highest value before the warning region. Never null.
     */
    Double getUpperWarningLimit();

    /**
     * Highest value before the alarm region. Never null.
     */
    Double getUpperAlarmLimit();

    /**
     * Highest possible value (included). Never null.
     */
    Double getUpperCtrlLimit();

    /**
     * Highest possible value to be displayed. Never null.
     */
    Double getUpperDisplayLimit();
}
