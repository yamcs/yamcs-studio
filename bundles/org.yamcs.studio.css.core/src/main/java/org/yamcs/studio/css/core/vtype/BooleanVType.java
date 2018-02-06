package org.yamcs.studio.css.core.vtype;

import org.diirt.vtype.VBoolean;
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
        return Boolean.toString(pval.getEngValue().getBooleanValue());
    }
}
