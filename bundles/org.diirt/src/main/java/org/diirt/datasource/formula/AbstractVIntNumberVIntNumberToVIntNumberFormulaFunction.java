/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import static org.diirt.vtype.ValueFactory.displayNone;
import static org.diirt.vtype.ValueFactory.newVInt;

import java.util.Arrays;
import java.util.List;

import org.diirt.datasource.util.NullUtils;
import org.diirt.vtype.VNumber;

/**
 * Abstract class for formula functions that take two integer {@link VNumber} as arguments and return an integer
 * {@code VNumber}.
 * <p>
 * This class takes care of:
 * <ul>
 * <li>extracting int value from the {@code VNumber}</li>
 * <li>null handling - returns null if one argument is null</li>
 * <li>alarm handling - returns highest alarm</li>
 * <li>time handling - returns latest time, or now if no time is available</li>
 * <li>display handling - returns display none</li>
 * </ul>
 *
 * @author shroffk
 */
public abstract class AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction implements FormulaFunction {

    private final String name;
    private final String description;
    private final List<Class<?>> argumentTypes;
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
    public AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction(String name, String description, String arg1Name,
            String arg2Name) {
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
        this.argumentTypes = Arrays.<Class<?>> asList(VNumber.class, VNumber.class);
        this.argumentNames = Arrays.asList(arg1Name, arg2Name);
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
    public final boolean isPure() {
        return true;
    }

    @Override
    public final boolean isVarArgs() {
        return false;
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
        return VNumber.class;
    }

    @Override
    public final Object calculate(List<Object> args) {
        if (NullUtils.containsNull(args)) {
            return null;
        }

        Number arg1 = ((VNumber) args.get(0)).getValue();
        Number arg2 = ((VNumber) args.get(1)).getValue();
        if (arg1 instanceof Float || arg2 instanceof Float ||
                arg1 instanceof Double || arg2 instanceof Double) {
            throw new IllegalArgumentException("Operator '" + getName() + "' only works with integers");
        }

        return newVInt(
                calculate(arg1.intValue(), arg2.intValue()),
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
     *            the second argument
     * @return the result
     */
    public abstract int calculate(int arg1, int arg2);

}
