/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.vstring;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Function that concatenates the arguments into a single string.
 */
class ConcatStringsFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public String getDescription() {
        return "Concatenate the strings";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VString.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("string");
    }

    @Override
    public Class<?> getReturnType() {
        return VString.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        // Handle nulls
        if (containsNull(args)) {
            return null;
        }

        // Concatenate strings
        StringBuilder sb = new StringBuilder();
        for (Object object : args) {
            VString str = (VString) object;
            sb.append(str.getValue());
        }

        // Return new value
        return ValueFactory.newVString(sb.toString(),
                highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args));
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
