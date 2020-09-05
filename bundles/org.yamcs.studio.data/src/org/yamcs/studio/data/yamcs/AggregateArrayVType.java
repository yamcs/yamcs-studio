package org.yamcs.studio.data.yamcs;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VTypeToString;

public class AggregateArrayVType extends YamcsVType implements VStringArray {

    private ListInt sizes;

    private List<String> data;

    public AggregateArrayVType(ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value aggregateValue = pval.getEngValue().getArrayValue(i);
            data.add(StringConverter.toString(aggregateValue));
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
