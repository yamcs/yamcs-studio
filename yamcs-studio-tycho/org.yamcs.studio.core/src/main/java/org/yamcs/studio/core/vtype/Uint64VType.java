package org.yamcs.studio.core.vtype;

import java.math.BigInteger;

import org.epics.vtype.VLong;
import org.yamcs.protobuf.Pvalue.ParameterValue;

/**
 * Known defect: getValue will return negative numbers for UINT64 values that use the sign bit. Not
 * sure that there's a VType that would support this range of values.
 */
public class Uint64VType extends YamcsVType implements VLong {

    static final BigInteger B64 = BigInteger.ZERO.setBit(64);

    public Uint64VType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public Long getValue() {
        return pval.getEngValue().getUint64Value();
    }

    @Override
    public String toString() {
        if (pval.getEngValue().getUint64Value() >= 0)
            return Long.toString(pval.getEngValue().getUint64Value());
        else
            return BigInteger.valueOf(pval.getEngValue().getUint64Value()).add(B64).toString();
    }
}
