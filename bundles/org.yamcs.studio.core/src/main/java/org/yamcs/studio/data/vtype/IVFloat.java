package org.yamcs.studio.data.vtype;

/**
 * Immutable VFloat implementation.
 */
public class IVFloat extends IVNumeric implements VFloat {

    private final Float value;

    IVFloat(Float value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
