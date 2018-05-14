package org.yamcs.studio.css.core.vtype;

import java.util.Date;

import org.diirt.vtype.VTimestamp;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.utils.TimeEncoding;

public class TimestampVType extends YamcsVType implements VTimestamp {

    public TimestampVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Date getValue() {
        String stringValue = pval.getEngValue().getStringValue();
        return YamcsUTCString.parse(stringValue);
    }

    @Override
    public String toString() {
        Date dt = getValue();
        long instant = TimeEncoding.fromUnixMillisec(dt.getTime());
        return YamcsUIPlugin.getDefault().formatInstant(instant);
    }
}
