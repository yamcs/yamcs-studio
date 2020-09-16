package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VLong;

public class Sint64VType extends YamcsVType implements VLong {

    public Sint64VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Long getValue() {
        return value.getSint64Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint64Value());
    }
}
