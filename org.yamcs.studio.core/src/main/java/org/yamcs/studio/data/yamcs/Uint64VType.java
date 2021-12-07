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

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.data.vtype.VLong;

/**
 * Known defect: getValue will return negative numbers for UINT64 values that use the sign bit. Not sure that there's a
 * VType that would support this range of values.
 */
public class Uint64VType extends YamcsVType implements VLong {

    static final BigInteger B64 = BigInteger.ZERO.setBit(64);

    public Uint64VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Long getValue() {
        return value.getUint64Value();
    }

    @Override
    public String toString() {
        if (value.getUint64Value() >= 0) {
            return Long.toString(value.getUint64Value());
        } else {
            return BigInteger.valueOf(value.getUint64Value()).add(B64).toString();
        }
    }
}
