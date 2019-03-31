package org.yamcs.studio.css.core.vtype;

import org.diirt.vtype.VString;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.utils.StringConverter;

public class AggregateVType extends YamcsVType implements VString {

    public AggregateVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public String getValue() {
        return StringConverter.toString(pval.getEngValue(), false);
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
