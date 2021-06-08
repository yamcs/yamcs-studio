/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.yamcs.studio.data.formula.array;

import org.yamcs.studio.data.formula.AbstractVNumberArrayVNumberArrayToVNumberArrayFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction;
import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;
import org.yamcs.studio.data.vtype.ListMath;
import org.yamcs.studio.data.vtype.ListNumber;
import org.yamcs.studio.data.vtype.VNumberArray;

/**
 * A set of functions to work with {@link VNumberArray}s.
 */
public class ArrayFunctionSet extends FormulaFunctionSet {

    public ArrayFunctionSet() {
        super(
                new FormulaFunctionSetDescription("array",
                        "Aggregation and calculations on arrays")
                                .addFormulaFunction(new ArrayOfNumberFormulaFunction())
                                .addFormulaFunction(new ArrayOfStringFormulaFunction())
                                .addFormulaFunction(new ArrayWithBoundariesFormulaFunction())
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction("arrayPow",
                                                "Result[x] = pow(array[x], expon)",
                                                "array", "expon") {
                                            @Override
                                            public ListNumber calculate(ListNumber arg1, double arg2) {
                                                return ListMath.pow(arg1, arg2);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction("arrayPow",
                                                "Result[x] = pow(base, array[x])", "base", "array") {
                                            @Override
                                            public ListNumber calculate(double arg1, ListNumber arg2) {
                                                return ListMath.pow(arg1, arg2);
                                            }
                                        })
                                .addFormulaFunction(new RescaleArrayFormulaFunction())
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberArrayToVNumberArrayFormulaFunction("arrayMult",
                                                "Result[x] = array1[x] * array2[x]",
                                                "array1", "array2") {

                                            @Override
                                            public ListNumber calculate(ListNumber array1, ListNumber array2) {
                                                return ListMath.multiply(array1, array2);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberArrayToVNumberArrayFormulaFunction("arrayDiv",
                                                "Result[x] = array1[x] / array2[x]",
                                                "array1", "array2") {

                                            @Override
                                            public ListNumber calculate(ListNumber array1, ListNumber array2) {
                                                return ListMath.divide(array1, array2);
                                            }
                                        })
                                .addFormulaFunction(new SubArrayFormulaFunction())
                                .addFormulaFunction(new ElementAtNumberFormulaFunction())
                                .addFormulaFunction(new ElementAtStringFormulaFunction())
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberArrayToVNumberArrayFormulaFunction("+",
                                                "Result[x] = array1[x] + array2[x]",
                                                "array1", "array2") {

                                            @Override
                                            public ListNumber calculate(ListNumber array1, ListNumber array2) {
                                                return ListMath.add(array1, array2);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberArrayToVNumberArrayFormulaFunction("-",
                                                "Result[x] = array1[x] - array2[x]",
                                                "array1", "array2") {

                                            @Override
                                            public ListNumber calculate(ListNumber array1, ListNumber array2) {
                                                return ListMath.subtract(array1, array2);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction("arraySum",
                                                "Result[x] = array[x] + offset",
                                                "array", "offset") {

                                            @Override
                                            public ListNumber calculate(ListNumber array, double offset) {
                                                return ListMath.rescale(array, 1.0, offset);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction("arraySum",
                                                "Result[x] = offset + array[x]",
                                                "offset", "array") {

                                            @Override
                                            public ListNumber calculate(double offset, ListNumber array) {
                                                return ListMath.rescale(array, 1.0, offset);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction("arraySub",
                                                "Result[x] = array[x] - offset",
                                                "array", "offset") {

                                            @Override
                                            public ListNumber calculate(ListNumber array, double offset) {
                                                return ListMath.rescale(array, 1.0, -offset);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction("arraySub",
                                                "Result[x] = offset - array[x]",
                                                "offset", "array") {

                                            @Override
                                            public ListNumber calculate(double offset, ListNumber array) {
                                                return ListMath.rescale(array, -1.0, offset);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction("arrayDiv",
                                                "Result[x] = num / array[x]",
                                                "num", "array") {

                                            @Override
                                            public ListNumber calculate(double numerator, ListNumber array) {
                                                return ListMath.inverseRescale(array, numerator, 0.0);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction("*",
                                                "Result[x] = array[x] * num",
                                                "array", "num") {

                                            @Override
                                            public ListNumber calculate(ListNumber array, double num) {
                                                return ListMath.rescale(array, num, 0.0);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberVNumberArrayToVNumberArrayFormulaFunction("*",
                                                "Result[x] = num * array[x]",
                                                "num", "array") {

                                            @Override
                                            public ListNumber calculate(double num, ListNumber array) {
                                                return ListMath.rescale(array, num, 0.0);
                                            }
                                        })
                                .addFormulaFunction(
                                        new AbstractVNumberArrayVNumberToVNumberArrayFormulaFunction("arrayDiv",
                                                "Result[x] = array[x] / num",
                                                "array", "num") {

                                            @Override
                                            public ListNumber calculate(ListNumber array, double num) {
                                                return ListMath.rescale(array, (1 / num), 0.0);
                                            }
                                        })
                                .addFormulaFunction(new DftFormulaFunction())
                                .addFormulaFunction(new ArrayRangeOfFormulaFunction())
                                .addFormulaFunction(new DimDisplayFormulaFunction())
                                .addFormulaFunction(new NdArrayFormulaFunction()));
    }
}
