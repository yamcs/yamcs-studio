package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VDouble;

public class DoubleVType extends YamcsVType implements VDouble {

    public DoubleVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Double getValue() {
        return value.getDoubleValue();
    }

    @Override
    public String toString() {
        return Double.toString(value.getDoubleValue());
    }
}
