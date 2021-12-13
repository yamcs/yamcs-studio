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

import java.io.IOException;
import java.util.ArrayList;

import org.yamcs.studio.autocomplete.parser.engine.expr.Expr;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprArray;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprBinaryOperator;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprConditionalOperator;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprDouble;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprException;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprExpression;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprFunction;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprInteger;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprMissing;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprPV;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprString;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprType;
import org.yamcs.studio.autocomplete.parser.engine.expr.ExprVariable;
import org.yamcs.studio.autocomplete.parser.engine.expr.IBinaryOperator;

public class ExprParser {

    private Expr current;

    public static Expr parse(String text) throws IOException, ExprException {
        var p = new ExprParser();
        p.parse(new ExprLexer(text));
        return p.get();
    }

    public void parse(ExprLexer lexer) throws IOException, ExprException {
        ExprToken e = null;
        while ((e = lexer.next()) != null) {
            parseToken(lexer, e);
        }
    }

    private void parseToken(ExprLexer lexer, ExprToken token) throws ExprException, IOException {
        switch (token.type) {
        case Plus:
        case Minus:
        case Multiply:
        case Divide:
        case Power:
        case LessThan:
        case LessThanOrEqualTo:
        case GreaterThan:
        case GreaterThanOrEqualTo:
        case Equal:
        case NotEqual:
        case Not:
        case CondAnd:
        case CondOr:
        case BitAnd:
        case BitOr:
        case Remainder:
            parseOperator(token, lexer);
            break;
        case QuestionMark:
            parseConditionalOperator(token, lexer);
            break;
        case Decimal:
        case Integer:
        case String:
        case Variable:
        case QuotedVariable:
            parseValue(token);
            break;
        case Function:
            parseFunction(token, lexer);
            break;
        case OpenBracket:
            parseExpression(lexer);
            break;
        case OpenBrace:
            parseArray(lexer);
            break;
        case SimpleEqual: // avoid error when typing
            break;
        default:
            throw new ExprException("Unexpected " + token.type + " found");
        }
    }

    private void parseFunction(ExprToken token, ExprLexer lexer) throws ExprException, IOException {
        var c = current;
        current = null;
        ExprToken e = null;
        var args = new ArrayList<Expr>();
        var complete = false;
        while ((e = lexer.next()) != null) {
            if (e.type.equals(ExprTokenType.Comma)) {
                if ((e = lexer.next()) != null) {
                    current = null;
                    parseToken(lexer, e);
                    args.add(current == null ? new ExprMissing() : current);
                } else {
                    args.add(new ExprMissing());
                }
            } else if (e.type.equals(ExprTokenType.CloseBracket)) { // end
                complete = true;
                current = c;
                break;
            } else { // first arg
                parseToken(lexer, e);
                args.add(current == null ? new ExprMissing() : current);
            }
        }
        var f = new ExprFunction(token.val, args.toArray(new Expr[0]));
        f.setComplete(complete);

        setValue(f);
    }

    private void parseExpression(ExprLexer lexer) throws IOException, ExprException {
        var c = current;
        current = null;
        ExprToken e = null;
        while ((e = lexer.next()) != null) {
            if (e.type.equals(ExprTokenType.CloseBracket)) {
                var t = current;
                current = c;
                setValue(new ExprExpression(t));
                break;
            } else {
                parseToken(lexer, e);
            }
        }
    }

    private void parseArray(ExprLexer lexer) throws ExprException, IOException {
        var c = current;
        current = null;
        ExprToken e = null;
        var cols = -1;
        var count = 0;
        var args = new ArrayList<Expr>();
        while ((e = lexer.next()) != null) {
            if (e.type.equals(ExprTokenType.Comma)) {
                // if (current == null)
                // throw new ExprException("Arrays cannot contain empty values");
                args.add(current == null ? new ExprMissing() : current);
                current = null;
                count++;
            } else if (e.type.equals(ExprTokenType.SemiColon)) {
                // if (current == null)
                // throw new ExprException("Arrays cannot contain empty values");
                args.add(current == null ? new ExprMissing() : current);
                current = null;
                count++;
                // if (count == 0) {
                // throw new ExprException("Array rows must contain at least one element");
                // }
                // if (cols != -1 && count != cols) {
                // throw new ExprException("Array rows must be equal sizes");
                // }
                cols = count;
                count = 0;
            } else if (e.type.equals(ExprTokenType.CloseBrace)) {
                args.add(current == null ? new ExprMissing() : current);
                current = c;
                var rows = 1;
                if (cols == -1) {
                    cols = args.size();
                } else {
                    rows = args.size() / cols;
                }
                var a = new ExprArray(rows, cols);
                for (var i = 0; i < args.size(); i++) {
                    a.set(0, i, args.get(i));
                }
                setValue(a);
                break;
            } else {
                parseToken(lexer, e);
            }
        }
    }

    private void parseValue(ExprToken e) throws ExprException {
        Expr value = null;
        switch (e.type) {
        case Decimal:
            value = new ExprDouble(e.doubleValue);
            break;
        case Integer:
            value = new ExprInteger(e.integerValue);
            break;
        case String:
            value = new ExprString(e.val);
            break;
        case Variable:
            value = new ExprVariable(e.val);
            break;
        case QuotedVariable:
            value = new ExprPV(e.val);
            break;
        default:
            break;
        }
        setValue(value);
    }

    private void setValue(Expr value) throws ExprException {
        var c = current;
        if (c instanceof IBinaryOperator) {
            ((IBinaryOperator) c).setRHS(value);
        } else {
            current = value;
        }
    }

    private void parseOperator(ExprToken e, ExprLexer lexer) throws ExprException, IOException {
        // handle negative numbers
        if ((e.type == ExprTokenType.Minus || e.type == ExprTokenType.Plus) && current == null) {
            var nextToken = lexer.next();
            if (nextToken == null) {
                return;
            }
            Expr value = null;
            switch (nextToken.type) {
            case Decimal:
                value = new ExprDouble(e.type == ExprTokenType.Minus ? -nextToken.doubleValue : nextToken.doubleValue);
                setValue(value);
                return;
            case Integer:
                value = new ExprInteger(
                        e.type == ExprTokenType.Minus ? -nextToken.integerValue : nextToken.integerValue);
                setValue(value);
                return;
            default:
                break;
            }
            current = new ExprBinaryOperator(ExprType.BinaryOperation, null, null);
            parseToken(lexer, nextToken);
            return;
        }
        current = new ExprBinaryOperator(ExprType.BinaryOperation, current, null);
    }

    private void parseConditionalOperator(ExprToken token, ExprLexer lexer) throws ExprException, IOException {
        var c = current;
        current = null;
        ExprToken e = null;
        var co = new ExprConditionalOperator(c, null, null);
        while ((e = lexer.next()) != null) {
            if (e.type.equals(ExprTokenType.Colon)) {
                if ((e = lexer.next()) != null) {
                    current = null;
                    parseToken(lexer, e);
                    co.setValueIfFalse(current == null ? new ExprMissing() : current);
                } else {
                    co.setValueIfFalse(new ExprMissing());
                }
                break;
            } else {
                parseToken(lexer, e);
                co.setValueIfTrue(current == null ? new ExprMissing() : current);
            }
        }
        setValue(co);
    }

    public Expr get() {
        return current;
    }
}
