package org.yamcs.studio.data.yamcs;

import org.yamcs.studio.data.vtype.VInt;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Sint32VType extends YamcsVType implements VInt {

    public Sint32VType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Integer getValue() {
        return pval.getEngValue().getSint32Value();
    }

    @Override
    public String toString() {
        return String.valueOf(pval.getEngValue().getSint32Value());
    }
}
