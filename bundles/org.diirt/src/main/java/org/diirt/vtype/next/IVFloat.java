/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

/**
 * Immutable VFloat implementation.
 *
 * @author carcassi
 */
class IVFloat extends VFloat {

    private final Float value;
    private final Alarm alarm;
    private final Time time;
    private final Display display;

    IVFloat(Float value, Alarm alarm, Time time, Display display) {
        this.value = value;
        this.alarm = alarm;
        this.time = time;
        this.display = display;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

}
