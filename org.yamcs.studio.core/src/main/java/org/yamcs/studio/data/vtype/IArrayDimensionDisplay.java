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

import java.util.Objects;

public class IArrayDimensionDisplay implements ArrayDimensionDisplay {

    private final ListNumber cellBoundaries;
    private final boolean reversed;
    private final String units;

    public IArrayDimensionDisplay(ListNumber cellBoundaries, boolean reversed, String units) {
        this.cellBoundaries = cellBoundaries;
        this.reversed = reversed;
        this.units = units;
    }

    @Override
    public ListNumber getCellBoundaries() {
        return cellBoundaries;
    }

    public int getSize() {
        return getCellBoundaries().size() - 1;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public boolean isReversed() {
        return reversed;
    }

    @Override
    public int hashCode() {
        return getCellBoundaries().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IArrayDimensionDisplay) {
            var info = (IArrayDimensionDisplay) obj;
            return Objects.equals(info.getSize(), getSize()) && Objects.equals(info.isReversed(), isReversed())
                    && Objects.equals(info.getUnits(), getUnits())
                    && Objects.equals(info.getCellBoundaries(), getCellBoundaries());
        }

        return false;
    }
}
