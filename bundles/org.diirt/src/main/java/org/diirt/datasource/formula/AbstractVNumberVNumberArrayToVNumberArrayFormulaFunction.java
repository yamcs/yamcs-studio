/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import static org.diirt.vtype.ValueFactory.displayNone;
import static org.diirt.vtype.ValueFactory.newVNumberArray;

import java.util.Arrays;
import java.util.List;

import org.diirt.datasource.util.NullUtils;
import org.diirt.util.array.ListNumber;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VNumberArray;

/**
 * Abstract class for formula functions that take a {@link VNumberArray} and a {@link VNumber} as arguments and return a
 * {@code VNumberArray}.
 * <p>
 * This class takes care of:
 * <ul>
 * <li>extracting double value from {@code VNumber}</li>
 * <li>extracting ListNumber value from {@code VNumberArray}</li>
 * <li>null handling - returns null if one argument is null</li>
 * <li>alarm handling - returns highest alarm</li>
 * <li>time handling - returns latest time, or now if no time is available</li>
 * <li>display handling - returns display none</li>
 * </ul>
 *
 * @author shroffk
 *
 */
public abstract class AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction implements
        FormulaFunction {

    private static final List<Class<?>> argumentTypes = Arrays.<Class<?>> asList(VNumber.class, VNumberArray.class);

    private final String name;
    private final String description;
    private final List<String> argumentNames;

    /**
     * Creates a new function.
     *
     * @param name
     *            function name; can't be null
     * @param description
     *            function description; can't be null
     * @param arg1Name
     *            first argument name; can't be null
     * @param arg2Name
     *            second argument name; can't be null
     */
    public AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction(String name, String description,
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

        VNumber arg1 = (VNumber) args.get(0);
        VNumberArray arg2 = (VNumberArray) args.get(1);

        return newVNumberArray(
                calculate(arg1.getValue().doubleValue(), arg2.getData()),
                highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args),
                displayNone());
    }

    /**
     * Calculates the result based on the two arguments. This is the only method one has to implement.
     *
     * @param arg1
     *            the first argument
     * @param arg2
     *            the second argument; not null
     * @return the result; not null
     */
    public abstract ListNumber calculate(double arg1, ListNumber arg2);

}
