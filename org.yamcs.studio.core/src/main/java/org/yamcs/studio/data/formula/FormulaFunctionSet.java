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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A set of functions that can be used in the formulas.
 * <p>
 * Objects of this class can be registered in the {@link FormulaRegistry} and the functions will be available in the
 * formula language.
 */
public abstract class FormulaFunctionSet {
    static Pattern namePattern = Pattern.compile("[a-zA-Z_]\\w*");

    private final String name;
    private final String description;
    private final Collection<FormulaFunction> formulaFunctions;

    /**
     * Creates a new ser of functions to be registered in the formula language.
     *
     * @param functionSetDescription
     *            the description of the function set
     */
    public FormulaFunctionSet(FormulaFunctionSetDescription functionSetDescription) {
        this.name = functionSetDescription.name;
        this.description = functionSetDescription.description;
        this.formulaFunctions = Collections.unmodifiableSet(new HashSet<>(functionSetDescription.formulaFunctions));
    }

    /**
     * Returns the name of the function set.
     *
     * @return the function set name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the description of the function set.
     *
     * @return the function set description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * The names of all functions in this set.
     *
     * @return the function names
     */
    public final Collection<String> getFunctionNames() {
        Set<String> names = new HashSet<>();
        for (FormulaFunction formulaFunction : formulaFunctions) {
            names.add(formulaFunction.getName());
        }
        return names;
    }

    /**
     * Returns all the functions in the set with the given name.
     *
     * @param name
     *            the name of the function
     * @return the matched functions; never null
     */
    public final Collection<FormulaFunction> findFunctions(String name) {
        if (name == null) {
            return Collections.emptyList();
        }

        Set<FormulaFunction> formulas = new HashSet<>();
        for (FormulaFunction formulaFunction : formulaFunctions) {
            if (name.equals(formulaFunction.getName())) {
                formulas.add(formulaFunction);
            }
        }
        return formulas;
    }

    /**
     * Returns all functions in the set.
     *
     * @return the functions in the set
     */
    public final Collection<FormulaFunction> getFunctions() {
        return formulaFunctions;
    }
}
