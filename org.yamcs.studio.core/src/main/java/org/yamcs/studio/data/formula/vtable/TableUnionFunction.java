/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VTableFactory;

/**
 * Union of a set of tables
 */
class TableUnionFunction implements FormulaFunction {

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
        return Arrays.<Class<?>> asList(VString.class, VStringArray.class, VTable.class);
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
