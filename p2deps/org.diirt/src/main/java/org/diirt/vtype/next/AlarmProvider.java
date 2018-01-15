/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * A value that provides an alarm.
 *
 * @author carcassi
 */
interface AlarmProvider {

    /**
     * The alarm associated with this value.
     *
     * @return the alarm; not null
     */
    public abstract Alarm getAlarm();
}
