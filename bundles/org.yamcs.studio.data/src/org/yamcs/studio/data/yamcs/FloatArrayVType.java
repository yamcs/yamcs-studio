package org.yamcs.studio.data.yamcs;

import java.util.List;

import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ArrayFloat;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListFloat;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VFloatArray;
import org.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class FloatArrayVType extends YamcsVType implements VFloatArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ListFloat data;

    public FloatArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        float[] floatValues = new float[size];
        for (int i = 0; i < floatValues.length; i++) {
            floatValues[i] = pval.getEngValue().getArrayValue(i).getFloatValue();
        }
        data = new ArrayFloat(floatValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListFloat getData() {
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
