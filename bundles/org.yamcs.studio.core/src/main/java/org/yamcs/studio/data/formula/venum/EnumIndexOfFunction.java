/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.venum;

import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVInt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VNumber;

class EnumIndexOfFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "indexOf";
    }

    @Override
    public String getDescription() {
        return "Gets the index of a VEnum";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VEnum.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("enum");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumber.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }
        // args[0] is a VEnum
        VEnum value = (VEnum) args.get(0);
        return newVInt(value.getIndex(),
                highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args),
                displayNone());
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
