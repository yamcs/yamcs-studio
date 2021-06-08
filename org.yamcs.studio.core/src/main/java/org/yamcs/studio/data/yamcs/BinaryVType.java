package org.yamcs.studio.data.yamcs;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VString;

public class BinaryVType extends YamcsVType implements VString {

    public BinaryVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public String getValue() {
        byte[] barr = value.getBinaryValue().toByteArray();
        return "0x" + arrayToHexString(barr, 0, barr.length, false);
    }

    @Override
    public String toString() {
        byte[] barr = value.getBinaryValue().toByteArray();
        return "0x" + arrayToHexString(barr, 0, barr.length, false);
    }

    private static String arrayToHexString(byte[] b, int offset, int length, boolean beautify) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            if (beautify && (i - offset) % 32 == 0) {
                sb.append(String.format("\n0x%04X: ", (i - offset)));
            }
            sb.append(String.format("%02X", b[i] & 0xFF));
            /*String s = Integer.toString(b[i] & 0xFF, 16);
            if (s.length() == 1) {
                s = "0" + s;
            }
            sb.append(s.toUpperCase());*/
            if (beautify && (i - offset) % 2 == 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
