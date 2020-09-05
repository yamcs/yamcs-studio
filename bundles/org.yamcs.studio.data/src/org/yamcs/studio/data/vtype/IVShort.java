package org.yamcs.studio.data.vtype;

/**
 * Immutable VShort implementation.
 */
public class IVShort extends IVNumeric implements VShort {

    private final Short value;

    IVShort(Short value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Short getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
