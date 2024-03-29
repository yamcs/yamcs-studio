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

public class StringMatchFilter {
    private final VTable table;
    private final int columnIndex;
    private final String substring;

    public StringMatchFilter(VTable table, String columnName, String substring) {
        this.table = table;
        columnIndex = VTableFactory.columnNames(table).indexOf(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Table does not contain column '" + columnName + "'");
        }
        Class<?> columnType = table.getColumnType(columnIndex);
        if (!columnType.equals(String.class)) {
            throw new IllegalArgumentException("Column '" + columnName + "' is not a string");
        }
        this.substring = substring;
    }

    public boolean filterRow(int rowIndex) {
        @SuppressWarnings("unchecked")
        var columnData = (List<String>) table.getColumnData(columnIndex);
        return columnData.get(rowIndex).contains(substring);
    }
}
