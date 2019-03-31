package org.yamcs.studio.css.core.vtype;

import org.diirt.util.array.ArrayBoolean;
import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListBoolean;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.VBooleanArray;
import org.diirt.vtype.VTypeToString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class BooleanArrayVType extends YamcsVType implements VBooleanArray {

    private ListInt sizes;

    private ArrayBoolean data;

    public BooleanArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        boolean[] booleanValues = new boolean[size];
        for (int i = 0; i < booleanValues.length; i++) {
            booleanValues[i] = pval.getEngValue().getArrayValue(i).getBooleanValue();
        }
        data = new ArrayBoolean(booleanValues);
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListBoolean getData() {
        return data;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
