package org.csstudio.platform.libs.yamcs.vtype;

import org.epics.vtype.VString;
import org.yamcs.protobuf.ParameterValue;

public class StringVType extends YamcsVType implements VString {

    public StringVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public String getValue() {
        return pval.getEngValue().getStringValue();
    }
    
    @Override
    public String toString() {
        // Use String.valueOf, because it formats a nice "null" string
        // in case it is null
        return String.valueOf(pval.getEngValue().getStringValue());
    }
}
