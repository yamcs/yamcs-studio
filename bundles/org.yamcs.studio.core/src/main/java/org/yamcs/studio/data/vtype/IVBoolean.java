package org.yamcs.studio.data.vtype;

/**
 * Simple implementation for VBoolean.
 */
public class IVBoolean extends IVMetadata implements VBoolean {

    private final boolean value;

    public IVBoolean(boolean value, Alarm alarm, Time time) {
        super(alarm, time);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
