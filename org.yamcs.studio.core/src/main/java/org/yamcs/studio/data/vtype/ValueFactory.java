/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Factory class for all concrete implementation of the types.
 * <p>
 * The factory methods do not do anything in terms of defensive copy and immutability to the objects, which they are
 * passed as they are. It's the client responsibility to prepare them appropriately, which is automatically done anyway
 * for all objects except collections.
 */
public class ValueFactory {

    /**
     * Creates a new VString.
     */
    public static VString newVString(String value, Alarm alarm, Time time) {
        return new IVString(value, alarm, time);
    }

    /**
     * Creates a new VBoolean.
     */
    public static VBoolean newVBoolean(boolean value, Alarm alarm, Time time) {
        return new IVBoolean(value, alarm, time);
    }

    /**
     * Creates a new VMultiDouble.
     */
    public static VMultiDouble newVMultiDouble(List<VDouble> values, Alarm alarm, Time time, Display display) {
        return new IVMultiDouble(values, alarm, time, display);
    }

    /**
     * Creates a new VLong.
     */
    public static VLong newVLong(Long value, Alarm alarm, Time time, Display display) {
        return new IVLong(value, alarm, time, display);
    }

    /**
     * Creates a new VInt.
     */
    public static VInt newVInt(Integer value, Alarm alarm, Time time, Display display) {
        return new IVInt(value, alarm, time, display);
    }

    /**
     * Creates a new VShort.
     */
    public static VShort newVShort(Short value, Alarm alarm, Time time, Display display) {
        return new IVShort(value, alarm, time, display);
    }

    /**
     * Creates a new VByte.
     */
    public static VByte newVByte(Byte value, Alarm alarm, Time time, Display display) {
        return new IVByte(value, alarm, time, display);
    }

    /**
     * New alarm with the given severity and status.
     */
    public static Alarm newAlarm(AlarmSeverity alarmSeverity, String alarmName) {
        return new Alarm() {
            @Override
            public AlarmSeverity getAlarmSeverity() {
                return alarmSeverity;
            }

            @Override
            public String getAlarmName() {
                return alarmName;
            }

            @Override
            public String toString() {
                return VTypeToString.alarmToString(this);
            }
        };
    }

    private static final Alarm alarmNone = newAlarm(AlarmSeverity.NONE, "NONE");
    private static final Display displayBoolean = newDisplay(0.0, 0.0, 0.0, "", NumberFormats.toStringFormat(), 1.0,
            1.0, 1.0, 0.0, 1.0);

    /**
     * No alarm.
     *
     * @return severity and status NONE
     */
    public static Alarm alarmNone() {
        return alarmNone;
    }

    /**
     * Alarm based on the value and the display ranges.
     */
    public static Alarm newAlarm(Number value, Display display) {
        // Calculate new AlarmSeverity, using display ranges
        var severity = AlarmSeverity.NONE;
        var status = "NONE";
        if (value.doubleValue() <= display.getLowerAlarmLimit()) {
            status = "LOLO";
            severity = AlarmSeverity.MAJOR;
        } else if (value.doubleValue() >= display.getUpperAlarmLimit()) {
            status = "HIHI";
            severity = AlarmSeverity.MAJOR;
        } else if (value.doubleValue() <= display.getLowerWarningLimit()) {
            status = "LOW";
            severity = AlarmSeverity.MINOR;
        } else if (value.doubleValue() >= display.getUpperWarningLimit()) {
            status = "HIGH";
            severity = AlarmSeverity.MINOR;
        }

        return newAlarm(severity, status);
    }

    /**
     * Creates a new time.
     */
    public static Time newTime(Instant timestamp, Integer timeUserTag, boolean timeValid) {
        return new Time() {
            @Override
            public Instant getTimestamp() {
                return timestamp;
            }

            @Override
            public Integer getTimeUserTag() {
                return timeUserTag;
            }

            @Override
            public boolean isTimeValid() {
                return timeValid;
            }
        };
    }

    /**
     * New time, with no user tag and valid data.
     */
    public static Time newTime(Instant timestamp) {
        return newTime(timestamp, null, true);
    }

    /**
     * New time with the current timestamp, no user tag and valid data.
     *
     * @return the new time
     */
    public static Time timeNow() {
        return newTime(Instant.now(), null, true);
    }

