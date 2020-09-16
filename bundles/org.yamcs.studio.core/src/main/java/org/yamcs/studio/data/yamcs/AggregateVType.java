package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VString;

public class AggregateVType extends YamcsVType implements VString {

    public AggregateVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public String getValue() {
        return StringConverter.toString(value);
    }

    @Override
    public String toString() {
        if (value.hasAggregateValue() || value.getAggregateValue() == null) {
            return "null";
        } else {
            return getValue();
        }
    }
}
