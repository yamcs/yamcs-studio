package org.yamcs.studio.css.core.vtype;

import java.util.List;

import org.diirt.util.array.ArrayDouble;
import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListDouble;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.ArrayDimensionDisplay;
import org.diirt.vtype.VDoubleArray;
import org.diirt.vtype.VTypeToString;
import org.diirt.vtype.ValueUtil;
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