    /**
     * Creates a new display
     */
    public static Display newDisplay(Double lowerDisplayLimit, Double lowerAlarmLimit, double lowerWarningLimit,
            String units, NumberFormat numberFormat, Double upperWarningLimit, double upperAlarmLimit,
            Double upperDisplayLimit, double lowerCtrlLimit, Double upperCtrlLimit) {
        return new Display() {
            @Override
            public Double getLowerCtrlLimit() {
                return lowerCtrlLimit;
            }

            @Override
            public Double getUpperCtrlLimit() {
                return upperCtrlLimit;
            }

            @Override
            public Double getLowerDisplayLimit() {
                return lowerDisplayLimit;
            }

            @Override
            public Double getLowerAlarmLimit() {
                return lowerAlarmLimit;
            }

            @Override
            public Double getLowerWarningLimit() {
                return lowerWarningLimit;
            }

            @Override
            public String getUnits() {
                return units;
            }

            @Override
            public NumberFormat getFormat() {
                return numberFormat;
            }

            @Override
            public Double getUpperWarningLimit() {
                return upperWarningLimit;
            }

            @Override
            public Double getUpperAlarmLimit() {
                return upperAlarmLimit;
            }

            @Override
            public Double getUpperDisplayLimit() {
                return upperDisplayLimit;
            }
        };
    }

    public static ArrayDimensionDisplay newDisplay(ListNumber boundaries, String unit) {
        return newDisplay(boundaries, false, unit);
    }

    public static ArrayDimensionDisplay newDisplay(ListNumber boundaries, boolean reversed, String unit) {
        return new IArrayDimensionDisplay(boundaries, reversed, unit);
    }

    public static ArrayDimensionDisplay newDisplay(int size, ListNumberProvider boundaryProvider, boolean invert) {
        return newDisplay(boundaryProvider.createListNumber(size + 1), invert, "");
    }

    public static ArrayDimensionDisplay newDisplay(int size, ListNumberProvider boundaryProvider) {
        return newDisplay(boundaryProvider.createListNumber(size + 1), false, "");
    }

    /**
     * Returns an array display where the index is used to calculate the cell boundaries.
     *
     * @param nCells
     *            the number of cells along the direction
     * @return a new array display
     */
    public static ArrayDimensionDisplay newDisplay(int nCells) {
        var boundaries = ListNumbers.linearList(0, 1, nCells + 1);
        return newDisplay(boundaries, "");
    }

