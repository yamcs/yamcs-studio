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
package org.yamcs.studio.data.formula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A set of utility methods for formula functions.
 */
public class FormulaFunctions {

    /**
     * Check whether the function will accept the given list of values as arguments.
     *
     * @param arguments
     *            the possible values
     * @param function
     *            a function
     * @return true if the function can accept the given arguments
     */
    public static boolean matchArgumentTypes(List<Object> arguments, FormulaFunction function) {
        return matchArgumentTypes(arguments, function, false);
    }

    /**
     * Checks whether the function will accept the given arguments.
     *
     * @param arguments
     *            the arguments
     * @param function
     *            a function
     * @param allowNull
     *            whether the function should allow null arguments
     * @return true if the function can accept the given arguments
     */
    public static boolean matchArgumentTypes(List<Object> arguments, FormulaFunction function, boolean allowNull) {
        var types = function.getArgumentTypes();

        if (!matchArgumentCount(arguments.size(), function)) {
            return false;
        }

        for (var i = 0; i < arguments.size(); i++) {
            var j = Math.min(i, types.size() - 1);
            if (!types.get(j).isInstance(arguments.get(i))) {
                if (allowNull && arguments.get(i) == null) {
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check whether the function will accept the given number of arguments.
     *
     * @param nArguments
     *            the number of arguments
     * @param function
     *            a function
     * @return true if the function can accept the given number of arguments
     */
    public static boolean matchArgumentCount(int nArguments, FormulaFunction function) {
        // no varargs must match
        if (!function.isVarArgs() && (function.getArgumentTypes().size() != nArguments)) {
            return false;
        }

        // varargs can have 0 arguments
        if (function.isVarArgs() && ((function.getArgumentTypes().size() - 1) > nArguments)) {
            return false;
        }

        return true;
    }

    /**
     * Finds the first function that can accept the given values as arguments.
     *
     * @param arguments
     *            the possible values
     * @param formulaFunctions
     *            a collection of functions
     * @return the first function that accepts the give arguments
     */
    public static FormulaFunction findFirstMatch(List<Object> arguments, Collection<FormulaFunction> formulaFunctions) {
        for (FormulaFunction formulaFunction : formulaFunctions) {
            if (matchArgumentTypes(arguments, formulaFunction, true)) {
                return formulaFunction;
            }
        }

        return null;
    }

    /**
     * Finds the functions that match the given types as arguments.
     *
     * @param argTypes
     *            the possible types
     * @param formulaFunctions
     *            a collection of functions
     * @return the first function that accepts the give arguments
     */
    public static Collection<FormulaFunction> findArgTypeMatch(List<Class<?>> argTypes,
            Collection<FormulaFunction> formulaFunctions) {
        Collection<FormulaFunction> functions = new HashSet<>();
        for (FormulaFunction formulaFunction : formulaFunctions) {
            if (formulaFunction.getArgumentTypes().equals(argTypes)) {
                functions.add(formulaFunction);
            }
        }

        return functions;
    }

    /**
     * Returns a string representation of the function that will include the function name, argument types, argument
     * names and the result type.
     *
     * @param function
     *            a function
     * @return string representation
     */
    public static String formatSignature(FormulaFunction function) {
        // Prepare arguments
        List<String> arguments = new ArrayList<>();
        for (var i = 0; i < function.getArgumentTypes().size() - 1; i++) {
            arguments
                    .add(function.getArgumentTypes().get(i).getSimpleName() + " " + function.getArgumentNames().get(i));
        }
        var lastArgument = new StringBuilder();
        lastArgument.append(function.getArgumentTypes().get(function.getArgumentTypes().size() - 1).getSimpleName());
        if (function.isVarArgs()) {
            lastArgument.append("...");
        }
        lastArgument.append(" ").append(function.getArgumentNames().get(function.getArgumentTypes().size() - 1));
        arguments.add(lastArgument.toString());

        // Format strings
        var sb = new StringBuilder();
        sb.append(format(function.getName(), arguments));
        sb.append(": ");
        sb.append(function.getReturnType().getSimpleName());
        return sb.toString();
    }

    private static final Pattern postfixTwoArg = Pattern
            .compile("\\+|-|\\*|/|%|\\^|\\*\\*|<=|>=|<|>|==|!=|\\|\\||&&|\\||&");
    private static final Pattern prefixOneArg = Pattern.compile("-|!");

    /**
     * Given the function name and a string representation of the arguments, returns the properly formatted string
     * representation of the whole expression.
     *
     * @param function
     *            the function name
     * @param args
     *            the arguments
     * @return the expression text representation
     */
    public static String format(String function, List<String> args) {
        if (args.size() == 3 && "?:".equals(function)) {
            return conditionalOperator(function, args);
        }
        if (args.size() == 2 && postfixTwoArg.matcher(function).matches()) {
            return formatPostfixTwoArgs(function, args);
        }
        if (args.size() == 1 && prefixOneArg.matcher(function).matches()) {
            return formatPrefixOneArg(function, args);
        }
        return formatFunction(function, args);
    }

    private static String conditionalOperator(String function, List<String> args) {
        var sb = new StringBuilder();
        sb.append("(").append(args.get(0)).append(" ? ").append(args.get(1)).append(" : ").append(args.get(2))
                .append(")");
        return sb.toString();
    }

    private static String formatPostfixTwoArgs(String function, List<String> args) {
        var sb = new StringBuilder();
        sb.append("(").append(args.get(0)).append(" ").append(function).append(" ").append(args.get(1)).append(")");
        return sb.toString();
    }

    private static String formatPrefixOneArg(String function, List<String> args) {
        var sb = new StringBuilder();
        sb.append(function).append(args.get(0));
        return sb.toString();
    }

    private static String formatFunction(String function, List<String> args) {
        var sb = new StringBuilder();
        sb.append(function).append('(');
        var first = true;
        for (String arg : args) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(arg);
        }
        sb.append(')');
        return sb.toString();
    }
}
