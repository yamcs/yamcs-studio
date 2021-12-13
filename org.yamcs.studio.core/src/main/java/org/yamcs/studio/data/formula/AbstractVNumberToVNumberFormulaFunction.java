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
import java.util.List;

import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Abstract class for formula functions that take one {@link VNumber} as argument and return a {@code VNumber}.
 * <p>
 * This class takes care of:
 * <ul>
 * <li>extracting double value from {@code VNumber}</li>
 * <li>null handling - returns null if the argument is null</li>
 * <li>alarm handling - returns the argument alarm</li>
 * <li>time handling - returns the argument time</li>
 * <li>display handling - returns display none</li>
 * </ul>
 */
public abstract class AbstractVNumberToVNumberFormulaFunction implements FormulaFunction {

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
     * @param argName
     *            the argument name; can't be null
     */
    public AbstractVNumberToVNumberFormulaFunction(String name, String description, String argName) {
        // Validate parameters
        if (name == null) {
            throw new NullPointerException("Function name can't be null");
        }
        if (description == null) {
            throw new NullPointerException("Function description can't be null");
        }
        if (argName == null) {
            throw new NullPointerException("Argument name can't be null");
        }

        this.name = name;
        this.description = description;
        argumentTypes = Arrays.<Class<?>> asList(VNumber.class);
        argumentNames = Arrays.asList(argName);
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
        return VNumber.class;
    }

    @Override
    public final Object calculate(List<Object> args) {
        var arg = (VNumber) args.get(0);
        if (arg == null) {
            return null;
        }
        return ValueFactory.newVDouble(calculate(arg.getValue().doubleValue()), arg, arg, ValueFactory.displayNone());
    }

    /**
     * Calculates the result based on the arguments. This is the only method one has to implement.
     *
     * @param arg
     *            the argument
     * @return the result
     */
    public abstract double calculate(double arg);
}
