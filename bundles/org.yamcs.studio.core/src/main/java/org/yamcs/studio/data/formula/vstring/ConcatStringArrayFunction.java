/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vstring;

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.ValueFactory;

class ConcatStringArrayFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public String getDescription() {
        return "Concatenate the strings of the given string array ";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VStringArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("stringArray");
    }

    @Override
    public Class<?> getReturnType() {
        return VString.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        VStringArray stringArray = (VStringArray) args.get(0);

        if (stringArray == null) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        for (String str : stringArray.getData()) {
            sb.append(str);
        }
        return ValueFactory.newVString(sb.toString(),
                stringArray,
                stringArray);

    }

}
