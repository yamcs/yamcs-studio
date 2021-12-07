package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVIntArray extends IVNumberArray implements VIntArray {

    private final ListInt data;

    public IVIntArray(ListInt data, ListInt sizes, Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVIntArray(ListInt data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListInt getData() {
        return data;
    }
}
