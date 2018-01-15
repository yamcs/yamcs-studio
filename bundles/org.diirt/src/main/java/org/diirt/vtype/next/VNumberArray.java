/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import org.diirt.util.array.ListDouble;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.SimpleValueFormat;
import org.diirt.vtype.ValueFormat;

/**
 * Numeric array with alarm, timestamp, display and control information.
 * <p>
 * This class allows to use any numeric array (i.e. {@link VIntArray} or
 * {@link VDoubleArray}) through the same interface.
 *
 * @author carcassi
 */
public abstract class VNumberArray extends Array implements AlarmProvider, TimeProvider, DisplayProvider {

    /**
     * The numeric value.
     *
     * @return the value
     */
    @Override
    public abstract ListNumber getData();

    private final static ValueFormat format = new SimpleValueFormat(3);

    /**
     * Default toString implementation for VNumberArray.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Class type = typeOf(this);
        builder.append(type.getSimpleName())
                .append("[");
        builder.append(format.format(this));
        builder.append(", size ")
                .append(getData().size())
                .append(" ,")
                .append(getAlarm())
                .append(", ")
                .append(getTime())
                .append(']');
        return builder.toString();
    }

    /**
     * Creates a new VNumber based on the type of the data
     *
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new number
     */
    public static VNumberArray create(ListNumber data, Alarm alarm, Time time, Display display){
        if (data instanceof ListDouble) {
            return VDoubleArray.create((ListDouble) data, alarm, time, display);
//        } else if (value instanceof Float) {
//            return newVFloat((Float) value, alarm, time, display);
//        } else if (value instanceof Long) {
//            return newVLong((Long) value, alarm, time, display);
//        } else if (value instanceof Integer) {
//            return VInt.create((Integer) value, alarm, time, display);
//        } else if (value instanceof Short) {
//            return newVShort((Short) value, alarm, time, display);
//        } else if (value instanceof Byte) {
//            return newVByte((Byte) value, alarm, time, display);
        }
        throw new UnsupportedOperationException();
    }

}
