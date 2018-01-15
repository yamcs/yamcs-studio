/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import org.diirt.util.array.ListDouble;
import org.diirt.util.array.ListInt;

/**
 * Immutable VDoubleArray implementation.
 *
 * @author carcassi
 */
class IVDoubleArray extends VDoubleArray {

    private final ListDouble data;
    private final ListInt sizes;
    private final Alarm alarm;
    private final Time time;
    private final Display display;

    IVDoubleArray(ListDouble data, ListInt sizes, Alarm alarm, Time time, Display display) {
        this.data = data;
        this.alarm = alarm;
        this.time = time;
        this.display = display;
        this.sizes = sizes;
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListDouble getData() {
        return data;
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
