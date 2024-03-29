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

public class IVTable implements VTable {

    private final List<Class<?>> types;
    private final List<String> names;
    private final List<Object> values;
    private final int rowCount;

    public IVTable(List<Class<?>> types, List<String> names, List<Object> values) {
        this.types = types;
        this.names = names;
        this.values = values;
        var maxCount = 0;
        for (var array : values) {
            maxCount = Math.max(maxCount, getDataSize(array));
        }
        rowCount = maxCount;
    }

    private static int getDataSize(Object data) {
        if (data instanceof List) {
            return ((List<?>) data).size();
        } else if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        }

        throw new IllegalArgumentException("Object " + data + " is not supported");
    }

    @Override
    public int getColumnCount() {
        return names.size();
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Class<?> getColumnType(int column) {
        return types.get(column);
    }

    @Override
    public String getColumnName(int column) {
        return names.get(column);
    }

    @Override
    public Object getColumnData(int column) {
        return values.get(column);
    }

    @Override
    public String toString() {
        return VTypeToString.toString(this);
    }
}
