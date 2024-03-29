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

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VColumn {

    private final String name;
    private final Class<?> type;
    private final Object data;

    public VColumn(String name, Class<?> type, Object data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public static VColumn from(VTable table, int index) {
        return new VColumn(table.getColumnName(index), table.getColumnType(index), table.getColumnData(index));
    }

    public static VColumn from(VTable table, String column) {
        if (column == null || table == null) {
            return null;
        }
        for (var index = 0; index < table.getColumnCount(); index++) {
            if (column.equals(table.getColumnName(index))) {
                return new VColumn(table.getColumnName(index), table.getColumnType(index), table.getColumnData(index));
            }
        }
        return null;
    }

    public static Map<String, VColumn> columnMap(VTable table) {
        Map<String, VColumn> columns = new HashMap<>();
        for (var index = 0; index < table.getColumnCount(); index++) {
            columns.put(table.getColumnName(index), from(table, index));
        }
        return columns;
    }

    public static Object combineData(Class<?> type, int size, ListInt offsets, List<VColumn> columns) {
        if (String.class.equals(type)) {
            return combineStringData(size, offsets, columns);
        } else if (Double.TYPE.equals(type)) {
            return combineDoubleData(size, offsets, columns);
        }
        throw new UnsupportedOperationException("Type " + type + " not supported for column combineData");
    }

    private static Object combineStringData(int size, ListInt offsets, List<VColumn> columns) {
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                var tableIndex = ListNumbers.binarySearchValueOrLower(offsets, index);
                if (columns.get(tableIndex) == null) {
                    return null;
                }

                var rowIndex = index - offsets.getInt(tableIndex);
                // TODO: mismatched type should be handled better
                if (columns.get(tableIndex).getType() != String.class) {
                    return null;
                }
                @SuppressWarnings("unchecked")
                var values = (List<String>) columns.get(tableIndex).getData();
                if (rowIndex < values.size()) {
                    return values.get(rowIndex);
                } else {
                    return null;
                }
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    private static Object combineDoubleData(int size, ListInt offsets, List<VColumn> columns) {
        return new ListDouble() {
            @Override
            public double getDouble(int index) {
                var tableIndex = ListNumbers.binarySearchValueOrLower(offsets, index);
                if (columns.get(tableIndex) == null) {
                    return Double.NaN;
                }

                var rowIndex = index - offsets.getInt(tableIndex);
                // TODO: mismatched type should be handled better
                if (!(columns.get(tableIndex).getData() instanceof ListNumber)) {
                    return Double.NaN;
                }

                var values = (ListNumber) columns.get(tableIndex).getData();
                if (rowIndex < values.size()) {
                    return values.getDouble(rowIndex);
                } else {
                    return Double.NaN;
                }
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
}
