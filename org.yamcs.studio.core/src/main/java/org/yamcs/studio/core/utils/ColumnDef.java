/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.utils;

import java.util.Objects;

public class ColumnDef {
    public final String name;
    final int keyIndex;

    public int newIndex;
    public int width;

    public boolean visible;
    public boolean moveable;
    public boolean resizable;

    public ColumnDef(String name, int currIndex) {
        this.name = name;
        keyIndex = currIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, keyIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColumnDef)) {
            return false;
        }
        var other = (ColumnDef) obj;
        return Objects.equals(name, other.name) && Objects.equals(keyIndex, other.keyIndex);
    }

    @Override
    public String toString() {
        return name;
    }
}
