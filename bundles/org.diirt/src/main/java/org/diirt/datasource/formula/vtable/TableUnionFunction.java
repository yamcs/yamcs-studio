/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.vtype.VString;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VTable;
import org.diirt.vtype.table.VTableFactory;

/**
 * Union of a set of tables.
 *
 * @author carcassi
 */
class TableUnionFunction implements FormulaFunction {

    @Override
    public boolean isPure() {
        return true;
    }

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "union";
    }

    @Override
    public String getDescription() {
        return "Union between tables";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VString.class, VStringArray.class, VTable.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("columnName", "columnValues", "tables");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VString columnName = (VString) args.get(0);
        VStringArray columnValues = (VStringArray) args.get(1);
        List<VTable> tables = new ArrayList<>();
        for (int i = 2; i < args.size(); i++) {
            Object object = args.get(i);
            tables.add((VTable) object);
        }

        return VTableFactory.union(columnName, columnValues, tables.toArray(new VTable[tables.size()]));
    }

}
