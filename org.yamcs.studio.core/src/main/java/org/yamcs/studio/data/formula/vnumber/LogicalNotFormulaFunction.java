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
package org.yamcs.studio.data.formula.vnumber;

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VBoolean;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Implementation for ! operator.
 */
class LogicalNotFormulaFunction implements FormulaFunction {

    private final List<Class<?>> argumentTypes;
    private final List<String> argumentNames;

    public LogicalNotFormulaFunction() {
        argumentTypes = Arrays.<Class<?>> asList(VBoolean.class);
        argumentNames = Arrays.asList("arg");
    }

    @Override
    public String getName() {
        return "!";
    }

    @Override
    public String getDescription() {
        return "Conditional NOT";
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
        var value = (VBoolean) args.get(0);
        if (value == null) {
            return null;
        }
        return ValueFactory.newVBoolean(!value.getValue(), value, value);
    }
}
