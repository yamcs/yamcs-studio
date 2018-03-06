/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.util.NullUtils;
import org.diirt.vtype.VBoolean;
import org.diirt.vtype.ValueFactory;
import org.diirt.vtype.ValueUtil;


/**
 * Abstract class for formula functions that take two {@link VBoolean}s as arguments
 * and return a {@link VBoolean}.
 * <p>
 * This class takes care of:
 * <ul>
 *    <li>extracting boolean value from {@code VBoolean}</li>
 *    <li>null handling - returns null if one argument is null</li>
 *    <li>alarm handling - returns highest alarm</li>
 *    <li>time handling - returns latest time, or now if no time is available</li>
 *    <li>display handling - returns display none</li>
 * </ul>
 *
 * @author carcassi
 */
public abstract class AbstractVBooleanVBooleanToVBooleanFormulaFunction implements FormulaFunction {

    private final String name;
    private final String description;
    private final List<Class<?>> argumentTypes;
    private final List<String> argumentNames;

    /**
     * Creates a new function.
     *
     * @param name function name; can't be null
     * @param description function description; can't be null
     * @param arg1Name first argument name; can't be null
     * @param arg2Name second argument name; can't be null
     */
    public AbstractVBooleanVBooleanToVBooleanFormulaFunction(String name, String description, String arg1Name, String arg2Name) {
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
        this.argumentTypes = Arrays.<Class<?>>asList(VBoolean.class, VBoolean.class);
        this.argumentNames = Arrays.asList(arg1Name, arg2Name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isPure() {
        return true;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public List<String> getArgumentNames() {
        return argumentNames;
    }

    @Override
    public Class<?> getReturnType() {
        return VBoolean.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (NullUtils.containsNull(args)) {
            return null;
        }

        VBoolean arg1 = (VBoolean) args.get(0);
        VBoolean arg2 = (VBoolean) args.get(1);

        return ValueFactory.newVBoolean(
                calculate(arg1.getValue(), arg2.getValue()),
                ValueUtil.highestSeverityOf(args, false),
                ValueUtil.latestValidTimeOrNowOf(args));
    }

    /**
     * Calculates the result based on the two arguments. This is the only
     * method one has to implement.
     *
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the result
     */
    public abstract boolean calculate(boolean arg1, boolean arg2);

}
