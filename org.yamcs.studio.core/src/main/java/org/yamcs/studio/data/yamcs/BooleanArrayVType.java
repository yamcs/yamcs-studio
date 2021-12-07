package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.ArrayBoolean;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListBoolean;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VBooleanArray;
import org.yamcs.studio.data.vtype.VTypeToString;

public class BooleanArrayVType extends YamcsVType implements VBooleanArray {

    private ListInt sizes;

    private ArrayBoolean data;

    public BooleanArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        var size = value.getArrayValueCount();
        sizes = new ArrayInt(size);

        var booleanValues = new boolean[size];
        for (var i = 0; i < booleanValues.length; i++) {
            booleanValues[i] = value.getArrayValue(i).getBooleanValue();
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
