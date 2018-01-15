/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.venum;

import static org.diirt.vtype.ValueFactory.*;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;

import org.diirt.datasource.util.NullUtils;
import org.diirt.vtype.VEnum;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import org.diirt.vtype.VStringArray;
import org.diirt.vtype.ValueUtil;

/**
 *
 * @author carcassi
 */
class EnumFromVNumberFunction implements FormulaFunction {

    @Override
    public boolean isPure() {
        return true;
    }

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
        return Arrays.<Class<?>>asList(VNumber.class, VNumberArray.class, VStringArray.class);
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
        if (NullUtils.containsNull(args)) {
            return null;
        }
        VNumber value = (VNumber) args.get(0);
        VNumberArray intervals = (VNumberArray) args.get(1);
        VStringArray labels = (VStringArray) args.get(2);
        int index = 0;
        while (index < intervals.getData().size() && value.getValue().doubleValue() >= intervals.getData().getDouble(index)) {
            index++;
        }
        return newVEnum(index, labels.getData(),
                value,
                ValueUtil.latestValidTimeOrNowOf(args));
    }

}
