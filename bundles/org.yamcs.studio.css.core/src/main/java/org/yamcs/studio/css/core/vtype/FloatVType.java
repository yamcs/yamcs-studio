package org.yamcs.studio.css.core.vtype;

import org.diirt.vtype.VFloat;
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
        return Float.toString(pval.getEngValue().getFloatValue());
    }
}
