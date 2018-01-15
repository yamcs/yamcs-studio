/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vnumber;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.vtype.VBoolean;


/**
 * Implementation for ?: operator.
 *
 * @author carcassi
 */
class ConditionalOperatorFormulaFunction implements FormulaFunction {

    @Override
    public String getName() {
        return "?:";
    }

    @Override
    public String getDescription() {
        return "Conditional operator";
    }

    @Override
    public boolean isPure() {
        return true;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VBoolean.class, Object.class, Object.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("condition", "valueIfTrue", "valueIfFalse");
    }

    @Override
    public Class<?> getReturnType() {
        return VBoolean.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        // Convert arguments to actual types
        VBoolean condition = (VBoolean) args.get(0);

        // Handle null case
        if (condition == null) {
            return null;
        }

        // Select return based on value
        Object value;
        if (condition.getValue()) {
            value = args.get(1);
        } else {
            value = args.get(2);
        }

        return value;
    }

}
