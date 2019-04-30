package org.yamcs.studio.css.core.vtype;

import java.util.ArrayList;
import java.util.List;

import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.VEnumArray;
import org.diirt.vtype.VTypeToString;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.css.core.pvmanager.PVConnectionInfo;

public class EnumeratedArrayVType extends YamcsVType implements VEnumArray {

    private ListInt sizes;

    private ListInt indexes;
    private List<String> data;

    public EnumeratedArrayVType(PVConnectionInfo info, ParameterValue pval) {
        super(pval);

        int size = pval.getEngValue().getArrayValueCount();
        sizes = new ArrayInt(size);

        List<Integer> indexes = new ArrayList<>();
        data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Value value = pval.getEngValue().getArrayValue(i);
            int index = (int) value.getSint64Value();
            indexes.add(index);
            data.add(value.getStringValue());
        }
        this.indexes = new ArrayInt(indexes.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public List<String> getLabels() {
        ParameterCatalogue catalogue = ParameterCatalogue.getInstance();

        // TODO Get an id matching the qualified name from the info object
        // (not e.g. the opsname)
        // But be careful that any suffixes ('[]' or '.') are kept
        NamedObjectId id = NamedObjectId.newBuilder()
                .setName(pval.getId().getName())
                .build();

        ParameterTypeInfo specificPtype = catalogue.getParameterTypeInfo(id);
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
