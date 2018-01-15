/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.diirt.datasource.expression.DesiredRateExpression;
import org.diirt.datasource.expression.DesiredRateExpressionList;
import org.diirt.datasource.expression.DesiredRateExpressionListImpl;
import org.diirt.util.text.StringUtil;

/**
 * The abstract syntax tree corresponding to a formula expression.
 * This class provides a logical representation of the expression,
 * static factory methods to create such expressions from text representation
 * (i.e. parsing) and the ability to convert to datasource expressions.
 *
 * @author carcassi
 */
public class FormulaAst {
    /**
     * The type of a formula AST node.
     */
    public enum Type {
        /**
         * An operator/function node
         */
        OP,

        /**
         * A String literal node
         */
        STRING,

        /**
         * An integer literal node
         */
        INTEGER,

        /**
         * A floating point literal node
         */
        FLOATING_POINT,

        /**
         * A channel node
         */
        CHANNEL,

        /**
         * An id node
         */
        ID};

    private final Type type;
    private final List<FormulaAst> children;
    private final Object value;

    private FormulaAst(Type type, List<FormulaAst> children, Object value) {
        this.type = type;
        this.children = children;
        this.value = value;
    }

    /**
     * The type of the AST node.
     *
     * @return the node type
     */
    public Type getType() {
        return type;
    }

    /**
     * The value corresponding to the node. The value depends on the type
     * as follows:
     * <ul>
     *   <li>OP: String with the name of the function/operator</li>
     *   <li>STRING: the String constant (unquoted)</li>
     *   <li>INTEGER: the Integer constant</li>
     *   <li>FLOATING_POINT: the Double constant</li>
     *   <li>CHANNEL: String with the channel name (unquoted)</li>
     *   <li>ID: String with the name of the id</li>
     * </ul>
     * @return the value of the node
     */
    public Object getValue() {
        return value;
    }

    /**
     * The children of this node, if IO, null otherwise.
     *
     * @return the node children; null if no children
     */
    public List<FormulaAst> getChildren() {
        return children;
    }

    /**
     * Lists all the channel names used in the AST.
     *
     * @return a list of channel names
     */
    public List<String> listChannelNames() {
        List<String> names = new ArrayList<>();
        listChannelNames(names);
        return Collections.unmodifiableList(names);
    }

    private void listChannelNames(List<String> names) {
        switch(getType()) {
            case OP:
                for (FormulaAst child : getChildren()) {
                    child.listChannelNames(names);
                }
                break;
            case CHANNEL:
                names.add((String) getValue());
            default:
        }
    }

    /**
     * A STRING node from a quoted token.
     *
     * @param token the quoted string
     * @return the new node
     */
    public static FormulaAst stringFromToken(String token) {
        return string(StringUtil.unquote(token));
    }

    /**
     * A STRING node representing the given string.
     *
     * @param unquotedString the string
     * @return the new node
     */
    public static FormulaAst string(String unquotedString) {
        return new FormulaAst(Type.STRING, null, unquotedString);
    }

    /**
     * An INTEGER node from a token.
     *
     * @param token a string parsable to an integer
     * @return the new node
     */
    public static FormulaAst integerFromToken(String token) {
        return integer(Integer.parseInt(token));
    }

    /**
     * An INTEGER node from the given value.
     *
     * @param integer the integer value
     * @return the new node
     */
    public static FormulaAst integer(int integer) {
        return new FormulaAst(Type.INTEGER, null, integer);
    }

    /**
     * A FLOATING_POINT node from a token.
     *
     * @param token a string parseable to a double
     * @return the new node
     */
    public static FormulaAst floatingPointFromToken(String token) {
        return floatingPoint(Double.parseDouble(token));
    }

    /**
     * A FLOATING_POINT node from the given value.
     *
     * @param floatingPoint the double value
     * @return the new node
     */
    public static FormulaAst floatingPoint(double floatingPoint) {
        return new FormulaAst(Type.FLOATING_POINT, null, floatingPoint);
    }

    /**
     * A CHANNEL node from a quoted token.
     *
     * @param token the quoted channel name
     * @return the new node
     */
    public static FormulaAst channelFromToken(String token) {
        return channel(StringUtil.unquote(token));
    }

    /**
     * A CHANNEL node representing the given channel name.
     *
     * @param channelName the channel name
     * @return the new node
     */
    public static FormulaAst channel(String channelName) {
        return new FormulaAst(Type.CHANNEL, null, channelName);
    }

    /**
     * An ID node representing the given id.
     *
     * @param id the id
     * @return the new node
     */
    public static FormulaAst id(String id) {
        return new FormulaAst(Type.ID, null, id);
    }

