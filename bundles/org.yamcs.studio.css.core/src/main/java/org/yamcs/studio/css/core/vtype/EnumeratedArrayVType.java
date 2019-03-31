package org.yamcs.studio.css.core.vtype;

import java.util.ArrayList;
import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VTypeToString;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.css.core.pvmanager.PVConnectionInfo;

public class EnumeratedArrayVType extends YamcsVType implements VEnumArray {

    private ListInt sizes;

    private ListInt indexes;
    private List<String> labels;
    private List<String> array;

    public EnumeratedArrayVType(PVConnectionInfo info, ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value value = pval.getEngValue().getArrayValue(i);
            int index = EnumeratedVType.getIndexForValue(value);
            indexes.add(index);
        }
        this.indexes = new ArrayInt(indexes.stream().mapToInt(Integer::intValue).toArray());

        // TODO this may not be the correct ptype if the enum array is somwhere inside an aggregate or array.
        this.labels = EnumeratedVType.getLabelsForType(info.parameter.getType());

        List<String> tempArray = new ArrayList<>(this.indexes.size());
        for (int i = 0; i < this.indexes.size(); i++) {
            int index = this.indexes.getInt(i);
            if (index < 0 || index >= labels.size()) {
                throw new IndexOutOfBoundsException("VEnumArray indexes must be within the label range");
            }
            tempArray.add(labels.get(index));
        }
        this.array = tempArray;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public List<String> getData() {
        return array;
    }

    @Override
    public ListInt getIndexes() {
        return indexes;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
