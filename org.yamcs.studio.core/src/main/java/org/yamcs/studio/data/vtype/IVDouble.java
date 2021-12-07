package org.yamcs.studio.data.vtype;

/**
 * Immutable VDouble implementation.
 */
public class IVDouble extends IVNumeric implements VDouble {

    private double value;

    public IVDouble(Double value) {
        this(value, ValueFactory.alarmNone(), ValueFactory.timeNow(), ValueFactory.displayNone());
    }

    public IVDouble(Double value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