    /**
     * An OP node representing the given operator/function with the given
     * arguments.
     *
     * @param opName the name of the operator/function
     * @param children the node children
     * @return the new node
     */
    public static FormulaAst op(String opName, FormulaAst... children) {
        return op(opName, Arrays.asList(children));
    }

    /**
     * An OP node representing the given operator/function with the given
     * arguments.
     *
     * @param opName the name of the operator/function
     * @param children the node children
     * @return the new node
     */
    public static FormulaAst op(String opName, List<FormulaAst> children) {
        return new FormulaAst(Type.OP, children, opName);
    }

    /**
     * Creates a parser for the given text.
     *
     * @param text the string to be parsed
     * @return the new parser
     */
    static FormulaParser createParser(String text) {
        CharStream stream = new ANTLRStringStream(text);
        FormulaLexer lexer = new FormulaLexer(stream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        return new FormulaParser(tokenStream);
    }

    /**
     * The AST corresponding to the parsed formula.
     *
     * @param formula the string to be parsed
     * @return the parsed AST
     */
    public static FormulaAst formula(String formula) {
        FormulaAst ast = staticChannel(formula);
        if (ast != null) {
            return ast;
        }
        formula = formula.substring(1);

        try {
            ast = createParser(formula).formula();
            if (ast == null) {
                throw new IllegalArgumentException("Parsing failed");
            }
            return ast;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error parsing formula: " + ex.getMessage(), ex);
        }
    }

    private static FormulaAst staticChannel(String formula) {
        if (formula.startsWith("=")) {
            return null;
        }

        if (formula.trim().matches(StringUtil.SINGLEQUOTED_STRING_REGEX)) {
            return channel(formula.trim());
        }
        return channel(formula);
    }

    /**
     * The AST corresponding to a single channel, if the formula represents one,
     * or null, if the formula is not a single channel.
     *
     * @param formula the string to be parsed
     * @return the parsed AST
     */
    public static FormulaAst singleChannel(String formula) {
        FormulaAst ast = staticChannel(formula);
        if (ast != null) {
            return ast;
        }
        formula = formula.substring(1);

        try {
            ast = createParser(formula).singleChannel();
            return ast;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Converts the AST to a datasource expression.
     *
     * @return the new expression
     */
    public DesiredRateExpression<?> toExpression() {
        switch(getType()) {
            case CHANNEL:
                return new LastOfChannelExpression<>((String) getValue(), Object.class);
            case FLOATING_POINT:
                return org.diirt.datasource.vtype.ExpressionLanguage.vConst((Double) getValue());
            case INTEGER:
                return org.diirt.datasource.vtype.ExpressionLanguage.vConst((Integer) getValue());
            case STRING:
                return org.diirt.datasource.vtype.ExpressionLanguage.vConst((String) getValue());
            case ID:
                return ExpressionLanguage.namedConstant((String) getValue());
            case OP:
                DesiredRateExpressionList<Object> expressions = new DesiredRateExpressionListImpl<>();
                for (FormulaAst child : getChildren()) {
                    expressions.and(child.toExpression());
                }
                return ExpressionLanguage.function((String) getValue(), expressions);
            default:
                throw new IllegalArgumentException("Unsupported type " + getType() + " for ast");
        }
    }

    /**
     * Returns a new AST where the channel nodes that match the keys of the map
     * are substituted with the values of the map.
     *
     * @param substitutions from channel name to new AST
     * @return a new AST
     */
    public FormulaAst substituteChannels(Map<String, FormulaAst> substitutions) {
        switch(getType()) {
            case CHANNEL:
                FormulaAst sub = substitutions.get((String) getValue());
                if (sub == null) {
                    sub = this;
                }
                return sub;
            case OP:
                FormulaAst[] subs = new FormulaAst[getChildren().size()];
                for (int i = 0; i < subs.length; i++) {
                    subs[i] = getChildren().get(i).substituteChannels(substitutions);
                }
                return op((String) getValue(), subs);
            default:
                return this;
        }
    }

    @Override
    public String toString() {
        switch(getType()) {
            case OP:
                return FormulaFunctions.format((String) getValue(), new AbstractList<String>() {

                    @Override
                    public String get(int index) {
                        return getChildren().get(index).toString();
                    }

                    @Override
                    public int size() {
                        return getChildren().size();
                    }
                });
            default:
                return getValue().toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof FormulaAst) {
            FormulaAst other = (FormulaAst) obj;
            return Objects.equals(getType(), other.getType()) &&
                    Objects.equals(getValue(), other.getValue()) &&
                    Objects.equals(getChildren(), other.getChildren());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.type);
        hash = 89 * hash + Objects.hashCode(this.children);
        hash = 89 * hash + Objects.hashCode(this.value);
        return hash;
    }

}
