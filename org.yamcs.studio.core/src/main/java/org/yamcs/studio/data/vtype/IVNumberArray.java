package org.yamcs.studio.data.vtype;

import java.util.List;

public abstract class IVNumberArray extends IVNumeric implements VNumberArray {

    private final ListInt sizes;
    private final List<ArrayDimensionDisplay> dimensionDisplay;

    public IVNumberArray(ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(alarm, time, display);
        this.sizes = sizes;
        if (dimDisplay == null) {
            this.dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);
        } else {
            this.dimensionDisplay = dimDisplay;
        }
    }

    @Override
    public final ListInt getSizes() {
        return sizes;
    }

    @Override
    public final String toString() {
        return VTypeToString.toString(this);
    }

    @Override
    public final List<ArrayDimensionDisplay> getDimensionDisplay() {
        return dimensionDisplay;
    }
}
