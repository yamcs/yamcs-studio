/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2012-18 diirt developers.
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

import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newVNumberArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.vtype.ListNumber;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;

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
 */
public abstract class AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction implements FormulaFunction {

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
    public AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction(String name, String description, String arg1Name,
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
        this.argumentNames = Arrays.asList(arg1Name, arg2Name);
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
        if (containsNull(args)) {
            return null;
        }

        var arg1 = (VNumber) args.get(0);
        var arg2 = (VNumberArray) args.get(1);

        return newVNumberArray(calculate(arg1.getValue().doubleValue(), arg2.getData()), highestSeverityOf(args, false),
                latestValidTimeOrNowOf(args), displayNone());
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

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
