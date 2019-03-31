package org.yamcs.studio.css.core.vtype;

import java.util.ArrayList;
import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VTypeToString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class StringArrayVType extends YamcsVType implements VStringArray {

    private ListInt sizes;

    private List<String> data;

    public StringArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(pval.getEngValue().getArrayValue(i).getStringValue());
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
