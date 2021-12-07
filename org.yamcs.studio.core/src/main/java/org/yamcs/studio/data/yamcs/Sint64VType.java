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
import org.yamcs.studio.data.vtype.VLong;

public class Sint64VType extends YamcsVType implements VLong {

    public Sint64VType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Long getValue() {
        return value.getSint64Value();
    }

    @Override
    public String toString() {
        return String.valueOf(value.getSint64Value());
    }
}
