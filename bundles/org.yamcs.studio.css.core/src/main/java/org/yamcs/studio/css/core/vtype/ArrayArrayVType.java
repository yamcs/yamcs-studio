package org.yamcs.studio.css.core.vtype;

import java.util.ArrayList;
import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VTypeToString;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.utils.StringConverter;

// Array of arrays, just render them with string values.
public class ArrayArrayVType extends YamcsVType implements VStringArray {

    private ListInt sizes;

    private List<String> data;

    public ArrayArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value arrayValue = pval.getEngValue().getArrayValue(i);
            data.add(StringConverter.toString(arrayValue, false));
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
