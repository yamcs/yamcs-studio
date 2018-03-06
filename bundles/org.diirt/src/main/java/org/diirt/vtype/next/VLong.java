/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Scalar long with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VLong extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Long getValue();

    /**
     * Creates a new VLong.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VLong create(final Long value, final Alarm alarm, final Time time, final Display display) {
        return new IVLong(value, alarm, time, display);
    }
}
