/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.vtype;

import org.diirt.vtype.VString;
import org.diirt.vtype.Time;
import org.diirt.vtype.ValueFormat;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.ValueUtil;
import org.diirt.vtype.VType;
import org.diirt.vtype.Alarm;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.WriteFunction;

/**
 * Converts the value of the argument to a VString.
 *
 * @author carcassi
 */
class VStringOfFunction implements ReadFunction<VString> {

    private final ReadFunction<? extends VType> argument;
    private final ValueFormat format;
    private final WriteFunction<VType> forward;

    public VStringOfFunction(ReadFunction<? extends VType> argument, ValueFormat format, WriteFunction<VType> forward) {
        this.argument = argument;
        this.format = format;
        this.forward = forward;
    }

    public VStringOfFunction(ReadFunction<? extends VType> argument, ValueFormat format) {
        this(argument, format, null);
    }

    @Override
    public VString readValue() {
        VType value = argument.readValue();
        if (forward != null) {
            forward.writeValue(value);
        }
        if (value == null) {
            return null;
        }
        String string = format.format(value);
        Alarm alarm = ValueUtil.alarmOf(value);
        if (alarm == null) {
            alarm = ValueFactory.alarmNone();
        }
        Time time = ValueUtil.timeOf(value);
        if (time == null) {
            time = ValueFactory.timeNow();
        }
        return ValueFactory.newVString(string, alarm, time);
    }

}
