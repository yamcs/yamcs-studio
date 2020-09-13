/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVNumberArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ListMath;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;

class SubArrayFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "subArray";
    }

    @Override
    public String getDescription() {
        return "Result[] = [ array[fromIndex], ..., array[toIndex-1] ]";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, VNumber.class,
                VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array", "fromIndex", "toIndex");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        VNumberArray numberArray = (VNumberArray) args.get(0);
        int fromIndex = ((VNumber) args.get(1)).getValue().intValue();
        int toIndex = ((VNumber) args.get(2)).getValue().intValue();

        return newVNumberArray(
                ListMath.limit(numberArray.getData(), fromIndex, toIndex),
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
