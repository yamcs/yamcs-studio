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

public class IVByteArray extends IVNumberArray implements VByteArray {

    private final ListByte data;

    public IVByteArray(ListByte data, ListInt sizes, Alarm alarm, Time time, Display display) {
        this(data, sizes, null, alarm, time, display);
    }

    public IVByteArray(ListByte data, ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(sizes, dimDisplay, alarm, time, display);
        this.data = data;
    }

    @Override
    public ListByte getData() {
        return data;
    }
}
