/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.array;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.vtype.ArrayDimensionDisplay;
import org.diirt.vtype.VBoolean;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.table.VTableFactory;

/**
 *
 * @author carcassi
 */
class DimDisplayFormulaFunction implements FormulaFunction {

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
        return "dimDisplay";
    }

    @Override
    public String getDescription() {
        return "Gathers information for one dimension of an nd array";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VNumber.class, VBoolean.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("size", "invert");
    }

    @Override
    public Class<?> getReturnType() {
        return ArrayDimensionDisplay.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VNumber size = (VNumber) args.get(0);
        VBoolean invert = (VBoolean) args.get(1);

        if (size == null || invert == null) {
            return null;
        }

        return ValueFactory.newDisplay(size.getValue().intValue(), VTableFactory.step(0, 1), invert.getValue());
    }

}
