package org.yamcs.studio.core.vtype;

import org.epics.vtype.VFloat;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class FloatVType extends YamcsVType implements VFloat {

    public FloatVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Float getValue() {
        return pval.getEngValue().getFloatValue();
    }

    @Override
    public String toString() {
        return String.valueOf(pval.getEngValue().getFloatValue());
    }
}
