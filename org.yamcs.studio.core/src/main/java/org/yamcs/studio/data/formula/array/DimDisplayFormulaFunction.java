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
package org.yamcs.studio.data.formula.array;

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ArrayDimensionDisplay;
import org.yamcs.studio.data.vtype.VBoolean;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VTableFactory;
import org.yamcs.studio.data.vtype.ValueFactory;

class DimDisplayFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "dimDisplay";
    }

    @Override
    public String getDescription() {
        return "Gathers information for one dimension of an nd array";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumber.class, VBoolean.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("size", "invert");
    }

    @Override
    public Class<?> getReturnType() {
        return ArrayDimensionDisplay.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VNumber size = (VNumber) args.get(0);
        VBoolean invert = (VBoolean) args.get(1);

        if (size == null || invert == null) {
            return null;
        }

        return ValueFactory.newDisplay(size.getValue().intValue(), VTableFactory.step(0, 1), invert.getValue());
    }

}
