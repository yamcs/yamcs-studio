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

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVDoubleArray;
import static org.yamcs.studio.data.vtype.ValueFactory.newVStringArray;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.time.Instant;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VTableFactory {

    public static VTable join(List<VTable> tables) {
        return join(tables.toArray(new VTable[tables.size()]));
    }

    public static VTable join(VTable... tables) {
        if (tables.length == 0) {
            return null;
        }

        if (tables.length == 1) {
            return tables[0];
        }

        // Find columns to join
        Map<String, int[]> commonColumnsIndexes = null;
        for (var nTable = 0; nTable < tables.length; nTable++) {
            var vTable = tables[nTable];
            if (commonColumnsIndexes == null) {
                commonColumnsIndexes = new HashMap<>();
                for (var i = 0; i < vTable.getColumnCount(); i++) {
                    var indexes = new int[tables.length];
                    indexes[0] = i;
                    commonColumnsIndexes.put(vTable.getColumnName(i), indexes);

                }
            } else {
                commonColumnsIndexes.keySet().retainAll(columnNames(vTable));
                for (var i = 0; i < vTable.getColumnCount(); i++) {
                    if (commonColumnsIndexes.keySet().contains(vTable.getColumnName(i))) {
                        commonColumnsIndexes.get(vTable.getColumnName(i))[nTable] = i;
                    }
                }
            }
        }

        if (commonColumnsIndexes.isEmpty()) {
            throw new UnsupportedOperationException("Case not implemented yet");
        }

        var filters = new ArrayList<EqualValueFilter>();
        for (var entry : commonColumnsIndexes.entrySet()) {
            var indexes = entry.getValue();
            filters.add(new EqualValueFilter(Arrays.asList(tables), indexes));
        }

        // Find rows
        var done = false;
        List<BufferInt> rowIndexes = new ArrayList<>();
        for (var i = 0; i < tables.length; i++) {
            rowIndexes.add(new BufferInt());
            if (tables[i].getRowCount() == 0) {
                done = true;
            }
        }
        var currentIndexes = new int[tables.length];
        while (!done) {
            var match = true;
            for (var filter : filters) {
                match = match && filter.filterRow(currentIndexes);
            }
            if (match) {
                for (var i = 0; i < currentIndexes.length; i++) {
                    rowIndexes.get(i).addInt(currentIndexes[i]);
                }
            }
            var needsIncrement = true;
            var offset = currentIndexes.length - 1;
            while (needsIncrement) {
                currentIndexes[offset]++;
                if (currentIndexes[offset] == tables[offset].getRowCount()) {
                    currentIndexes[offset] = 0;
                    offset--;
                    if (offset == -1) {
                        done = true;
                        needsIncrement = false;
                    }
                } else {
                    needsIncrement = false;
                }
            }
        }

        var columnNames = new ArrayList<String>();
        var columnTypes = new ArrayList<Class<?>>();
        var columnData = new ArrayList<>();
        for (var nColumn = 0; nColumn < tables[0].getColumnCount(); nColumn++) {
            columnNames.add(tables[0].getColumnName(nColumn));
            Class<?> type = tables[0].getColumnType(nColumn);
            if (type.isPrimitive()) {
                columnTypes.add(type);
                columnData.add(ListNumbers.listView((ListNumber) tables[0].getColumnData(nColumn), rowIndexes.get(0)));
            } else {
                columnTypes.add(type);
                columnData.add(createView((List<?>) tables[0].getColumnData(nColumn), rowIndexes.get(0)));
            }
        }
        for (var i = 1; i < tables.length; i++) {
            var vTable = tables[i];
            for (var nColumn = 0; nColumn < vTable.getColumnCount(); nColumn++) {
                if (!commonColumnsIndexes.containsKey(vTable.getColumnName(nColumn))) {
                    columnNames.add(vTable.getColumnName(nColumn));
                    Class<?> type = vTable.getColumnType(nColumn);
                    if (type.isPrimitive()) {
                        columnTypes.add(type);
                        columnData.add(
                                ListNumbers.listView((ListNumber) vTable.getColumnData(nColumn), rowIndexes.get(i)));
                    } else {
                        columnTypes.add(type);
                        columnData.add(createView((List<?>) vTable.getColumnData(nColumn), rowIndexes.get(i)));
                    }
                }
            }
        }

        return ValueFactory.newVTable(columnTypes, columnNames, columnData);
    }

    public static VTable union(VString extraColumnName, VStringArray extraColumnData, VTable... tables) {
        // Prune nulls
        var extraColumnDataPruned = new ArrayList<String>();
        var tablesPruned = new ArrayList<VTable>();

        for (var i = 0; i < tables.length; i++) {
            var table = tables[i];
            if (table != null) {
                extraColumnDataPruned.add(extraColumnData.getData().get(i));
                tablesPruned.add(table);
            }
        }

        if (tablesPruned.isEmpty()) {
            return null;
        }

        var columnNames = new ArrayList<String>();
        var columnTypes = new ArrayList<Class<?>>();
        var tableColumns = new ArrayList<Map<String, VColumn>>();
        if (extraColumnName != null) {
            columnNames.add(extraColumnName.getValue());
            columnTypes.add(String.class);
        }
        var tableOffsets = new int[tablesPruned.size()];
        var currentOffset = 0;
        for (var k = 0; k < tablesPruned.size(); k++) {
            var table = tablesPruned.get(k);
            if (table == null) {
                continue;
            }
            tableOffsets[k] = currentOffset;
            currentOffset += table.getRowCount();
            tableColumns.add(VColumn.columnMap(table));
            for (var i = 0; i < table.getColumnCount(); i++) {
                var name = table.getColumnName(i);
                if (!columnNames.contains(name)) {
                    columnNames.add(name);
                    columnTypes.add(table.getColumnType(i));
                }
            }
        }
        var rowCount = currentOffset;
        ListInt offsets = new ArrayInt(tableOffsets);

        var columnData = new ArrayList<>();
        if (extraColumnName != null) {
            columnData.add(new AbstractList<String>() {
                @Override
                public String get(int index) {
                    var nTable = ListNumbers.binarySearchValueOrLower(offsets, index);
                    return extraColumnDataPruned.get(nTable);
                }

                @Override
                public int size() {
                    return rowCount;
                }
            });
        }

        for (var i = 1; i < columnNames.size(); i++) {
            var columnName = columnNames.get(i);
            var columns = new ArrayList<VColumn>();
            for (var j = 0; j < tableColumns.size(); j++) {
                columns.add(tableColumns.get(j).get(columnName));
            }
            columnData.add(VColumn.combineData(columnTypes.get(i), rowCount, offsets, columns));
        }

        return ValueFactory.newVTable(columnTypes, columnNames, columnData);
    }

    private static Object selectColumnData(VTable table, int column, ListInt indexes) {
        var type = table.getColumnType(column);
        if (type.isPrimitive()) {
            return ListNumbers.listView((ListNumber) table.getColumnData(column), indexes);
        } else {
            return createView((List<?>) table.getColumnData(column), indexes);
        }
    }

    private static <T> List<T> createView(List<T> list, ListInt indexes) {
        return new AbstractList<>() {
            @Override
            public T get(int index) {
                return list.get(indexes.getInt(index));
            }

            @Override
            public int size() {
                return indexes.size();
            }
        };
    }

    private static Object createView(Object columnData, ListInt indexes) {
        if (columnData instanceof List) {
            var data = (List<?>) columnData;
            return createView(data, indexes);
        } else if (columnData instanceof ListNumber) {
            return ListNumbers.listView((ListNumber) columnData, indexes);
        } else {
            throw new IllegalArgumentException("Unsupported column data " + columnData);
        }
    }

    public static VTable select(VTable table, ListInt indexes) {
        var names = columnNames(table);
        var types = columnTypes(table);
        var data = new AbstractList<>() {
            @Override
            public Object get(int index) {
                return selectColumnData(table, index, indexes);
            }

            @Override
            public int size() {
                return table.getColumnCount();
            }
        };
        return ValueFactory.newVTable(types, names, data);
    }

    public static VTable newVTable(Column... columns) {
        List<String> columnNames = new ArrayList<>();
        columnNames.addAll(Collections.<String> nCopies(columns.length, null));
        List<Class<?>> columnTypes = new ArrayList<>();
        columnTypes.addAll(Collections.<Class<?>> nCopies(columns.length, null));
        List<Object> columnData = new ArrayList<>();
        columnData.addAll(Collections.nCopies(columns.length, null));

        var size = -1;
        // First add all the static columns
        for (var i = 0; i < columns.length; i++) {
            var column = columns[i];
            if (!column.isGenerated()) {
                var data = column.getData(size);
                size = sizeOf(data);
                columnNames.set(i, column.getName());
                columnTypes.set(i, column.getType());
                columnData.set(i, data);
            }
        }

        if (size == -1) {
            throw new IllegalArgumentException("At least one column must be of a defined size");
        }

        // Then add all the generated columns
        for (var i = 0; i < columns.length; i++) {
            var column = columns[i];
            if (column.isGenerated()) {
                var data = column.getData(size);
                columnNames.set(i, column.getName());
                columnTypes.set(i, column.getType());
                columnData.set(i, data);
            }
        }

        return ValueFactory.newVTable(columnTypes, columnNames, columnData);
    }

    private static int sizeOf(Object data) {
        if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        } else {
            return ((List<?>) data).size();
        }
    }

    public static Column column(String name, VNumberArray numericArray) {
        // TODO: for now rewrapping in VDouble. Will need to make table work with
        // all primitive types so that this is not an issue

        ListDouble data;
        if (numericArray.getData() instanceof ListDouble) {
            data = (ListDouble) numericArray.getData();
        } else {
            data = new ListDouble() {
                @Override
                public double getDouble(int index) {
                    return numericArray.getData().getDouble(index);
                }

                @Override
                public int size() {
                    return numericArray.getData().size();
                }
            };
        }

        return new Column(name, double.class, false) {
            @Override
            public Object getData(int size) {
                if (size >= 0) {
                    if (size != data.size()) {
                        throw new IllegalArgumentException("Column size does not match the others (this is "
                                + data.size() + " previous is " + size);
                    }
                }
                return data;
            }
        };
    }

    public static Column column(String name, VStringArray stringArray) {
        var data = stringArray.getData();

        return new Column(name, String.class, false) {
            @Override
            public Object getData(int size) {
                if (size >= 0) {
                    if (size != data.size()) {
                        throw new IllegalArgumentException("Column size does not match the others (this is "
                                + data.size() + " previous is " + size);
                    }
                }
                return data;
            }
        };
    }

    public static Column column(String name, ListNumberProvider dataProvider) {
        return new Column(name, dataProvider.getType(), true) {
            @Override
            public Object getData(int size) {
                return dataProvider.createListNumber(size);
            }
        };
    }

    public static ListNumberProvider range(double min, double max) {
        return new Range(min, max);
    }

    private static class Range extends ListNumberProvider {

        private double min;
        private double max;

        public Range(double min, double max) {
            super(double.class);
            this.min = min;
            this.max = max;
        }

        @Override
        public ListNumber createListNumber(int size) {
            return ListNumbers.linearListFromRange(min, max, size);
        }
    }

    public static ListNumberProvider step(double initialValue, double increment) {
        return new Step(initialValue, increment);
    }

    private static class Step extends ListNumberProvider {

        private double initialValue;
        private double increment;

        public Step(double initialValue, double increment) {
            super(double.class);
            this.initialValue = initialValue;
            this.increment = increment;
        }

        @Override
        public ListNumber createListNumber(int size) {
            return ListNumbers.linearList(initialValue, increment, size);
        }
    }

    public static VTable extractRow(VTable vTable, int row) {
        if (vTable == null || row >= vTable.getRowCount() || row < 0) {
            return null;
        }
        var columnNames = new ArrayList<String>(vTable.getColumnCount());
        var columnTypes = new ArrayList<Class<?>>(vTable.getColumnCount());
        var columnData = new ArrayList<>(vTable.getColumnCount());
        for (var nCol = 0; nCol < vTable.getColumnCount(); nCol++) {
            columnNames.add(vTable.getColumnName(nCol));
            columnTypes.add(vTable.getColumnType(nCol));
            columnData.add(extractColumnData(vTable.getColumnData(nCol), row));
        }
        return ValueFactory.newVTable(columnTypes, columnNames, columnData);
    }

    private static VTable extractRows(VTable vTable, ListInt indexes) {
        if (vTable == null || indexes == null) {
            return null;
        }
        var columnNames = new ArrayList<String>(vTable.getColumnCount());
        var columnTypes = new ArrayList<Class<?>>(vTable.getColumnCount());
        var columnData = new ArrayList<>(vTable.getColumnCount());
        for (var nCol = 0; nCol < vTable.getColumnCount(); nCol++) {
            columnNames.add(vTable.getColumnName(nCol));
            columnTypes.add(vTable.getColumnType(nCol));
            columnData.add(createView(vTable.getColumnData(nCol), indexes));
        }
        return ValueFactory.newVTable(columnTypes, columnNames, columnData);
    }

    private static Object extractColumnData(Object columnData, int... rows) {
        if (columnData instanceof List) {
            var data = new ArrayList<>(rows.length);
            for (var i = 0; i < rows.length; i++) {
                var j = rows[i];
                data.add(((List<?>) columnData).get(j));
            }
            return data;
        } else if (columnData instanceof ListNumber) {
            var data = new double[rows.length];
            for (var i = 0; i < rows.length; i++) {
                var j = rows[i];
                data[i] = ((ListNumber) columnData).getDouble(j);
            }
            return new ArrayDouble(data);
        }
        return null;
    }

    public static List<String> columnNames(VTable vTable) {
        return new AbstractList<>() {
            @Override
            public String get(int index) {
                return vTable.getColumnName(index);
            }

            @Override
            public int size() {
                return vTable.getColumnCount();
            }
        };
    }

    public static List<Class<?>> columnTypes(VTable vTable) {
        return new AbstractList<>() {
            @Override
            public Class<?> get(int index) {
                return vTable.getColumnType(index);
            }

            @Override
            public int size() {
                return vTable.getColumnCount();
            }
        };
    }

    public static VTable valueTable(List<? extends VType> values) {
        return valueTable(null, values);
    }

    public static VTable valueTable(List<String> names, List<? extends VType> values) {
        var nullValue = values.indexOf(null);
        if (nullValue != -1) {
            values = new ArrayList<>(values);
            if (names != null) {
                names = new ArrayList<>(names);
            }
            for (var i = values.size() - 1; i >= 0; i--) {
                if (values.get(i) == null) {
                    values.remove(i);
                    if (names != null) {
                        names.remove(i);
                    }
                }
            }
        }

        if (values.isEmpty()) {
            return valueNumberTable(names, values);
        }

        if (values.get(0) instanceof VNumber) {
            for (var vType : values) {
                if (!(vType instanceof VNumber)) {
                    throw new IllegalArgumentException(
                            "Values do not match (VNumber and " + ValueUtil.typeOf(vType).getSimpleName());
                }
            }
            return valueNumberTable(names, values);
        }

        throw new IllegalArgumentException(
                "Type " + ValueUtil.typeOf(values.get(0)).getSimpleName() + " not supported for value table");
    }

    private static VTable valueNumberTable(List<String> names, List<? extends VType> values) {
        var data = new double[values.size()];
        var severity = new ArrayList<String>();
        var status = new ArrayList<String>();

        for (var i = 0; i < values.size(); i++) {
            var vNumber = (VNumber) values.get(i);
            data[i] = vNumber.getValue().doubleValue();
            severity.add(vNumber.getAlarmSeverity().name());
            status.add(vNumber.getAlarmName());
        }

        if (names == null) {
            return newVTable(
                    column("Value", newVDoubleArray(new ArrayDouble(data), alarmNone(), timeNow(), displayNone())),
                    column("Severity", newVStringArray(severity, alarmNone(), timeNow())),
                    column("Status", newVStringArray(status, alarmNone(), timeNow())));
        } else {
            return newVTable(column("Name", newVStringArray(names, alarmNone(), timeNow())),
                    column("Value", newVDoubleArray(new ArrayDouble(data), alarmNone(), timeNow(), displayNone())),
                    column("Severity", newVStringArray(severity, alarmNone(), timeNow())),
                    column("Status", newVStringArray(status, alarmNone(), timeNow())));
        }
    }

    public static VTable tableValueFilter(VTable table, String columnName, Object value) {
        var valueFilter = new ValueFilter(table, columnName, value);
        var indexes = new BufferInt();
        for (var i = 0; i < table.getRowCount(); i++) {
            if (valueFilter.filterRow(i)) {
                indexes.addInt(i);
            }
        }

        return extractRows(table, indexes);
    }

    public static VTable tableStringMatchFilter(VTable table, String columnName, String substring) {
        var filter = new StringMatchFilter(table, columnName, substring);
        var indexes = new BufferInt();
        for (var i = 0; i < table.getRowCount(); i++) {
            if (filter.filterRow(i)) {
                indexes.addInt(i);
            }
        }

        return extractRows(table, indexes);
    }

    public static VTable tableRangeFilter(VTable table, String columnName, Object min, Object max) {
        var valueFilter = new RangeFilter(table, columnName, min, max);
        var indexes = new BufferInt();
        for (var i = 0; i < table.getRowCount(); i++) {
            if (valueFilter.filterRow(i)) {
                indexes.addInt(i);
            }
        }

        return extractRows(table, indexes);
    }

    public static void validateTable(VTable vTable) {
        for (var i = 0; i < vTable.getColumnCount(); i++) {
            Class<?> type = null;
            String name = null;
            Object data = null;

            try {
                type = vTable.getColumnType(i);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Can't get column " + i + " type", ex);
            }

            try {
                name = vTable.getColumnName(i);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Can't get column " + i + " name", ex);
            }

            try {
                data = vTable.getColumnData(i);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Can't get column " + i + " data", ex);
            }

            if (String.class.equals(type)) {
                if (!(data instanceof List)) {
                    throw new IllegalArgumentException(
                            "Data for column " + i + " (" + name + ") is not a List<String> (" + data + ")");
                }
            } else if (type.equals(double.class) || type.equals(float.class) || type.equals(long.class)
                    || type.equals(int.class) || type.equals(short.class) || type.equals(byte.class)) {
                if (!(data instanceof ListNumber)) {
                    throw new IllegalArgumentException(
                            "Data for column " + i + " (" + name + ") is not a ListNumber (" + data + ")");
                }
            } else if (type.equals(Instant.class)) {
                if (!(data instanceof List)) {
                    throw new IllegalArgumentException(
                            "Data for column " + i + " (" + name + ") is not a List<Timestamp> (" + data + ")");
                }
            } else {
                throw new IllegalArgumentException("Column type " + type.getSimpleName() + " not supported");
            }
        }
    }
}
