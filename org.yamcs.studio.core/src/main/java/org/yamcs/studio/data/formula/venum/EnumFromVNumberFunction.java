/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.venum;

import static org.yamcs.studio.data.vtype.ValueFactory.newVEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VStringArray;

class EnumFromVNumberFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "enumOf";
    }

    @Override
    public String getDescription() {
        return "Creates a VEnum based a value and a set of intervals";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumber.class, VNumberArray.class, VStringArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("value", "intervals", "labels");
    }

    @Override
    public Class<?> getReturnType() {
        return VEnum.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }
        VNumber value = (VNumber) args.get(0);
        VNumberArray intervals = (VNumberArray) args.get(1);
        VStringArray labels = (VStringArray) args.get(2);
        int index = 0;
        while (index < intervals.getData().size()
                && value.getValue().doubleValue() >= intervals.getData().getDouble(index)) {
            index++;
        }
        return newVEnum(index, labels.getData(),
                value,
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
