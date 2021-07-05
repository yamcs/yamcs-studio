package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VFloat;

public class FloatVType extends YamcsVType implements VFloat {

    public FloatVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Float getValue() {
        return value.getFloatValue();
    }

    @Override
    public String toString() {
        return Float.toString(value.getFloatValue());
    }
}
