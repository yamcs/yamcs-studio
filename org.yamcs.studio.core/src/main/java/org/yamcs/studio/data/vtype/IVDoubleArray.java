package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVDoubleArray extends IVNumberArray implements VDoubleArray {

    private final ListDouble data;

    public IVDoubleArray(ListDouble data, ListInt sizes,
            Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVDoubleArray(ListDouble data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay,
            Alarm alarm, Time time, Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListDouble getData() {
        return data;
    }

}
