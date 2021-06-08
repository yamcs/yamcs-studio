/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vtable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VTableFactory;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Selects the rows of the table for which the column value is within the range.
 */
class TableRangeArrayFilterFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "tableRangeFilter";
    }

    @Override
    public String getDescription() {
        return "Extract the rows where the column value is within the range [min, max)";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VTable.class, VString.class, VNumberArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("table", "columName", "arrayRange");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        VTable table = (VTable) args.get(0);
        VString columnName = (VString) args.get(1);
        VNumberArray range = (VNumberArray) args.get(2);

        if (range.getData().size() != 2) {
            throw new IllegalArgumentException("Range array must be of 2 elements");
        }

        VTable result = VTableFactory.tableRangeFilter(table, columnName.getValue(),
                ValueFactory.newVDouble(range.getData().getDouble(0)),
                ValueFactory.newVDouble(range.getData().getDouble(1)));

        return result;
    }

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
