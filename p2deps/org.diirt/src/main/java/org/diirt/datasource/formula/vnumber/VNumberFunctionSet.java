/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vnumber;

import org.diirt.datasource.formula.AbstractVBooleanVBooleanToVBooleanFormulaFunction;
import org.diirt.datasource.formula.AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction;
import org.diirt.datasource.formula.AbstractVNumberVNumberToVBooleanFormulaFunction;
import org.diirt.datasource.formula.FormulaFunctionSet;
import org.diirt.datasource.formula.FormulaFunctionSetDescription;
import org.diirt.datasource.formula.AbstractVNumberToVNumberFormulaFunction;
import org.diirt.datasource.formula.AbstractVNumberVNumberToVNumberFormulaFunction;

/**
 * Formula functions that operate on {@link org.diirt.vtype.VNumber}s.
 *
 * @author carcassi
 */
public class VNumberFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public VNumberFunctionSet() {
        // Use abstact classes for most of the functions as the signature
        // is the same
        super(new FormulaFunctionSetDescription("vnumber", "Operators for numeric scalars")
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("+", "Numeric addition", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 + arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("-", "Numeric subtraction", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 - arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("*", "Numeric multiplication", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 * arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("/", "Numeric division", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 / arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("%", "Numeric remainder", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return arg1 % arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVNumberFormulaFunction("^", "Numeric power", "arg1", "arg2") {
                    @Override
                    public double calculate(double arg1, double arg2) {
                        return Math.pow(arg1, arg2);
                    }
                })
                .addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("-", "Numeric negation", "arg1") {
                    @Override
                    public double calculate(double arg1) {
                        return - arg1;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("<=", "Less than or equal", "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 <= arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction(">=", "Greater than or equal", "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 >= arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("<", "Less than", "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 < arg2;
                    }
                })
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction(">", "Greater than", "arg1", "arg2") {
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
                .addFormulaFunction(new AbstractVNumberVNumberToVBooleanFormulaFunction("!=", "Not equal", "arg1", "arg2") {
                    @Override
                    public boolean calculate(double arg1, double arg2) {
                        return arg1 != arg2;
                    }
                })
                .addFormulaFunction(new AbstractVBooleanVBooleanToVBooleanFormulaFunction("||", "Conditional OR", "arg1", "arg2") {
                    @Override
                    public boolean calculate(boolean arg1, boolean arg2) {
                        return arg1 || arg2;
                    }
                })
                .addFormulaFunction(new AbstractVBooleanVBooleanToVBooleanFormulaFunction("&&", "Conditional AND", "arg1", "arg2") {
                    @Override
                    public boolean calculate(boolean arg1, boolean arg2) {
                        return arg1 && arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("xor", "Bitwise XOR", "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 ^ arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("|", "Bitwise OR", "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 | arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("or", "Bitwise OR", "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 | arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("&", "Bitwise AND", "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                })
                .addFormulaFunction(new AbstractVIntNumberVIntNumberToVIntNumberFormulaFunction("and", "Bitwise AND", "arg1", "arg2") {
                    @Override
                    public int calculate(int arg1, int arg2) {
                        return arg1 & arg2;
                    }
                })
                .addFormulaFunction(new ConditionalOperatorFormulaFunction())
                .addFormulaFunction(new LogicalNotFormulaFunction())
                );
    }


}
