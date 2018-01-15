/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Scalar byte with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VByte extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Byte getValue();

    /**
     * Creates a new VByte.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VByte create(final Byte value, final Alarm alarm, final Time time, final Display display) {
        return new IVByte(value, alarm, time, display);
    }
}
