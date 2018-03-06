/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.diirt.datasource.expression.DesiredRateExpression;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.expression.DesiredRateExpressionImpl;
import org.diirt.datasource.expression.DesiredRateExpressionList;
import org.diirt.datasource.expression.DesiredRateExpressionListImpl;
import org.diirt.datasource.expression.DesiredRateReadWriteExpression;
import org.diirt.datasource.expression.DesiredRateReadWriteExpressionImpl;
import org.diirt.datasource.expression.Expressions;
import org.diirt.datasource.expression.WriteExpression;
import org.diirt.vtype.ValueUtil;

/**
 * Support for formula expressions.
 *
 * @author carcassi
 */
public class ExpressionLanguage {
    private ExpressionLanguage() {
        // No instances
    }

    /**
     * If the formula represents a single channels it returns the name,
     * null otherwise.
     *
     * @param formula the formula to parse
     * @return the channel it represents or null
     */
    public static String channelFromFormula(String formula) {
        FormulaAst ast = FormulaAst.singleChannel(formula);
        if (ast == null) {
            return null;
        } else {
            return (String) ast.getValue();
        }
    }

    /**
     * Returns the expression that will return the live value of the
     * given formula.
     *
     * @param formula the formula to parse
     * @return an expression for the formula
     */
    public static DesiredRateReadWriteExpression<?, Object> formula(String formula) {
        DesiredRateExpression<?> exp = parseFormula(formula);

        if (exp instanceof LastOfChannelExpression) {
            return new DesiredRateReadWriteExpressionImpl<>(exp, org.diirt.datasource.vtype.ExpressionLanguage.vType(exp.getName()));
        } else if (exp instanceof ErrorDesiredRateExpression) {
            return new DesiredRateReadWriteExpressionImpl<>(exp, readOnlyWriteExpression("Parsing error"));
        } else {
            return new DesiredRateReadWriteExpressionImpl<>(exp, readOnlyWriteExpression("Read-only formula"));
        }
    }

    /**
     * Returns the expression corresponding to the formula represented by the
     * given Abstract Syntax Tree.
     *
     * @param ast a formula abstract syntax tree
     * @return an expression for the formula
     */
    public static DesiredRateReadWriteExpression<?, Object> formula(FormulaAst ast) {
        DesiredRateExpression<?> exp = ast.toExpression();

        if (exp instanceof LastOfChannelExpression) {
            return new DesiredRateReadWriteExpressionImpl<>(exp, org.diirt.datasource.vtype.ExpressionLanguage.vType(exp.getName()));
        } else if (exp instanceof ErrorDesiredRateExpression) {
            return new DesiredRateReadWriteExpressionImpl<>(exp, readOnlyWriteExpression("Parsing error"));
        } else {
            return new DesiredRateReadWriteExpressionImpl<>(exp, readOnlyWriteExpression("Read-only formula"));
        }
    }

    private static DesiredRateExpression<?> parseFormula(String formula) {
        try {
            return FormulaAst.formula(formula).toExpression();
        } catch(RuntimeException ex) {
            return errorDesiredRateExpression(ex);
        }
    }

    /**
     * An expression that returns the value of the formula and return null
     * for empty or null formula.
     * <p>
     * Some expressions allow for null expression arguments to handle
     * optional elements. In those cases, using this method makes
     * undeclared arguments fall through.
     *
     * @param formula the formula, can be null
     * @return an expression of the given type; null if formula is null or empty
     */
    public static DesiredRateExpression<?> formulaArg(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return null;
        }

