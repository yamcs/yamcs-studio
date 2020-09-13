/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
        super(new FormulaFunctionSetDescription("vnumber", "Operators for numeric scalars")
                .addFormulaFunction(
                        new AbstractVNumberVNumberToVNumberFormulaFunction("+", "Numeric addition", "arg1", "arg2") {
                            @Override
                            public double calculate(double arg1, double arg2) {
                                return arg1 + arg2;
                            }
                        })
                .addFormulaFunction(
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
                })
                .addFormulaFunction(
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
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("<=", "Less than or equal",
                        "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 <= arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction(">=", "Greater than or equal",
                        "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 >= arg2;
                    }
                })
                .addFormulaFunction(
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
                })
                .addFormulaFunction(
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
                })
                .addFormulaFunction(
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
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("&", "Bitwise AND",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("and", "Bitwise AND",
                        "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                })
                .addFormulaFunction(new ConditionalOperatorFormulaFunction())
                .addFormulaFunction(new LogicalNotFormulaFunction()));
    }

}
