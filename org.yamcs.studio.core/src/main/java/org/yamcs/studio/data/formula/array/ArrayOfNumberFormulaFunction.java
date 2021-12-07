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
package org.yamcs.studio.data.formula.array;

import static org.yamcs.studio.data.vtype.ValueFactory.displayNone;
import static org.yamcs.studio.data.vtype.ValueFactory.newDisplay;

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.formula.StatisticsUtil;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.NumberFormats;
import org.yamcs.studio.data.vtype.VNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.ValueFactory;
import org.yamcs.studio.data.vtype.ValueUtil;

class ArrayOfNumberFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return true;
    }

    @Override
    public String getName() {
        return "arrayOf";
    }

    @Override
    public String getDescription() {
        return "Constructs array from a series of numbers";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumber.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("args");
    }

    @Override
    public Class<?> getReturnType() {
        return VNumberArray.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        ListDouble data = new ListDouble() {
            @Override
            public double getDouble(int index) {
                var number = (VNumber) args.get(index);
                if (number == null || number.getValue() == null) {
                    return Double.NaN;
                } else {
                    return number.getValue().doubleValue();
                }
            }

            @Override
            public int size() {
                return args.size();
            }
        };

        VNumber firstNonNull = null;
        for (Object object : args) {
            if (object != null) {
                firstNonNull = (VNumber) object;
            }
        }

        var display = displayNone();
        if (firstNonNull != null) {
            if (ValueUtil.displayHasValidDisplayLimits(firstNonNull)) {
                display = firstNonNull;
            } else {
                var stats = StatisticsUtil.statisticsOf(data);
                display = newDisplay(stats.getRange().getMinimum(), stats.getRange().getMinimum(),
                        stats.getRange().getMinimum(), "", NumberFormats.toStringFormat(),
                        stats.getRange().getMaximum(), stats.getRange().getMaximum(), stats.getRange().getMaximum(),
                        stats.getRange().getMinimum(), stats.getRange().getMaximum());
            }

        }

        return ValueFactory.newVNumberArray(data, highestSeverityOf(args, false), latestValidTimeOrNowOf(args),
                display);
    }
}
