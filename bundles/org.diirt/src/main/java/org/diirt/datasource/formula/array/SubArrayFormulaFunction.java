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
class SubArrayFormulaFunction implements FormulaFunction {

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#isPure()
     */
    @Override
    public boolean isPure() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#isVarArgs()
     */
    @Override
    public boolean isVarArgs() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#getName()
     */
    @Override
    public String getName() {
        return "subArray";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#getDescription()
     */
    @Override
    public String getDescription() {
        return "Result[] = [ array[fromIndex], ..., array[toIndex-1] ]";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#getArgumentTypes()
     */
    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class, VNumber.class,
                VNumber.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#getArgumentNames()
     */
    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array", "fromIndex", "toIndex");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvmanager.formula.FormulaFunction#getReturnType()
     */
    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.epics.pvmanager.formula.FormulaFunction#calculate(java.util.List)
     */
    @Override
    public Object calculate(List<Object> args) {
        if (NullUtils.containsNull(args)) {
            return null;
        }

        VNumberArray numberArray = (VNumberArray) args.get(0);
        int fromIndex = ((VNumber) args.get(1)).getValue().intValue();
        int toIndex = ((VNumber) args.get(2)).getValue().intValue();

        return newVNumberArray(
                ListMath.limit(numberArray.getData(), fromIndex, toIndex),
                ValueUtil.highestSeverityOf(args, false),
                ValueUtil.latestValidTimeOrNowOf(args),
                displayNone());
    }

}
