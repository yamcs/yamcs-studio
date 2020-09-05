package org.yamcs.studio.data.yamcs;

import org.yamcs.studio.data.vtype.VDouble;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class DoubleVType extends YamcsVType implements VDouble {

    public DoubleVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Double getValue() {
        return pval.getEngValue().getDoubleValue();
    }

    @Override
    public String toString() {
        return Double.toString(pval.getEngValue().getDoubleValue());
    }
}
