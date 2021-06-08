package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVStringArray extends IVMetadata implements VStringArray {

    private final ListInt sizes;
    private final List<String> data;

    public IVStringArray(List<String> data, ListInt sizes, Alarm alarm, Time time) {
        super(alarm, time);
        this.data = data;
        this.sizes = sizes;
    }

    @Override
    public List<String> getData() {
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
