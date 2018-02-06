/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vtable;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.vtype.VString;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;

/**
 * Extracts a columns from a VTable.
 *
 * @author carcassi
 */
class TableStringMatchFilterFunction implements FormulaFunction {

    @Override
    public boolean isPure() {
        return true;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "tableStringMatchFilter";
    }

    @Override
    public String getDescription() {
        return "Extract the rows where the column value contains the given string";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VTable.class, VString.class, VString.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("table", "columName", "substring");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VTable table = (VTable) args.get(0);
        VString columnName = (VString) args.get(1);
        VString substring = (VString) args.get(2);

        if (columnName == null || columnName.getValue() == null || table == null || substring == null) {
            return null;
        }

        VTable result = VTableFactory.tableStringMatchFilter(table, columnName.getValue(), substring.getValue());

        return result;
    }

}
