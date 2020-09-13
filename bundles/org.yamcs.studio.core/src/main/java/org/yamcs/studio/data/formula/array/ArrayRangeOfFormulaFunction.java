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
import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.VNumberArray;

class ArrayRangeOfFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "arrayRangeOf";
    }

    @Override
    public String getDescription() {
        return "Returns the range where the array is defined";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array");
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
        double min = numberArray.getDimensionDisplay().get(0).getCellBoundaries().getDouble(0);
        double max = numberArray.getDimensionDisplay().get(0).getCellBoundaries()
                .getDouble(numberArray.getSizes().getInt(0));

        return newVNumberArray(
                new ArrayDouble(min, max),
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
