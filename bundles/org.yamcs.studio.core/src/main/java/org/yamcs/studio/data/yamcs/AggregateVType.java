package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VString;

public class AggregateVType extends YamcsVType implements VString {

    public AggregateVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public String getValue() {
        return StringConverter.toString(pval.getEngValue());
    }

    @Override
    public String toString() {
        if (pval.getEngValue().hasAggregateValue() || pval.getEngValue().getAggregateValue() == null) {
            return "null";
        } else {
            return getValue();
        }
    }
}
