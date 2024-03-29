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

public class ValueFilter {

    private final VTable table;
    private final int columnIndex;
    private final Object value;

    public ValueFilter(VTable table, String columnName, Object value) {
        this.table = table;
        columnIndex = VTableFactory.columnNames(table).indexOf(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Table does not contain column '" + columnName + "'");
        }
        Class<?> columnType = table.getColumnType(columnIndex);
        if (columnType.isPrimitive()) {
            if (!(value instanceof VNumber)) {
                throw new IllegalArgumentException(
                        "Column '" + columnName + "' is a number but not value '" + value + "'");
            }
        } else if (columnType.equals(String.class)) {
            if (!(value instanceof VString)) {
                throw new IllegalArgumentException(
                        "Column '" + columnName + "' is a string but not value '" + value + "'");
            }
        } else {
            throw new UnsupportedOperationException("Equal value filter only works on numbers and strings");
        }
        this.value = value;
    }

    public boolean filterRow(int rowIndex) {
        if (value instanceof VNumber) {
            var columnValue = ((ListNumber) table.getColumnData(columnIndex)).getDouble(rowIndex);
            return columnValue == ((VNumber) value).getValue().doubleValue();
        } else if (value instanceof VString) {
            @SuppressWarnings("unchecked")
            var columnData = (List<String>) table.getColumnData(columnIndex);
            return Objects.equals(columnData.get(rowIndex), ((VString) value).getValue());
        }
        throw new IllegalStateException("Unexpected error");
    }
}
