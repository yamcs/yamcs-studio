package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VLong;

public class Uint32VType extends YamcsVType implements VLong {

    public Uint32VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Long getValue() {
        return value.getUint32Value() & 0xFFFFFFFFL;
    }

    @Override
    public String toString() {
        return Long.toString(value.getUint32Value() & 0xFFFFFFFFL);
    }
}
