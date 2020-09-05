package org.yamcs.studio.data.yamcs;

import java.util.List;

import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ArrayLong;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.ListLong;
import org.yamcs.studio.data.vtype.VLongArray;
import org.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Uint64ArrayVType extends YamcsVType implements VLongArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListLong data;

    public Uint64ArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        long[] longValues = new long[size];
        for (int i = 0; i < longValues.length; i++) {
            longValues[i] = pval.getEngValue().getArrayValue(i).getUint64Value();
        }
        data = new ArrayLong(longValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListLong getData() {
        return data;
    }

    @Override
    public List<ArrayDimensionDisplay> getDimensionDisplay() {
        return dimensionDisplay;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
