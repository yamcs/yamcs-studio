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
package org.yamcs.studio.data.formula.math;

import org.yamcs.studio.data.formula.AbstractVNumberToVNumberFormulaFunction;
import org.yamcs.studio.data.formula.FormulaFunctionSet;
import org.yamcs.studio.data.formula.FormulaFunctionSetDescription;

/**
 * A function set that corresponds to all methods in {@link Math}.
 */
public class MathFunctionSet extends FormulaFunctionSet {

    public MathFunctionSet() {
        // Use abstact classes for most of the functions as the signature
        // is the same
        super(new FormulaFunctionSetDescription("math", "Basic mathematical functions, wrapped from java.lang.Math")
                .addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("abs", "Absolute value", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.abs(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("acos", "Arc cosine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.acos(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("asin", "Arc sine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.asin(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("atan", "Arc tangent", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.atan(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("cbrt", "Cubic root", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.cbrt(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("ceil", "Ceiling function", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.ceil(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("cos", "Cosine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.cos(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("cosh", "Hyperbolic cosine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.cosh(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("exp", "Exponential", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.exp(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("floor", "Floor function", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.floor(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("log", "Natural logarithm", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.log(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("log10", "Base 10 logarithm", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.log10(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("round", "Round", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.round(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("signum", "Sign function", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.signum(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("sin", "Sine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.sin(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("sinh", "Hyperbolic sine", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.sinh(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("sqrt", "Square root", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.sqrt(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("tan", "Tangent", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.tan(arg);
                    }
                }).addFormulaFunction(new AbstractVNumberToVNumberFormulaFunction("tanh", "Hyperbolic tangent", "arg") {
                    @Override
                    public double calculate(double arg) {
                        return Math.tanh(arg);
                    }
                }).addFormulaFunction(
                        new AbstractVNumberToVNumberFormulaFunction("toDegrees", "Converts radians to degrees", "arg") {
                            @Override
                            public double calculate(double arg) {
                                return Math.toDegrees(arg);
                            }
                        })
                .addFormulaFunction(
                        new AbstractVNumberToVNumberFormulaFunction("toRadians", "Converts degrees to radians", "arg") {
                            @Override
                            public double calculate(double arg) {
                                return Math.toRadians(arg);
                            }
                        }));
    }
}
