package org.yamcs.studio.css.core.vtype;

import org.diirt.vtype.VString;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.StringConverter;

public class BinaryVType extends YamcsVType implements VString {

    public BinaryVType(ParameterValue pval) {
        super(pval);
    }

    @Override
    public String getValue() {
        byte[] barr = pval.getEngValue().getBinaryValue().toByteArray();
        return "0x" + StringConverter.arrayToHexString(barr);
    }

    @Override
    public String toString() {
        byte[] barr = pval.getEngValue().getBinaryValue().toByteArray();
        return "0x" + StringConverter.arrayToHexString(barr);
    }
}
