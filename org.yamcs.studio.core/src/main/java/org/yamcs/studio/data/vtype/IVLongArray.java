package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVLongArray extends IVNumberArray implements VLongArray {

    private final ListLong data;

    public IVLongArray(ListLong data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListLong getData() {
        return data;
    }
}
