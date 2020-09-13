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

class RescaleArrayFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "rescale";
    }

    @Override
    public String getDescription() {
        return "Rescale an array using the factor and offset";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, VNumber.class,
                VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array", "factor", "offset");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        VNumberArray arg1 = (VNumberArray) args.get(0);
        VNumber arg2 = (VNumber) args.get(1);
        VNumber arg3 = (VNumber) args.get(2);

        return newVNumberArray(
                ListMath.rescale(arg1.getData(), arg2.getValue().doubleValue(), arg3.getValue().doubleValue()),
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
