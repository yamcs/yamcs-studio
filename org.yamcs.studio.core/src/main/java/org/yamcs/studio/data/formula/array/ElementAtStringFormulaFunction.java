/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import static org.yamcs.studio.data.vtype.ValueFactory.newVString;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VStringArray;

class ElementAtStringFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "elementAt";
    }

    @Override
    public String getDescription() {
        return "Returns the element at the specified position in the string array.";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VStringArray.class, VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array", "index");
    }

    @Override
    public Class<?> getReturnType() {
        return VString.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        VStringArray stringArray = (VStringArray) args.get(0);
        VNumber index = (VNumber) args.get(1);
        int i = index.getValue().intValue();

        return newVString(stringArray.getData().get(i),
                stringArray, stringArray);
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
