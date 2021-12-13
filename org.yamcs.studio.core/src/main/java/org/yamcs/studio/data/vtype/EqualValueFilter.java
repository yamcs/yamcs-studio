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
import java.util.Objects;

public class EqualValueFilter {

    private final List<VTable> tables;
    private final int[] columnIndexes;
    private final Class<?> type;

    public EqualValueFilter(List<VTable> tables, int[] columnIndexes) {
        this.tables = tables;
        this.columnIndexes = columnIndexes;
        Class<?> firstTableType = tables.get(0).getColumnType(columnIndexes[0]);
        if (firstTableType.isPrimitive()) {
            type = double.class;
        } else if (firstTableType.equals(String.class)) {
            type = String.class;
        } else {
            throw new UnsupportedOperationException("Natural join only supports numbers and Strings");
        }
        for (var i = 1; i < columnIndexes.length; i++) {
            Class<?> tableType = tables.get(i).getColumnType(columnIndexes[i]);
            if (type == double.class) {
                if (!tableType.isPrimitive()) {
                    throw new IllegalArgumentException("Column types must match for natural join");
                }
            }
            if (type == String.class) {
                if (tableType != String.class) {
                    throw new IllegalArgumentException("Column types must match for natural join");
                }
            }
        }
    }

    public boolean filterRow(int[] rowIndexes) {
        if (type == double.class) {
            var value = ((ListNumber) tables.get(0).getColumnData(columnIndexes[0])).getDouble(rowIndexes[0]);
            for (var i = 1; i < rowIndexes.length; i++) {
                if (value != ((ListNumber) tables.get(i).getColumnData(columnIndexes[i])).getDouble(rowIndexes[i])) {
                    return false;
                }
            }
        } else {
            var value = ((List) tables.get(0).getColumnData(columnIndexes[0])).get(rowIndexes[0]);
            for (var i = 1; i < rowIndexes.length; i++) {
                if (!Objects.equals(value, ((List) tables.get(i).getColumnData(columnIndexes[i])).get(rowIndexes[i]))) {
                    return false;
                }
            }
        }
        return true;
    }
}
