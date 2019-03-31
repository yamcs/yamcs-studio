package org.yamcs.studio.css.core.vtype;

import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ArrayLong;
import org.diirt.util.array.ListInt;
import org.diirt.util.array.ListLong;
import org.diirt.vtype.ArrayDimensionDisplay;
import org.diirt.vtype.VLongArray;
import org.diirt.vtype.VTypeToString;
import org.diirt.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Uint32ArrayVType extends YamcsVType implements VLongArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListLong data;

    public Uint32ArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        long[] longValues = new long[size];
        for (int i = 0; i < longValues.length; i++) {
            longValues[i] = pval.getEngValue().getArrayValue(i).getUint32Value() & 0xFFFFFFFFL;
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
