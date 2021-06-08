/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.ListNumberProvider;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Formula function that constructs an array with given data and boundaries.
 */
class ArrayWithBoundariesFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "arrayWithBoundaries";
    }

    @Override
    public String getDescription() {
        return "Returns an array with the given values and cell boundaries";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, ListNumberProvider.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("dataArray", "boundaries");
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

        VNumberArray array = (VNumberArray) args.get(0);
        if (array.getSizes().size() != args.size() - 1) {
            throw new IllegalArgumentException("Dimension of the array must match the number of ListNumberProvider");
        }

        List<ArrayDimensionDisplay> dimDisplay = new ArrayList<>();
        for (int i = 1; i < args.size(); i++) {
            ListNumberProvider numberGenerator = (ListNumberProvider) args.get(i);
            dimDisplay.add(
                    ValueFactory.newDisplay(numberGenerator.createListNumber(array.getSizes().getInt(i - 1) + 1), ""));
        }

        return ValueFactory.newVNumberArray(array.getData(), array.getSizes(), dimDisplay, array, array, array);
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