        return parseFormula(formula);
    }

    /**
     * An expression that returns the value of the formula making sure
     * it's of the given type.
     *
     * @param <T> the type to read
     * @param formula the formula
     * @param readType the type to read
     * @return an expression of the given type
     */
    public static <T> DesiredRateExpression<T> formula(String formula, Class<T> readType) {
        DesiredRateExpression<?> exp = parseFormula(formula);
        return checkReturnType(readType, "Value", exp);
    }

    static DesiredRateExpression<?> cachedPv(String channelName) {
        return new LastOfChannelExpression<>(channelName, Object.class);
    }

    static DesiredRateExpression<?> namedConstant(String constantName) {
        Object value = FormulaRegistry.getDefault().findNamedConstant(constantName);
        if (value == null) {
            throw new IllegalArgumentException("No constant named '" + constantName + "' is defined");
        }
        return org.diirt.datasource.ExpressionLanguage.constant(value, constantName);
    }

    static <T> DesiredRateExpression<T> cast(Class<T> clazz, DesiredRateExpression<?> arg1) {
        if (arg1 instanceof LastOfChannelExpression) {
            return ((LastOfChannelExpression<?>)arg1).cast(clazz);
        }
        @SuppressWarnings("unchecked")
        DesiredRateExpression<T> op1 = (DesiredRateExpression<T>) arg1;
        return op1;
    }

    static <T> DesiredRateExpressionList<T> cast(Class<T> clazz, DesiredRateExpressionList<?> args) {
        for (DesiredRateExpression<? extends Object> desiredRateExpression : args.getDesiredRateExpressions()) {
            cast(clazz, desiredRateExpression);
        }
        @SuppressWarnings("unchecked")
        DesiredRateExpressionList<T> op1 = (DesiredRateExpressionList<T>) args;
        return op1;
    }

    static String opName(String op, DesiredRateExpression<?> arg1, DesiredRateExpression<?> arg2) {
        return "(" + arg1.getName() + op + arg2.getName() + ")";
    }

    static String opName(String op, DesiredRateExpression<?> arg) {
        return op + arg.getName();
    }

    static String funName(String fun, DesiredRateExpression<?> arg) {
        return fun + "(" + arg.getName()+ ")";
    }

    static DesiredRateExpression<?> powCast(DesiredRateExpression<?> arg1, DesiredRateExpression<?> arg2) {
        return function("^", new DesiredRateExpressionListImpl<Object>().and(arg1).and(arg2));
    }

    static DesiredRateExpression<?> threeArgOp(String opName, DesiredRateExpression<?> arg1, DesiredRateExpression<?> arg2, DesiredRateExpression<?> arg3) {
        return function(opName, new DesiredRateExpressionListImpl<Object>().and(arg1).and(arg2).and(arg3));
    }

    static DesiredRateExpression<?> twoArgOp(String opName, DesiredRateExpression<?> arg1, DesiredRateExpression<?> arg2) {
        return function(opName, new DesiredRateExpressionListImpl<Object>().and(arg1).and(arg2));
    }

    static DesiredRateExpression<?> oneArgOp(String opName, DesiredRateExpression<?> arg) {
        return function(opName, new DesiredRateExpressionListImpl<Object>().and(arg));
    }

    static DesiredRateExpression<?> function(String function, DesiredRateExpressionList<?> args) {
        Collection<FormulaFunction> matchedFunctions = FormulaRegistry.getDefault().findFunctions(function, args.getDesiredRateExpressions().size());
        FormulaReadFunction readFunction = new FormulaReadFunction(Expressions.functionsOf(args), matchedFunctions, function);
        List<String> argNames = new ArrayList<>(args.getDesiredRateExpressions().size());
        for (DesiredRateExpression<? extends Object> arg : args.getDesiredRateExpressions()) {
            argNames.add(arg.getName());
        }
        return new FormulaFunctionReadExpression(args, readFunction, FormulaFunctions.format(function, argNames));
    }

    static <T> WriteExpression<T> readOnlyWriteExpression(String errorMessage) {
        return new ReadOnlyWriteExpression<>(errorMessage, "");
    }

    static <T> DesiredRateExpression<T> errorDesiredRateExpression(RuntimeException error) {
        return new ErrorDesiredRateExpression<>(error, "");
    }

    static <T> DesiredRateExpression<T> checkReturnType(final Class<T> clazz, final String argName, final DesiredRateExpression<?> arg1) {
        return new DesiredRateExpressionImpl<T>(arg1, new ReadFunction<T>() {

            @Override
            public T readValue() {
                Object obj = arg1.getFunction().readValue();
                if (obj == null) {
                    return null;
                }

                if (clazz.isInstance(obj)) {
                    return clazz.cast(obj);
                } else {
                    throw new RuntimeException(argName + " must be a " + clazz.getSimpleName() + " (was " + ValueUtil.typeOf(obj).getSimpleName() + ")");
                }
            }
        }, arg1.getName());
    }
}
