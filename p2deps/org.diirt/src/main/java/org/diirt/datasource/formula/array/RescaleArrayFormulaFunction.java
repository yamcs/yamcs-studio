/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.array;

import static org.diirt.vtype.ValueFactory.displayNone;
import static org.diirt.vtype.ValueFactory.newVNumberArray;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.datasource.util.NullUtils;

import org.diirt.util.array.ListMath;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.ValueUtil;

/**
 * @author shroffk
 *
 */
class RescaleArrayFormulaFunction implements FormulaFunction {

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
        if (NullUtils.containsNull(args)) {
            return null;
        }

        VNumberArray arg1 = (VNumberArray) args.get(0);
        VNumber arg2 = (VNumber) args.get(1);
        VNumber arg3 = (VNumber) args.get(2);

        return newVNumberArray(
                ListMath.rescale(arg1.getData(), arg2.getValue().doubleValue(), arg3.getValue().doubleValue()),
                ValueUtil.highestSeverityOf(args, false),
                ValueUtil.latestValidTimeOrNowOf(args),
                displayNone());
    }
}
