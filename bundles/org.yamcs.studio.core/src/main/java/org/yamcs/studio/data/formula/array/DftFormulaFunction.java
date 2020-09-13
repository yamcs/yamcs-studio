/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
    public Object calculate(final List<Object> args) {
        VNumberArray array = (VNumberArray) args.get(0);
        if (array == null) {
            return null;
        }
        if (array.getSizes().size() != 1) {
            throw new IllegalArgumentException("Only 1D array supported for DFT");
        }

        // TODO: no need to allocate empty array
        List<ListNumber> fft = ListMath.dft(array.getData(), new ArrayDouble(new double[array.getData().size()]));
        final ListNumber real = fft.get(0);
        final ListNumber imaginary = fft.get(1);
        ListNumber modulus = new ListDouble() {

            @Override
            public double getDouble(int index) {
                double x = real.getDouble(index);
                double y = imaginary.getDouble(index);
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
                double x = real.getDouble(index);
                double y = imaginary.getDouble(index);
                return Math.atan2(y, x);
            }

            @Override
            public int size() {
                return real.size();
            }
        };
        return ValueFactory.newVTable(Arrays.<Class<?>> asList(double.class, double.class, double.class, double.class),
                Arrays.asList("x", "y", "mod", "phase"),
                Arrays.<Object> asList(real, imaginary, modulus, phase));
    }
}
