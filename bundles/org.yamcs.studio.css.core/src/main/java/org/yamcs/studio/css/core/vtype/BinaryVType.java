package org.yamcs.studio.css.core.vtype;

import java.util.List;

import org.diirt.util.array.ArrayByte;
import org.diirt.util.array.ArrayInt;
import org.diirt.util.array.ListByte;
import org.diirt.util.array.ListInt;
import org.diirt.vtype.ArrayDimensionDisplay;
import org.diirt.vtype.VByteArray;
import org.diirt.vtype.ValueUtil;
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
