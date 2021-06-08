package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VInt;

public class Sint32VType extends YamcsVType implements VInt {

    public Sint32VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Integer getValue() {
        return value.getSint32Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint32Value());
    }
}
