/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Scalar string with alarm and timestamp.
 *
 * @author carcassi
 */
public abstract class VString extends Scalar {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract String getValue();


    /**
     * Creates a new VString.
     *
     * @param value the string value
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VString create(final String value, final Alarm alarm, final Time time) {
        return new IVString(value, alarm, time);
    }

}
