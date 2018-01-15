/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vstring;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.datasource.util.NullUtils;

import org.diirt.vtype.VString;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.ValueUtil;

/**
 * Function that concatenates the arguments into a single string.
 *
 * @author shroffk
 */
class ConcatStringsFunction implements FormulaFunction {

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
        if (NullUtils.containsNull(args)) {
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
                ValueUtil.highestSeverityOf(args, false),
                ValueUtil.latestValidTimeOrNowOf(args));

    }

}
