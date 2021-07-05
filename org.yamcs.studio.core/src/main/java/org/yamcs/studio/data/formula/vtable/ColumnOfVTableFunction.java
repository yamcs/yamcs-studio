/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vtable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.ListInt;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Extracts a columns from a VTable.
 */
class ColumnOfVTableFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "columnOf";
    }

    @Override
    public String getDescription() {
        return "Extracts a column from the given table";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VTable.class, VString.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("table", "columName");
    }

    @Override
    public Class<?> getReturnType() {
        return VType.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VTable table = (VTable) args.get(0);
        VString columnName = (VString) args.get(1);

        if (columnName == null || table == null) {
            return null;
        }

        int index = -1;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (Objects.equals(columnName.getValue(), table.getColumnName(i))) {
                index = i;
            }
        }
        if (index == -1) {
            throw new RuntimeException("Table does not contain column '" + columnName.getValue() + "'");
        }

        Class<?> type = table.getColumnType(index);

        if (String.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            List<String> data = (List<String>) table.getColumnData(index);
            return ValueFactory.newVStringArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow());
        }

        if (Double.TYPE.isAssignableFrom(type)) {
            ListDouble data = (ListDouble) table.getColumnData(index);
            return ValueFactory.newVDoubleArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow(),
                    ValueFactory.displayNone());
        }

        if (Integer.TYPE.isAssignableFrom(type)) {
            ListInt data = (ListInt) table.getColumnData(index);
            return ValueFactory.newVIntArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow(),
                    ValueFactory.displayNone());
        }

        throw new RuntimeException("Unsupported type " + type.getSimpleName());
    }

}
