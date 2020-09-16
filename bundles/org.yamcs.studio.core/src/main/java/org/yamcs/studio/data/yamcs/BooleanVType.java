package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VBoolean;

public class BooleanVType extends YamcsVType implements VBoolean {

    public BooleanVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Boolean getValue() {
        return value.getBooleanValue();
    }

    @Override
    public String toString() {
        return Boolean.toString(value.getBooleanValue());
    }
}
