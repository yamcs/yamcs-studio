package org.yamcs.studio.data.vtype;

/**
 * Immutable VInt implementation.
 */
public class IVInt extends IVNumeric implements VInt {

    private final Integer value;

    public IVInt(Integer value) {
        this(value, ValueFactory.alarmNone(), ValueFactory.timeNow(), ValueFactory.displayNone());
    }

    public IVInt(Integer value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
