package org.yamcs.studio.data.vtype;

public class IVBooleanArray extends IVMetadata implements VBooleanArray {

    private final ListInt sizes;
    private final ListBoolean data;

    public IVBooleanArray(ListBoolean data, ListInt sizes, Alarm alarm, Time time) {
        super(alarm, time);
        this.data = data;
        this.sizes = sizes;
    }

    @Override
    public ListBoolean getData() {
        return data;
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
