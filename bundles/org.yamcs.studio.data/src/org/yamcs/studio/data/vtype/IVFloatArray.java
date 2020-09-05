package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVFloatArray extends IVNumberArray implements VFloatArray {

    private final ListFloat data;

    public IVFloatArray(ListFloat data, ListInt sizes,
            Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVFloatArray(ListFloat data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay,
            Alarm alarm, Time time, Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListFloat getData() {
        return data;
    }
}
