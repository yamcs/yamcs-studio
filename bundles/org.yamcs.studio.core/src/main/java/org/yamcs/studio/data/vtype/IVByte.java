package org.yamcs.studio.data.vtype;

/**
 * Immutable VByte implementation.
 */
public class IVByte extends IVNumeric implements VByte {

    private final Byte value;

    IVByte(Byte value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }

}
