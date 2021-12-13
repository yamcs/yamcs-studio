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

import java.util.Arrays;
import java.util.List;

import org.yamcs.studio.data.formula.FormulaFunction;
import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ListDouble;
import org.yamcs.studio.data.vtype.ListMath;
import org.yamcs.studio.data.vtype.ListNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.ValueFactory;

class DftFormulaFunction implements FormulaFunction {

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "dft";
    }

    @Override
    public String getDescription() {
        return "(Experimental) DFT of the argument";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>> asList(VNumberArray.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("array1D");
    }

    @Override
    public Class<?> getReturnType() {
        return VTable.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        var array = (VNumberArray) args.get(0);
        if (array == null) {
            return null;
        }
        if (array.getSizes().size() != 1) {
            throw new IllegalArgumentException("Only 1D array supported for DFT");
        }

        // TODO: no need to allocate empty array
        var fft = ListMath.dft(array.getData(), new ArrayDouble(new double[array.getData().size()]));
        var real = fft.get(0);
        var imaginary = fft.get(1);
        ListNumber modulus = new ListDouble() {
            @Override
            public double getDouble(int index) {
                var x = real.getDouble(index);
                var y = imaginary.getDouble(index);
                if (x != 0 || y != 0) {
                    return Math.sqrt(x * x + y * y);
                } else {
                    return 0.0;
                }
            }

            @Override
            public int size() {
                return real.size();
            }
        };
        ListNumber phase = new ListDouble() {
            @Override
            public double getDouble(int index) {
                var x = real.getDouble(index);
                var y = imaginary.getDouble(index);
                return Math.atan2(y, x);
            }

            @Override
            public int size() {
                return real.size();
            }
        };
        return ValueFactory.newVTable(Arrays.<Class<?>> asList(double.class, double.class, double.class, double.class),
                Arrays.asList("x", "y", "mod", "phase"), Arrays.<Object> asList(real, imaginary, modulus, phase));
    }
}
