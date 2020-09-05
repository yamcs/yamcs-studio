package org.yamcs.studio.data.vtype;

/**
 * Immutable VDouble implementation.
 */
class IVDouble extends IVNumeric implements VDouble {

    private final Double value;

    IVDouble(Double value, Alarm alarm, Time time, Display display) {
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
