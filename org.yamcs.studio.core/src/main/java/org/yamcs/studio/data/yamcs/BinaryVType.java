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

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VString;

public class BinaryVType extends YamcsVType implements VString {

    public BinaryVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public String getValue() {
        var barr = value.getBinaryValue().toByteArray();
        return "0x" + arrayToHexString(barr, 0, barr.length, false);
    }

    @Override
    public String toString() {
        var barr = value.getBinaryValue().toByteArray();
        return "0x" + arrayToHexString(barr, 0, barr.length, false);
    }

    private static String arrayToHexString(byte[] b, int offset, int length, boolean beautify) {
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
}
