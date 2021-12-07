package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVByteArray extends IVNumberArray implements VByteArray {

    private final ListByte data;

    public IVByteArray(ListByte data, ListInt sizes, Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVByteArray(ListByte data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListByte getData() {
        return data;
    }
}
