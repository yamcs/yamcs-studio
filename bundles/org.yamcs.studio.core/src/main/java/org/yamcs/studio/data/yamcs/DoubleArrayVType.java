package org.yamcs.studio.data.yamcs;

import java.util.List;

import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VDoubleArray;
import org.yamcs.studio.data.vtype.VTypeToString;
import org.yamcs.studio.data.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class DoubleArrayVType extends YamcsVType implements VDoubleArray {

    private ListInt sizes;
    private List<ArrayDimensionDisplay> dimensionDisplay;

    private ArrayDouble data;

    public DoubleArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);
        dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);

        double[] doubleValues = new double[size];
        for (int i = 0; i < doubleValues.length; i++) {
            doubleValues[i] = pval.getEngValue().getArrayValue(i).getDoubleValue();
        }
        data = new ArrayDouble(doubleValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListDouble getData() {
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
