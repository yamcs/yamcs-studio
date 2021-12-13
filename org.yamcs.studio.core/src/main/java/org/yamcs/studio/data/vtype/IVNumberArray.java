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

public abstract class IVNumberArray extends IVNumeric implements VNumberArray {

    private final ListInt sizes;
    private final List<ArrayDimensionDisplay> dimensionDisplay;

    public IVNumberArray(ListInt sizes, List<ArrayDimensionDisplay> dimDisplay, Alarm alarm, Time time,
            Display display) {
        super(alarm, time, display);
        this.sizes = sizes;
        if (dimDisplay == null) {
            dimensionDisplay = ValueUtil.defaultArrayDisplay(sizes);
        } else {
            dimensionDisplay = dimDisplay;
        }
    }

    @Override
    public final ListInt getSizes() {
        return sizes;
    }

    @Override
    public final String toString() {
        return VTypeToString.toString(this);
    }

    @Override
    public final List<ArrayDimensionDisplay> getDimensionDisplay() {
        return dimensionDisplay;
    }
}
