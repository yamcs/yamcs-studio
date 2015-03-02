package org.csstudio.platform.libs.yamcs.vtype;

import org.epics.vtype.VInt;
import org.yamcs.protostuff.ParameterValue;

public class Uint32VType extends YamcsVType implements VInt {

    public Uint32VType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Integer getValue() {
        return pval.getEngValue().getUint32Value();
    }
    
    @Override
    public String toString() {
        return String.valueOf(pval.getEngValue().getUint32Value());
    }
}
