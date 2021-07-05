/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.vtype.VBoolean;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Abstract class for formula functions that take two {@link VNumber} as arguments and return a {@link VBoolean}.
 * <p>
 * This class takes care of:
 * <ul>
 * <li>extracting double value from {@code VNumber}</li>
 * <li>null handling - returns null if one argument is null</li>
 * <li>alarm handling - returns highest alarm</li>
 * <li>time handling - returns latest time, or now if no time is available</li>
 * <li>display handling - returns display none</li>
 * </ul>
 */
public abstract class AbstractVNumberVNumberToVBooleanFormulaFunction implements FormulaFunction {

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
    public AbstractVNumberVNumberToVBooleanFormulaFunction(String name, String description, String arg1Name,
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
        return VBoolean.class;
    }

    @Override
    public final Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }

        VNumber arg1 = (VNumber) args.get(0);
        VNumber arg2 = (VNumber) args.get(1);

        return ValueFactory.newVBoolean(
                calculate(arg1.getValue().doubleValue(), arg2.getValue().doubleValue()),
                highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args));
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
    public abstract boolean calculate(double arg1, double arg2);

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
