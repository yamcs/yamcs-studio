package org.yamcs.studio.core.vtype;

import org.epics.vtype.VString;
import org.yamcs.protobuf.Pvalue.ParameterValue;

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
