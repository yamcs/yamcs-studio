/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012, 2021 diirt developers and others
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.yamcs.studio.data.formula;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.vtype.VBoolean;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Abstract class for formula functions that take two {@link VBoolean}s as arguments and return a {@link VBoolean}.
 * <p>
 * This class takes care of:
 * <ul>
 * <li>extracting boolean value from {@code VBoolean}</li>
 * <li>null handling - returns null if one argument is null</li>
 * <li>alarm handling - returns highest alarm</li>
 * <li>time handling - returns latest time, or now if no time is available</li>
 * <li>display handling - returns display none</li>
 * </ul>
 */
public abstract class AbstractVBooleanVBooleanToVBooleanFormulaFunction implements FormulaFunction {

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
    public AbstractVBooleanVBooleanToVBooleanFormulaFunction(String name, String description, String arg1Name,
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
        argumentTypes = Arrays.<Class<?>> asList(VBoolean.class, VBoolean.class);
        argumentNames = Arrays.asList(arg1Name, arg2Name);
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
        if (containsNull(args)) {
            return null;
        }

        var arg1 = (VBoolean) args.get(0);
        var arg2 = (VBoolean) args.get(1);

        return ValueFactory.newVBoolean(calculate(arg1.getValue(), arg2.getValue()), highestSeverityOf(args, false),
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
    public abstract boolean calculate(boolean arg1, boolean arg2);

    private static boolean containsNull(Collection<Object> args) {
        for (var object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
