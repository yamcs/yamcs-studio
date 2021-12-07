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
import org.yamcs.studio.data.vtype.VDouble;

public class DoubleVType extends YamcsVType implements VDouble {

    public DoubleVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Double getValue() {
        return value.getDoubleValue();
    }

    @Override
    public String toString() {
        return Double.toString(value.getDoubleValue());
    }
}
