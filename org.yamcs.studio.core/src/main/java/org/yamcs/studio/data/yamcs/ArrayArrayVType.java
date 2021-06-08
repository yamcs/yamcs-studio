package org.yamcs.studio.data.yamcs;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VTypeToString;

// Array of arrays, just render them with string values.
public class ArrayArrayVType extends YamcsVType implements VStringArray {

    private ListInt sizes;

    private List<String> data;

    public ArrayArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);

        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value arrayValue = value.getArrayValue(i);
            data.add(StringConverter.toString(arrayValue));
        }
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public List<String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