    private static final Display displayNone = newDisplay(Double.NaN, Double.NaN, Double.NaN, "",
            NumberFormats.toStringFormat(), Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    /**
     * Empty display information.
     *
     * @return no display
     */
    public static Display displayNone() {
        return displayNone;
    }

    /**
     * Returns a display from 0 to 1, suitable for booleans.
     *
     * @return a display for boolean
     */
    public static Display displayBoolean() {
        return displayBoolean;
    }

    /**
     * Creates a new VNumber based on the type of the data
     */
    public static VNumber newVNumber(Number value, Alarm alarm, Time time, Display display) {
        if (value instanceof Double) {
            return newVDouble((Double) value, alarm, time, display);
        } else if (value instanceof Float) {
            return newVFloat((Float) value, alarm, time, display);
        } else if (value instanceof Long) {
            return newVLong((Long) value, alarm, time, display);
        } else if (value instanceof BigInteger) { // Can be generated by jython when a value is set
            return newVLong(((BigInteger) value).longValue(), alarm, time, display);
        } else if (value instanceof Integer) {
            return newVInt((Integer) value, alarm, time, display);
        } else if (value instanceof Short) {
            return newVShort((Short) value, alarm, time, display);
        } else if (value instanceof Byte) {
            return newVByte((Byte) value, alarm, time, display);
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new VDouble.
     */
    public static VDouble newVDouble(Double value, Alarm alarm, Time time, Display display) {
        return new IVDouble(value, alarm, time, display);
    }

    /**
     * Creates a new VDouble using the given value, time, display and generating the alarm from the value and display
     * information.
     */
    public static VDouble newVDouble(Double value, Time time, Display display) {
        return newVDouble(value, newAlarm(value, display), time, display);
    }

    /**
     * Creates new immutable VDouble by using metadata from the old value, now as timestamp and computing alarm from the
     * metadata range.
     */
    public static VDouble newVDouble(Double value, Display display) {
        return newVDouble(value, timeNow(), display);
    }

    /**
     * Creates a new VDouble, no alarm, time now, no display.
     */
    public static VDouble newVDouble(Double value) {
        return newVDouble(value, alarmNone(), timeNow(), displayNone());
    }

    /**
     * Creates a new VDouble, no alarm, no display.
     */
    public static VDouble newVDouble(Double value, Time time) {
        return newVDouble(value, alarmNone(), time, displayNone());
    }

    /**
     * Creates a new VFloat.
     */
    public static VFloat newVFloat(Float value, Alarm alarm, Time time, Display display) {
        return new IVFloat(value, alarm, time, display);
    }

    /**
     * Create a new VEnum.
     *
     * @param index
     *            the index in the label array
     * @param labels
     *            the labels
     * @param alarm
     *            the alarm
     * @param time
     *            the time
     * @return the new value
     */
    public static VEnum newVEnum(int index, List<String> labels, Alarm alarm, Time time) {
        return new IVEnum(index, labels, alarm, time);
    }

    /**
     * Creates a new VStatistics.
     */
    public static VStatistics newVStatistics(double average, double stdDev, double min, double max, int nSamples,
            Alarm alarm, Time time, Display display) {
        return new IVStatistics(average, stdDev, min, max, nSamples, alarm, time, display);
    }

    /**
     * Creates a new VNumberArray based on the type of the data.
     */
    public static VNumberArray newVNumberArray(ListNumber data, Alarm alarm, Time time, Display display) {
        ListInt sizes = new ArrayInt(data.size());
        var dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);
        return newVNumberArray(data, sizes, dimensionDisplay, alarm, time, display);
    }

    /**
     * Creates a new VNumberArray based on the type of the data.
     *
     * @param data
     *            the array data
     * @param sizes
     *            the array shape
     * @param dimensionDisplay
     *            the array axis display information
     * @param alarm
     *            the alarm
     * @param time
     *            the time
     * @param display
     *            the display
     * @return a new value
     */
    public static VNumberArray newVNumberArray(ListNumber data, ListInt sizes,
            List<ArrayDimensionDisplay> dimensionDisplay, Alarm alarm, Time time, Display display) {
        if (data instanceof ListDouble) {
            return new IVDoubleArray((ListDouble) data, sizes, dimensionDisplay, alarm, time, display);
        } else if (data instanceof ListFloat) {
            return new IVFloatArray((ListFloat) data, sizes, dimensionDisplay, alarm, time, display);
        } else if (data instanceof ListLong) {
            return new IVLongArray((ListLong) data, sizes, dimensionDisplay, alarm, time, display);
        } else if (data instanceof ListInt) {
            return new IVIntArray((ListInt) data, sizes, dimensionDisplay, alarm, time, display);
        } else if (data instanceof ListByte) {
            return new IVByteArray((ListByte) data, sizes, dimensionDisplay, alarm, time, display);
        } else if (data instanceof ListShort) {
            return new IVShortArray((ListShort) data, sizes, dimensionDisplay, alarm, time, display);
        }
        throw new UnsupportedOperationException("Data is of an unsupported type (" + data.getClass() + ")");
    }

    /**
     * Constructs and nd array with the data, time and alarm in the first array and the given dimension information.
     *
     * @param data
     *            the array with the data
     * @param dimensions
     *            the dimension information
     * @return a new array
     */
    public static VNumberArray ndArray(VNumberArray data, ArrayDimensionDisplay... dimensions) {
        var sizes = new int[dimensions.length];
        List<ArrayDimensionDisplay> displays = new ArrayList<>();
        for (var i = 0; i < dimensions.length; i++) {
            var dimensionInfo = dimensions[i];
            sizes[i] = dimensionInfo.getCellBoundaries().size() - 1;
            displays.add(dimensionInfo);
        }
        return ValueFactory.newVNumberArray(data.getData(), new ArrayInt(sizes), displays, data, data, data);
    }

    /**
     * Creates a new VDoubleArray.
     */
    public static VDoubleArray newVDoubleArray(ListDouble data, Alarm alarm, Time time, Display display) {
        return new IVDoubleArray(data, new ArrayInt(data.size()), alarm, time, display);
    }

    /**
     * Creates a new VFloatArray.
     */
    public static VFloatArray newVFloatArray(ListFloat data, Alarm alarm, Time time, Display display) {
        return new IVFloatArray(data, new ArrayInt(data.size()), alarm, time, display);
    }

    /**
     * Creates a new VImage given the data and the size.
     */
    public static VImage newVImage(int height, int width, byte[] data) {
        return newVImage(height, width, data, alarmNone(), timeNow());
    }

    /**
     * Creates a new VImage given the data and the size.
     */
    public static VImage newVImage(int height, int width, byte[] data, Alarm alarm, Time time) {
        return new IVImage(height, width, new ArrayByte(data), VImageDataType.pvByte, VImageType.TYPE_3BYTE_BGR, alarm,
                time);
    }

    /**
     * Creates a new VImage of type TYPE_3BYTE_BGR given the data and the size.
     */
    public static VImage newVImage(int height, int width, ListNumber data, VImageDataType imageDataType, Alarm alarm,
            Time time) {
        return new IVImage(height, width, data, imageDataType, VImageType.TYPE_3BYTE_BGR, alarm, time);
    }

    /**
     * Creates a new VLongArray.
     */
    public static VLongArray newVLongArray(ListLong values, Alarm alarm, Time time, Display display) {
        ListInt sizes = new ArrayInt(values.size());
        return new IVLongArray(values, sizes, ValueUtil.defaultArrayDisplay(sizes), alarm, time, display);
    }

    /**
     * Creates a new VIntArray.
     */
    public static VIntArray newVIntArray(ListInt values, Alarm alarm, Time time, Display display) {
        return new IVIntArray(values, new ArrayInt(values.size()), alarm, time, display);
    }

    /**
     * Creates a new VShortArray.
     */
    public static VShortArray newVShortArray(ListShort values, Alarm alarm, Time time, Display display) {
        ListInt sizes = new ArrayInt(values.size());
        return new IVShortArray(values, sizes, ValueUtil.defaultArrayDisplay(sizes), alarm, time, display);
    }

    /**
     * Creates a new VByteArray.
     */
    public static VByteArray newVByteArray(ListByte values, Alarm alarm, Time time, Display display) {
        ListInt sizes = new ArrayInt(values.size());
        return new IVByteArray(values, sizes, ValueUtil.defaultArrayDisplay(sizes), alarm, time, display);
    }

    /**
     * Create a new VEnumArray.
     *
     * @param indexes
     *            the indexes in the label array
     * @param labels
     *            the labels
     * @param alarm
     *            the alarm
     * @param time
     *            the time
     * @return the new value
     */
    public static VEnumArray newVEnumArray(ListInt indexes, List<String> labels, Alarm alarm, Time time) {
        return new IVEnumArray(indexes, labels, new ArrayInt(indexes.size()), alarm, time);
    }

    /**
     * Creates a new VBooleanArray.
     */
    public static VBooleanArray newVBooleanArray(ListBoolean data, Alarm alarm, Time time) {
        return new IVBooleanArray(data, new ArrayInt(data.size()), alarm, time);
    }

    /**
     * Creates a new VStringArray.
     */
    public static VStringArray newVStringArray(List<String> data, Alarm alarm, Time time) {
        return new IVStringArray(data, new ArrayInt(data.size()), alarm, time);
    }

    /**
     * Creates a new VTable - this method is provisional and will change in the future.
     *
     * @param types
     *            the types for each column
     * @param names
     *            the names for each column
     * @param values
     *            the values for each column
     * @return the new value
     */
    public static VTable newVTable(List<Class<?>> types, List<String> names, List<Object> values) {
        // TODO: should check the types
        return new IVTable(types, names, values);
    }

    /**
     * Takes a java objects and wraps it into a vType. All numbers are wrapped as VDouble. String is wrapped as VString.
     * double[] and ListDouble are wrapped as VDoubleArray. A List of String is wrapped to a VStringArray. Alarms are
     * alarmNone(), time are timeNow() and display are displayNone();
     *
     * @param value
     *            the value to wrap
     * @return the wrapped value
     * @deprecated use {@link #toVType(java.lang.Object) }
     */
    @Deprecated
    public static VType wrapValue(Object value) {
        return wrapValue(value, alarmNone());
    }

    /**
     * Takes a java objects and wraps it into a vType. All numbers are wrapped as VDouble. String is wrapped as VString.
     * double[] and ListDouble are wrapped as VDoubleArray. A List of String is wrapped to a VStringArray. Alarms are
     * alarm, time are timeNow() and display are displayNone();
     *
     * @param value
     *            the value to wrap
     * @param alarm
     *            the alarm for the value
     * @return the wrapped value
     * @deprecated use
     *             {@link #toVType(java.lang.Object, org.yamcs.studio.data.vtype.Alarm, org.yamcs.studio.data.vtype.Time, org.yamcs.studio.data.vtype.Display) }
     */
    @Deprecated
    public static VType wrapValue(Object value, Alarm alarm) {
        if (value instanceof Number) {
            // Special support for numbers
            return newVDouble(((Number) value).doubleValue(), alarm, timeNow(), displayNone());
        } else if (value instanceof String) {
            // Special support for strings
            return newVString(((String) value), alarm, timeNow());
        } else if (value instanceof double[]) {
            return newVDoubleArray(new ArrayDouble((double[]) value), alarm, timeNow(), displayNone());
        } else if (value instanceof ListDouble) {
            return newVDoubleArray((ListDouble) value, alarm, timeNow(), displayNone());
        } else if (value instanceof List) {
            var matches = true;
            var list = (List) value;
            for (var object : list) {
                if (!(object instanceof String)) {
                    matches = false;
                }
            }
            if (matches) {
                @SuppressWarnings("unchecked")
                var newList = (List<String>) list;
                return newVStringArray(Collections.unmodifiableList(newList), alarm, timeNow());
            } else {
                throw new UnsupportedOperationException("Type " + value.getClass().getName() + " contains non Strings");
            }
        } else {
            // TODO: need to implement all the other arrays
            throw new UnsupportedOperationException("Type " + value.getClass().getName() + "  is not yet supported");
        }
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion is possible. Calls
     * {@link #toVType(java.lang.Object, org.yamcs.studio.data.vtype.Alarm, org.yamcs.studio.data.vtype.Time, org.yamcs.studio.data.vtype.Display)}
     * with no alarm, time now and no display.
     *
     * @param javaObject
     *            the value to wrap
     * @return the new VType value
     */
    public static VType toVType(Object javaObject) {
        return toVType(javaObject, alarmNone(), timeNow(), displayNone());
    }

    /**
     * Converts a standard java type to VTypes. Returns null if no conversion is possible.
     * <p>
     * Types are converted as follow:
     * <ul>
     * <li>Boolean -&gt; VBoolean</li>
     * <li>Number -&gt; corresponding VNumber</li>
     * <li>String -&gt; VString</li>
     * <li>number array -&gt; corresponding VNumberArray</li>
     * <li>ListNumber -&gt; corresponding VNumberArray</li>
     * <li>List -&gt; if all elements are String, VStringArray</li>
     * </ul>
     *
     * @param javaObject
     *            the value to wrap
     * @param alarm
     *            the alarm
     * @param time
     *            the time
     * @param display
     *            the display
     * @return the new VType value
     */
    public static VType toVType(Object javaObject, Alarm alarm, Time time, Display display) {
        if (javaObject instanceof Number) {
            return ValueFactory.newVNumber((Number) javaObject, alarm, time, display);
        } else if (javaObject instanceof String) {
            return newVString((String) javaObject, alarm, time);
        } else if (javaObject instanceof Boolean) {
            return newVBoolean((Boolean) javaObject, alarm, time);
        } else if (javaObject instanceof byte[] || javaObject instanceof short[] || javaObject instanceof int[]
                || javaObject instanceof long[] || javaObject instanceof float[] || javaObject instanceof double[]) {
            return newVNumberArray(ListNumbers.toListNumber(javaObject), alarm, time, display);
        } else if (javaObject instanceof ListNumber) {
            return newVNumberArray((ListNumber) javaObject, alarm, time, display);
        } else if (javaObject instanceof String[]) {
            return newVStringArray(Arrays.asList((String[]) javaObject), alarm, time);
        } else if (javaObject instanceof List) {
            var matches = true;
            var list = (List<?>) javaObject;
            for (var object : list) {
                if (!(object instanceof String)) {
                    matches = false;
                }
            }
            if (matches) {
                @SuppressWarnings("unchecked")
                var newList = (List<String>) list;
                return newVStringArray(Collections.unmodifiableList(newList), alarm, time);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * As {@link #toVType(java.lang.Object)} but throws an exception if conversion not possible.
     *
     * @param javaObject
     *            the value to wrap
     * @return the new VType value
     */
    public static VType toVTypeChecked(Object javaObject) {
        var value = toVType(javaObject);
        if (value == null) {
            throw new RuntimeException("Value " + value + " cannot be converted to VType.");
        }
        return value;
    }

    /**
     * As
     * {@link #toVType(java.lang.Object, org.yamcs.studio.data.vtype.Alarm, org.yamcs.studio.data.vtype.Time, org.yamcs.studio.data.vtype.Display)}
     * but throws an exception if conversion not possible.
     *
     * @param javaObject
     *            the value to wrap
     * @param alarm
     *            the alarm
     * @param time
     *            the time
     * @param display
     *            the display
     * @return the new VType value
     */
    public static VType toVTypeChecked(Object javaObject, Alarm alarm, Time time, Display display) {
        var value = toVType(javaObject, alarm, time, display);
        if (value == null) {
            throw new RuntimeException("Value " + value + " cannot be converted to VType.");
        }
        return value;
    }
}
