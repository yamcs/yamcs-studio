/********************************************************************************
 * Copyright (c) 2009, 2021 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine;

public class ExprToken {

    public static final ExprToken OPEN_BRACKET = new ExprToken(ExprTokenType.OpenBracket, "(");
    public static final ExprToken CLOSE_BRACKET = new ExprToken(ExprTokenType.CloseBracket, ")");
    public static final ExprToken OPEN_BRACE = new ExprToken(ExprTokenType.OpenBrace, "{");
    public static final ExprToken CLOSE_BRACE = new ExprToken(ExprTokenType.CloseBrace, "}");
    public static final ExprToken SEMI_COLON = new ExprToken(ExprTokenType.SemiColon, ";");
    public static final ExprToken COMMA = new ExprToken(ExprTokenType.Comma, ",");

    public static final ExprToken PLUS = new ExprToken(ExprTokenType.Plus, "+");
    public static final ExprToken MINUS = new ExprToken(ExprTokenType.Minus, "-");
    public static final ExprToken MULTIPLY = new ExprToken(ExprTokenType.Multiply, "*");
    public static final ExprToken DIVIDE = new ExprToken(ExprTokenType.Divide, "/");
    public static final ExprToken POWER = new ExprToken(ExprTokenType.Power, "^");

    public static final ExprToken LESS_THAN = new ExprToken(ExprTokenType.LessThan, "<");
    public static final ExprToken LESS_THAN_EQUAL = new ExprToken(ExprTokenType.LessThanOrEqualTo, "<=");
    public static final ExprToken GREATER_THAN = new ExprToken(ExprTokenType.GreaterThan, ">");
    public static final ExprToken GREATER_THAN_EQUAL = new ExprToken(ExprTokenType.GreaterThanOrEqualTo, ">=");
    public static final ExprToken EQUAL = new ExprToken(ExprTokenType.Equal, "==");
    public static final ExprToken NOT_EQUAL = new ExprToken(ExprTokenType.NotEqual, "!=");
    public static final ExprToken NOT = new ExprToken(ExprTokenType.Not, "!");
    public static final ExprToken COND_AND = new ExprToken(ExprTokenType.CondAnd, "&&");
    public static final ExprToken COND_OR = new ExprToken(ExprTokenType.CondOr, "||");

    public static final ExprToken BIT_AND = new ExprToken(ExprTokenType.BitAnd, "&");
    public static final ExprToken BIT_OR = new ExprToken(ExprTokenType.BitOr, "|");
    public static final ExprToken REMAINDER = new ExprToken(ExprTokenType.Remainder, "%");
    public static final ExprToken QUESTION_MARK = new ExprToken(ExprTokenType.QuestionMark, "?");
    public static final ExprToken COLON = new ExprToken(ExprTokenType.Colon, ":");

    public static final ExprToken SIMPLE_EQUAL = new ExprToken(ExprTokenType.SimpleEqual, "=");

    public final ExprTokenType type;
    public final String val;
    public double doubleValue;
    public final int integerValue;

    public ExprToken(ExprTokenType type, String val) {
        this.type = type;
        this.val = val;
        doubleValue = 0.;
        integerValue = 0;
    }

    public ExprToken(String val, double doubleValue) {
        type = ExprTokenType.Decimal;
        this.val = val;
        this.doubleValue = doubleValue;
        integerValue = 0;
    }

    public ExprToken(String val, int integerValue) {
        type = ExprTokenType.Integer;
        this.val = val;
        doubleValue = 0.;
        this.integerValue = integerValue;
    }

    @Override
    public String toString() {
        return type.toString() + ":" + val;
    }
}
