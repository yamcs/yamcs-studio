package org.yamcs.studio.css.core.vtype;

import java.time.Instant;
import java.util.Date;

import org.diirt.vtype.VTimestamp;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class TimestampVType extends YamcsVType implements VTimestamp {

    public TimestampVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Date getValue() {
        String stringValue = pval.getEngValue().getStringValue();
        return Date.from(Instant.parse(stringValue));
    }

    @Override
    public String toString() {
        String stringValue = pval.getEngValue().getStringValue();
        Instant instant = Instant.parse(stringValue);
        return YamcsUIPlugin.getDefault().formatInstant(instant);
    }
}
