/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.ValueFactory;

class NdArrayFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "ndArray";
    }

    @Override
    public String getDescription() {
        return "Creates an nd array with the given data and dimension display information";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, ArrayDimensionDisplay.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("data", "dimDisplay");
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

        VNumberArray data = (VNumberArray) args.get(0);
        ArrayDimensionDisplay[] displays = new ArrayDimensionDisplay[args.size() - 1];
        for (int i = 0; i < displays.length; i++) {
            displays[i] = (ArrayDimensionDisplay) args.get(i + 1);
        }

        return ValueFactory.ndArray(data, displays);
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
