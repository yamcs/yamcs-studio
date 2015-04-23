package org.yamcs.studio.client.vtype;

import java.util.List;

import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListByte;
import org.epics.util.array.ListInt;
import org.epics.vtype.ArrayDimensionDisplay;
import org.epics.vtype.VByteArray;
import org.epics.vtype.ValueUtil;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class BinaryVType extends YamcsVType implements VByteArray {

    public BinaryVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public ListInt getSizes() {
        return new ArrayInt(pval.getEngValue().getBinaryValue().size());
    }

    @Override
    public ListByte getData() {
        return new ArrayByte(pval.getEngValue().getBinaryValue().toByteArray());
    }

    @Override
    public List<ArrayDimensionDisplay> getDimensionDisplay() {
        return ValueUtil.defaultArrayDisplay(this);
    }

    @Override
    public String toString() {
        if (pval.getEngValue().getBinaryValue() == null) {
            return "null";
        } else {
            return "<binary>";
        }
    }
}
