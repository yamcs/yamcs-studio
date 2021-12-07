/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

import java.util.List;

/**
 * Immutable VMultiDouble implementation.
 */
public class IVMultiDouble extends IVNumeric implements VMultiDouble {

    private final List<VDouble> values;

    IVMultiDouble(List<VDouble> values, Alarm alarm, Time time, Display display) {
        super(alarm, time, display);
        this.values = values;
    }

    @Override
    public List<VDouble> getValues() {
        return values;
    }
}
