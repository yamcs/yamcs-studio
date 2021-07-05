package org.yamcs.studio.data.yamcs;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VEnumArray;
import org.yamcs.studio.data.vtype.VTypeToString;

public class EnumeratedArrayVType extends YamcsVType implements VEnumArray {

    private ListInt sizes;

    private ListInt indexes;
    private List<String> data;

    public EnumeratedArrayVType(ParameterValue pval, boolean raw) {
        super(pval, raw);

        int size = value.getArrayValueCount();
        sizes = new ArrayInt(size);

        List<Integer> indexes = new ArrayList<>();
        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value enumValue = value.getArrayValue(i);
            int index = (int) enumValue.getSint64Value();
            indexes.add(index);
            data.add(enumValue.getStringValue());
        }
        this.indexes = new ArrayInt(indexes.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public List<String> getLabels() {

        // TODO Get an id matching the qualified name from the info object
        // (not e.g. the opsname)
        // But be careful that any suffixes ('[]' or '.') are kept
        NamedObjectId id = NamedObjectId.newBuilder()
                .setName(getId().getName())
                .build();

        ParameterTypeInfo specificPtype = YamcsPlugin.getMissionDatabase().getParameterTypeInfo(id);
        return EnumeratedVType.getLabelsForType(specificPtype);
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
    public ListInt getIndexes() {
        return indexes;
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
