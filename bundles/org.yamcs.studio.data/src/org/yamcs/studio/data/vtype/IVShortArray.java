package org.yamcs.studio.data.vtype;

import java.util.List;

public class IVShortArray extends IVNumberArray implements VShortArray {

    private final ListShort data;

    public IVShortArray(ListShort data, ListInt sizes,
            Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVShortArray(ListShort data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay,
            Alarm alarm, Time time, Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListShort getData() {
        return data;
    }
}
