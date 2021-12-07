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
package org.yamcs.studio.data.formula.venum;

import static org.yamcs.studio.data.vtype.ValueFactory.newVEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VStringArray;

class EnumFromVNumberFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "enumOf";
    }

    @Override
    public String getDescription() {
        return "Creates a VEnum based a value and a set of intervals";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumber.class, VNumberArray.class, VStringArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("value", "intervals", "labels");
    }

    @Override
    public Class<?> getReturnType() {
        return VEnum.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        if (containsNull(args)) {
            return null;
        }
        var value = (VNumber) args.get(0);
        var intervals = (VNumberArray) args.get(1);
        var labels = (VStringArray) args.get(2);
        var index = 0;
        while (index < intervals.getData().size()
                && value.getValue().doubleValue() >= intervals.getData().getDouble(index)) {
            index++;
        }
        return newVEnum(index, labels.getData(), value, latestValidTimeOrNowOf(args));
    }

    private static boolean containsNull(Collection<Object> args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }
}
