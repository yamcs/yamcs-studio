/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.yamcs;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.yamcs.protobuf.Yamcs.Value;

public class StringConverter {

    private static final BigInteger B64 = BigInteger.ZERO.setBit(64);

    public static String toString(Value rv) {
        switch (rv.getType()) {
        case BINARY:
            return "0x" + arrayToHexString(rv.getBinaryValue().toByteArray());
        case DOUBLE:
            return Double.toString(rv.getDoubleValue());
        case FLOAT:
            return Float.toString(rv.getFloatValue());
        case SINT32:
            return Integer.toString(rv.getSint32Value());
        case UINT32:
            return Long.toString(rv.getUint32Value() & 0xFFFFFFFFL);
        case SINT64:
            return Long.toString(rv.getSint64Value());
        case UINT64:
            if (rv.getUint64Value() >= 0) {
                return Long.toString(rv.getUint64Value());
            } else {
                return BigInteger.valueOf(rv.getUint64Value()).add(B64).toString();
            }
        case STRING:
            return rv.getStringValue();
        case BOOLEAN:
            return Boolean.toString(rv.getBooleanValue());
        case TIMESTAMP:
            return rv.getStringValue();
        case ENUMERATED:
            return rv.getStringValue();
        case ARRAY:
            return "[" + rv.getArrayValueList().stream().map(value -> toString(value)).collect(Collectors.joining(", "))
                    + "]";
        case AGGREGATE:
            var agg = rv.getAggregateValue();
            return "{" + IntStream.range(0, agg.getNameCount())
                    .mapToObj(i -> agg.getName(i) + ": " + toString(agg.getValue(i))).collect(Collectors.joining(", "))
                    + "}";
        }
        return null;
    }

    public static String arrayToHexString(byte[] b, int offset, int length) {
        return arrayToHexString(b, offset, length, false);
    }

    public static String arrayToHexString(byte[] b, int offset, int length, boolean beautify) {
        var sb = new StringBuilder();
        for (var i = offset; i < offset + length; i++) {
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

    public static String arrayToHexString(byte[] b) {
        return arrayToHexString(b, 0, b.length);
    }

    public static String arrayToHexString(byte[] b, boolean beautify) {
        return arrayToHexString(b, 0, b.length, beautify);
    }

    public static String byteBufferToHexString(ByteBuffer bb) {
        bb.mark();
        var sb = new StringBuilder();
        var offset = 0;
        while (bb.hasRemaining()) {
            if (offset % 33 == 0) {
                sb.append("\n");
            }
            var s = Integer.toString(bb.get() & 0xFF, 16);
            offset++;
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s.toUpperCase());
        }
        bb.reset();
        return sb.toString();
    }

    /**
     * Convert a hex string into a byte array. If the string has an odd number of hex digits, it is padded with 0 at the
     * <b>beginning</b>.
     * 
     * @param s
     *            - string to be converted
     * @return binary array representation of the hex string
     */
    public static byte[] hexStringToArray(String s) {
        if ((s.length() & 1) == 1) {
            s = "0" + s;
        }
        ;
        var b = new byte[s.length() >> 1];
        for (var i = 0; i < b.length; i++) {
            b[i] = (byte) (Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16) & 0xFF);
        }
        return b;
    }
}
