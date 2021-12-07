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

import org.yamcs.studio.data.formula.AbstractVBooleanVBooleanToVBooleanFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVNumberToVNumberFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVNumberVNumberToVBooleanFormulaFunction;
import org.yamcs.studio.data.formula.AbstractVNumberVNumberToVNumberFormulaFunction;
import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * Formula functions that operate on {@link org.yamcs.studio.data.vtype.VNumber}s.
 */
public class VNumberFunctionSet extends FormulaFunctionSet {

    public VNumberFunctionSet() {
        // Use abstact classes for most of the functions as the signature
        // is the same
        super(new FormulaFunctionSetDescription("vnumber", "Operators for numeric scalars").addFormulaFunction(
                new AbstractVNumberVNumberToVNumberFormulaFunction("+", "Numeric addition", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 + arg2;
                    }
                }).addFormulaFunction(
                        new AbstractVNumberVNumberToVNumberFormulaFunction("-", "Numeric subtraction", "arg1", "arg2") {
                            @Override
                            public double calculate(double arg1, double arg2) {
                                return arg1 - arg2;
                            }
                        })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("*", "Numeric multiplication",
                        "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 * arg2;
                    }
                }).addFormulaFunction(
                        new AbstractVNumberVNumberToVNumberFormulaFunction("/", "Numeric division", "arg1", "arg2") {
                            @Override
                            public double calculate(double arg1, double arg2) {
                                return arg1 / arg2;
                            }
                        })
                .addFormulaFunction(
                        new AbstractVNumberVNumberToVNumberFormulaFunction("%", "Numeric remainder", "arg1", "arg2") {
                            @Override
                            public double calculate(double arg1, double arg2) {
                                return arg1 % arg2;
                            }
                        })
                .addFormulaFunction(
                        new AbstractVNumberVNumberToVNumberFormulaFunction("^", "Numeric power", "arg1", "arg2") {
                            @Override
                            public double calculate(double arg1, double arg2) {
                                return Math.pow(arg1, arg2);
                            }
                        })
                .addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("-", "Numeric negation", "arg1") {
                    @Override
                    public double calculate(double arg1) {
                        return -arg1;
                    }
                }).addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("<=", "Less than or equal",
                        "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 <= arg2;
                    }
                }).addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction(">=", "Greater than or equal",
                        "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 >= arg2;
                    }
                }).addFormulaFunction(
                        new AbstractVNumberVNumberToVBooleanFormulaFunction("<", "Less than", "arg1", "arg2") {
                            @Override
                            public boolean calculate(double arg1, double arg2) {
                                return arg1 < arg2;
                            }
                        })
                .addFormulaFunction(
                        new AbstractVNumberVNumberToVBooleanFormulaFunction(">", "Greater than", "arg1", "arg2") {
                            @Override
                            public boolean calculate(double arg1, double arg2) {
                                return arg1 > arg2;
                            }
                        })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("==", "Equal", "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 == arg2;
                    }
                }).addFormulaFunction(
                        new AbstractVNumberVNumberToVBooleanFormulaFunction("!=", "Not equal", "arg1", "arg2") {
                            @Override
                            public boolean calculate(double arg1, double arg2) {
                                return arg1 != arg2;
                            }
                        })
                .addFormulaFunction(
                        new AbstractVBooleanVBooleanToVBooleanFormulaFunction("||", "Conditional OR", "arg1", "arg2") {
                            @Override
                            public boolean calculate(boolean arg1, boolean arg2) {
                                return arg1 || arg2;
                            }
                        })
                .addFormulaFunction(
                        new AbstractVBooleanVBooleanToVBooleanFormulaFunction("&&", "Conditional AND", "arg1", "arg2") {
                            @Override
                            public boolean calculate(boolean arg1, boolean arg2) {
                                return arg1 && arg2;
                            }
                        })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("xor", "Bitwise XOR",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 ^ arg2;
                    }
                }).addFormulaFunction(
                        new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("|", "Bitwise OR", "arg1", "arg2") {
                            @Override
                            public int calculate(int arg1, int arg2) {
                                return arg1 | arg2;
                            }
                        })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("or", "Bitwise OR",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 | arg2;
                    }
                }).addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("&", "Bitwise AND",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                }).addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("and", "Bitwise AND",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                }).addFormulaFunction(new ConditionalOperatorFormulaFunction())
                .addFormulaFunction(new LogicalNotFormulaFunction()));
    }

}
