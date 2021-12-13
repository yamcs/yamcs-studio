/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012, 2021 diirt developers and others
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    public Object calculate(List<Object> args) {
        var table = (VTable) args.get(0);
        var columnName = (VString) args.get(1);

        if (columnName == null || table == null) {
            return null;
        }

        var index = -1;
        for (var i = 0; i < table.getColumnCount(); i++) {
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
            var data = (List<String>) table.getColumnData(index);
            return ValueFactory.newVStringArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow());
        }

        if (Double.TYPE.isAssignableFrom(type)) {
            var data = (ListDouble) table.getColumnData(index);
            return ValueFactory.newVDoubleArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow(),
                    ValueFactory.displayNone());
        }

        if (Integer.TYPE.isAssignableFrom(type)) {
            var data = (ListInt) table.getColumnData(index);
            return ValueFactory.newVIntArray(data, ValueFactory.alarmNone(), ValueFactory.timeNow(),
                    ValueFactory.displayNone());
        }

        throw new RuntimeException("Unsupported type " + type.getSimpleName());
    }
}
