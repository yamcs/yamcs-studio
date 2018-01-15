/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vtable;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.vtype.VString;
import org.diirt.vtype.table.Column;
import org.diirt.vtype.table.ListNumberProvider;
import org.diirt.vtype.table.VTableFactory;

/**
 *
 * @author carcassi
 */
class ColumnFromListNumberGeneratorFunction implements FormulaFunction {

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
        return "column";
    }

    @Override
    public String getDescription() {
        return "Constructs column from a list number generator";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VString.class, ListNumberProvider.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("columnName", "numberGenerator");
    }

    @Override
    public Class<?> getReturnType() {
        return Column.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VString name = (VString) args.get(0);
        ListNumberProvider data = (ListNumberProvider) args.get(1);

        if (name == null || data == null) {
            return null;
        }

        return VTableFactory.column(name.getValue(), data);
    }

}
