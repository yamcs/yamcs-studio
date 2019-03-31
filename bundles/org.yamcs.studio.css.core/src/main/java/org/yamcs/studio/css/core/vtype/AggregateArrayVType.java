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
            data.add(StringConverter.toString(aggregateValue, false));
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
