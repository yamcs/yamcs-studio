/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import static org.diirt.vtype.ValueFactory.displayNone;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.util.NullUtils;

import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;
import static org.diirt.vtype.ValueFactory.newVNumberArray;
import org.diirt.vtype.ValueUtil;

/**
 * Abstract class for formula functions that take a {@link VNumber} and a
 * {@link VNumberArray} as arguments
 * and return a {@code VNumberArray}.
 * <p>
 * This class takes care of:
 * <ul>
 *    <li>extracting double value from {@code VNumber}</li>
 *    <li>extracting ListNumber value from {@code VNumberArray}</li>
 *    <li>null handling - returns null if one argument is null</li>
 *    <li>alarm handling - returns highest alarm</li>
 *    <li>time handling - returns latest time, or now if no time is available</li>
 *    <li>display handling - returns display none</li>
 * </ul>
 *
 * @author shroffk
 *
 */
public abstract class AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction implements
        FormulaFunction {

    private static final List<Class<?>> argumentTypes = Arrays.<Class<?>> asList(VNumberArray.class, VNumber.class);

    private final String name;
    private final String description;
    private final List<String> argumentNames;

    /**
     * Creates a new function.
     *
     * @param name function name; can't be null
     * @param description function description; can't be null
     * @param arg1Name first argument name; can't be null
     * @param arg2Name second argument name; can't be null
     */
    public AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction(String name, String description,
            String arg1Name, String arg2Name) {
        // Validate parameters
        if (name == null) {
            throw new NullPointerException("Function name can't be null");
        }
        if (description == null) {
            throw new NullPointerException("Function description can't be null");
        }
        if (arg1Name == null) {
            throw new NullPointerException("First argument name can't be null");
        }
        if (arg2Name == null) {
            throw new NullPointerException("Second argument name can't be null");
        }

        this.name = name;
        this.description = description;
        this.argumentNames = Arrays.asList(arg1Name, arg2Name);
    }

    @Override
    public final boolean isPure() {
        return true;
    }

    @Override
    public final boolean isVarArgs() {
        return false;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final List<Class<?>> getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public final List<String> getArgumentNames() {
        return argumentNames;
    }

    @Override
    public final Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public final Object calculate(List<Object> args) {
        if (NullUtils.containsNull(args)) {
            return null;
        }

        VNumberArray arg1 = (VNumberArray) args.get(0);
        VNumber arg2 = (VNumber) args.get(1);

        return newVNumberArray(
                calculate(arg1.getData(), arg2.getValue().doubleValue()),
                ValueUtil.highestSeverityOf(args, false),
                ValueUtil.latestValidTimeOrNowOf(args),
                displayNone());
    }

    /**
     * Calculates the result based on the two arguments. This is the only
     * method one has to implement.
     *
     * @param arg1 the first argument; not null
     * @param arg2 the second argument
     * @return the result; not null
     */
    public abstract ListNumber calculate(ListNumber arg1, double arg2);

}
