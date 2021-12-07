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

public class IVFloatArray extends IVNumberArray implements VFloatArray {

    private final ListFloat data;

    public IVFloatArray(ListFloat data, ListInt sizes, Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVFloatArray(ListFloat data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListFloat getData() {
        return data;
    }
}
