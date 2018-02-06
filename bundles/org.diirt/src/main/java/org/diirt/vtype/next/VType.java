/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.diirt.util.array.ListNumber;
import org.diirt.util.array.ListNumbers;

/**
 * Tag interface to mark all the members of the value classes.
 *
 * @author carcassi
 */
public abstract class VType {

//    private static Collection<Class<?>> types = Arrays.<Class<?>>asList(VByte.class, VByteArray.class, VDouble.class,
//            VDoubleArray.class, VEnum.class, VEnumArray.class, VFloat.class, VFloatArray.class,
//            VLong.class, VLongArray.class, VInt.class, VIntArray.class, VMultiDouble.class, VMultiEnum.class,
//            VMultiInt.class, VMultiString.class, VShort.class, VShortArray.class,
//            VStatistics.class, VString.class, VStringArray.class, VBoolean.class, VBooleanArray.class, VTable.class,
//            VImage.class);
    private static final Collection<Class<?>> types = Arrays.<Class<?>>asList(
            VByte.class,
            VShort.class,
            VInt.class,
            VLong.class,
            VFloat.class,
            VDouble.class);

    /**
     * Returns the type of the object by returning the class object of one
     * of the VXxx interfaces. The getClass() methods returns the
     * concrete implementation type, which is of little use. If no
     * super-interface is found, Object.class is used.
     *
     * @param obj an object implementing a standard type
     * @return the type is implementing
     */
    public static Class<?> typeOf(Object obj) {
        if (obj == null)
            return null;

        for (Class<?> type : types) {
            if (type.isInstance(obj)) {
                return type;
            }
        }

        return Object.class;
    }

    /**
     * Returns the value with highest severity. null values can either be ignored or
     * treated as disconnected/missing value ({@link Alarm#noValue()}).
     *
     * @param values a list of values
     * @param ignoreNull true to simply skip null values
     * @return the value with highest alarm; can't be null
     */
    public static Alarm highestAlarmOf(final List<?> values, final boolean ignoreNull) {
        Alarm finalAlarm = Alarm.none();
        for (Object value : values) {
            Alarm newAlarm;
            if (value == null && !ignoreNull) {
                newAlarm = Alarm.noValue();
            } else {
                newAlarm = Alarm.none();
                if (value instanceof AlarmProvider) {
                    newAlarm = ((AlarmProvider) value).getAlarm();
                }
            }
            if (newAlarm.getSeverity().compareTo(finalAlarm.getSeverity()) > 0) {
                finalAlarm = newAlarm;
            }
        }

        return finalAlarm;
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible. Calls {@link #toVType(java.lang.Object, org.diirt.vtype.next.Alarm, org.diirt.vtype.next.Time, org.diirt.vtype.next.Display) }
     * with no alarm, time now and no display.
     *
     * @param javaObject the value to wrap
     * @return the new VType value
     */
    public static VType toVType(Object javaObject) {
        return toVType(javaObject, Alarm.none(), Time.now(), Display.none());
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible. Calls {@link #toVType(java.lang.Object, org.diirt.vtype.next.Alarm, org.diirt.vtype.next.Time, org.diirt.vtype.next.Display) }
     * with the given alarm, time now and no display.
     *
     * @param javaObject the value to wrap
     * @param alarm the alarm
     * @return the new VType value
     */
    public static VType toVType(Object javaObject, Alarm alarm) {
        return toVType(javaObject, alarm, Time.now(), Display.none());
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion
     * is possible.
     * <p>
     * Types are converted as follow:
     * <ul>
     *   <li>Boolean -&gt; VBoolean</li>
     *   <li>Number -&gt; corresponding VNumber</li>
     *   <li>String -&gt; VString</li>
     *   <li>number array -&gt; corresponding VNumberArray</li>
     *   <li>ListNumber -&gt; corresponding VNumberArray</li>
     *   <li>List -&gt; if all elements are String, VStringArray</li>
     * </ul>
     *
     * @param javaObject the value to wrap
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new VType value
     */
    public static VType toVType(Object javaObject, Alarm alarm, Time time, Display display) {
        if (javaObject instanceof Number) {
            return VNumber.create((Number) javaObject, alarm, time, display);
        } else if (javaObject instanceof String) {
            return VString.create((String) javaObject, alarm, time);
        } else if (javaObject instanceof Boolean) {
            return null;//newVBoolean((Boolean) javaObject, alarm, time);
        } else if (javaObject instanceof byte[]
                || javaObject instanceof short[]
                || javaObject instanceof int[]
                || javaObject instanceof long[]
                || javaObject instanceof float[]
                || javaObject instanceof double[]) {
            return VNumberArray.create(ListNumbers.toListNumber(javaObject), alarm, time, display);
        } else if (javaObject instanceof ListNumber) {
            return VNumberArray.create((ListNumber) javaObject, alarm, time, display);
        } else if (javaObject instanceof String[]) {
            return null;//newVStringArray(Arrays.asList((String[]) javaObject), alarm, time);
        } else if (javaObject instanceof List) {
            boolean matches = true;
            List list = (List) javaObject;
            for (Object object : list) {
                if (!(object instanceof String)) {
                    matches = false;
                }
            }
            if (matches) {
                @SuppressWarnings("unchecked")
                List<String> newList = (List<String>) list;
                return null;//newVStringArray(Collections.unmodifiableList(newList), alarm, time);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Null and non-VType safe utility to extracts alarm information.
     * <ul>
     * <li>If the value is an {@link AlarmProvider}, the associate alarm is returned.</li>
     * <li>If the value is not an {@link AlarmProvider}, {@link Alarm#NONE} is returned.</li>
     * <li>If the value is null, {@link Alarm#NO_VALUE} is returned.</li>
     * </ul>
     *
     * @param value the value
     * @return the alarm information for the value
     */
    public static Alarm alarmOf(Object value) {
        return alarmOf(value, true);
    }

    /**
     * Null and non-VType safe utility to extracts alarm information for a
     * connection.
     * <ul>
     * <li>If the value is an {@link AlarmProvider}, the associate alarm is returned.</li>
     * <li>If the value is not an {@link AlarmProvider}, {@link Alarm#NONE} is returned.</li>
     * <li>If the value is null and connected is true, {@link Alarm#NO_VALUE} is returned.</li>
     * <li>If the value is null and disconnected is true, {@link Alarm#DISCONNECTED} is returned.</li>
     * </ul>
     *
     * @param value a value
     * @param connected the connection status
     * @return the alarm information
     */
    public static Alarm alarmOf(Object value, boolean connected) {
        if (value != null) {
            if (value instanceof AlarmProvider) {
                return ((AlarmProvider) value).getAlarm();
            } else {
                return Alarm.none();
            }
        } else if (connected) {
            return Alarm.noValue();
        } else {
            return Alarm.disconnected();
        }
    }
}
