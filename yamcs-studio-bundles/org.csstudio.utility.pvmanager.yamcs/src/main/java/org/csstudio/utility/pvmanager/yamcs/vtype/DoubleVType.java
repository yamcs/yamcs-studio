package org.csstudio.utility.pvmanager.yamcs.vtype;

import org.epics.vtype.VDouble;
import org.yamcs.protobuf.ParameterValue;

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
        return String.valueOf(pval.getEngValue().getDoubleValue());
    }
}
