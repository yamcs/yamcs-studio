package org.yamcs.studio.data.vtype;

/**
 * Immutable VInt implementation.
 *
 * @author carcassi
 */
class IVLong extends IVNumeric implements VLong {

    private final Long value;

    IVLong(Long value, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }

}
