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

public class RangeFilter {

    private final VTable table;
    private final int columnIndex;
    private final Object min;
    private final Object max;

    public RangeFilter(VTable table, String columnName, Object min, Object max) {
        this.table = table;
        columnIndex = VTableFactory.columnNames(table).indexOf(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Table does not contain column '" + columnName + "'");
        }
        Class<?> columnType = table.getColumnType(columnIndex);
        if (columnType.isPrimitive()) {
            if (!(min instanceof VNumber && max instanceof VNumber)) {
                throw new IllegalArgumentException(
                        "Column '" + columnName + "' is a number but not boundaries '" + min + "' and '" + max + "'");
            }
        } else if (columnType.equals(String.class)) {
            if (!(min instanceof VString && max instanceof VString)) {
                throw new IllegalArgumentException(
                        "Column '" + columnName + "' is a string but not boundaries '" + min + "' and '" + max + "'");
            }
        } else {
            throw new UnsupportedOperationException("Equal value filter only works on numbers and strings");
        }
        this.min = min;
        this.max = max;
    }

    public boolean filterRow(int rowIndex) {
        if (min instanceof VNumber) {
            var columnValue = ((ListNumber) table.getColumnData(columnIndex)).getDouble(rowIndex);
            var minValue = ((VNumber) min).getValue().doubleValue();
            var maxValue = ((VNumber) max).getValue().doubleValue();
            return columnValue >= minValue && columnValue < maxValue;
        } else if (min instanceof VString) {
            @SuppressWarnings("unchecked")
            var columnData = (List<String>) table.getColumnData(columnIndex);
            var columnValue = columnData.get(rowIndex);
            var minValue = ((VString) min).getValue();
            var maxValue = ((VString) max).getValue();
            return minValue.compareTo(columnValue) <= 0 && maxValue.compareTo(columnValue) > 0;
        }
        throw new IllegalStateException("Unexpected error");
    }
}
