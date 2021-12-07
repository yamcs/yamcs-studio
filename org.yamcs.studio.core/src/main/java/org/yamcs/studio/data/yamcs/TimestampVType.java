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

import java.time.Instant;
import java.util.Date;

import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.VTimestamp;

public class TimestampVType extends YamcsVType implements VTimestamp {

    public TimestampVType(ParameterValue pval, boolean raw) {
        super(pval, raw);
    }

    @Override
    public Date getValue() {
        var stringValue = value.getStringValue();
        return Date.from(Instant.parse(stringValue));
    }

    @Override
    public String toString() {
        var stringValue = value.getStringValue();
        var instant = Instant.parse(stringValue);
        return YamcsPlugin.getDefault().formatInstant(instant);
    }
}
