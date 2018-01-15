/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.array;

import static org.diirt.vtype.ValueFactory.*;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.datasource.util.NullUtils;

import org.diirt.vtype.VNumber;
import org.diirt.vtype.VString;
import org.diirt.vtype.VStringArray;

/**
 * @author carcassi
 *
 */
class ElementAtStringFormulaFunction implements FormulaFunction {

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
        if (NullUtils.containsNull(args)) {
            return null;
        }

        VStringArray stringArray = (VStringArray) args.get(0);
        VNumber index = (VNumber) args.get(1);
        int i = index.getValue().intValue();

        return newVString(stringArray.getData().get(i),
                stringArray, stringArray);
    }

}
