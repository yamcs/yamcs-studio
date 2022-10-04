/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.datadefinition;

import java.util.Objects;

import org.eclipse.swt.graphics.RGB;

/**
 * A color Tuple which include a double value and rgb value.
 */
public class ColorTuple implements Comparable<ColorTuple> {
    public double value;
    public RGB rgb;

    public ColorTuple(double value, RGB rgb) {
        this.value = value;
        this.rgb = rgb;
    }

    @Override
    public int compareTo(ColorTuple o) {
        if (value < o.value) {
            return -1;
        } else if (equals(o)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgb, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (ColorTuple) obj;
        if (!Objects.equals(rgb, other.rgb)) {
            return false;
        }
        if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        return true;
    }
}
