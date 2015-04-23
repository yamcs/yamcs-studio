package org.yamcs.studio.client.vtype;

import org.epics.vtype.VBoolean;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class BooleanVType extends YamcsVType implements VBoolean {

    public BooleanVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Boolean getValue() {
        return pval.getEngValue().getBooleanValue();
    }

    @Override
    public String toString() {
        return String.valueOf(pval.getEngValue().getBooleanValue());
    }
}
