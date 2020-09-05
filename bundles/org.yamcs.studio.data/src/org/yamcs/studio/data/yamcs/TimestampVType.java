package org.yamcs.studio.data.yamcs;

import java.time.Instant;
import java.util.Date;

import org.yamcs.studio.data.vtype.VTimestamp;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.YamcsPlugin;

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
        return YamcsPlugin.getDefault().formatInstant(instant);
    }
}
