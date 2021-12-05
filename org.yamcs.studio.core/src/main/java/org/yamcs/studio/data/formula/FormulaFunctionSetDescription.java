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

import static org.yamcs.studio.data.formula.FormulaFunctionSet.namePattern;

import java.util.Collection;
import java.util.HashSet;

/**
 * The description for a function set to be used in formula language.
 */
public class FormulaFunctionSetDescription {

    String name;
    String description;
    Collection<FormulaFunction> formulaFunctions = new HashSet<>();

    /**
     * A new function set description.
     *
     * @param name
     *            the name of the function set
     * @param description
     *            the description of the function set
     */
    public FormulaFunctionSetDescription(String name, String description) {
        this.name = name;
        this.description = description;
        if (!namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Name must start by a letter and only consist of letters and numbers");
        }
    }

    /**
     * Adds a formula in the set.
     *
     * @param formulaFunction
     *            the formula to add
     * @return this description
     */
    public FormulaFunctionSetDescription addFormulaFunction(FormulaFunction formulaFunction) {
        formulaFunctions.add(formulaFunction);
        return this;
    }
}
