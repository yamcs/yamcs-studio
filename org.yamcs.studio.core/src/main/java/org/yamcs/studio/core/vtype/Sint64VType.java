package org.yamcs.studio.core.vtype;

import org.diirt.vtype.VLong;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class Sint64VType extends YamcsVType implements VLong {

    public Sint64VType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Long getValue() {
        return pval.getEngValue().getSint64Value();
    }

    @Override
    public String toString() {
        return String.valueOf(pval.getEngValue().getSint64Value());
    }
}
