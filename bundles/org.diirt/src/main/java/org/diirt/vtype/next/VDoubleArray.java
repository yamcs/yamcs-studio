/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import org.diirt.util.array.ListDouble;

/**
 * Scalar double array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VDoubleArray extends VNumberArray {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListDouble getData();

    /**
     * Creates a new VDouble.
     *
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VDoubleArray create(final ListDouble data, final Alarm alarm, final Time time, final Display display) {
        return new IVDoubleArray(data, null, alarm, time, display);
    }
}
