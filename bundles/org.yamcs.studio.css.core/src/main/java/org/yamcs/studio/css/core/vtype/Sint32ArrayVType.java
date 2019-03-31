package org.yamcs.studio.css.core.vtype;

import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.ArrayDimensionDisplay;
import org.diirt.vtype.VIntArray;
import org.diirt.vtype.VTypeToString;
import org.diirt.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Sint32ArrayVType extends YamcsVType implements VIntArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ArrayInt data;

    public Sint32ArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        int[] intValues = new int[size];
        for (int i = 0; i < intValues.length; i++) {
            intValues[i] = pval.getEngValue().getArrayValue(i).getSint32Value();
        }
        data = new ArrayInt(intValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListInt getData() {
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
