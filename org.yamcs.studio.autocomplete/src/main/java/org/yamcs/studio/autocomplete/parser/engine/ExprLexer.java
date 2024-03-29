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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ExprLexer {

    private TokenReader reader;
    private int lastChar;

    public ExprLexer(BufferedReader reader) {
        this.reader = new TokenReader(reader);
    }

    public ExprLexer(Reader reader) {
        this(new BufferedReader(reader));
    }

    public ExprLexer(String str) {
        this(new StringReader(str));
    }

    public ExprToken next() throws IOException {
        if (lastChar == 0 || Character.isWhitespace(lastChar)) {
            lastChar = reader.ignoreWhitespace();
        }

        return readToken();
    }

    private ExprToken readToken() throws IOException {
        if (Character.isDigit(lastChar)) {
            return readNumber();
        }

        switch (lastChar) {
        case '\"':
            return readString();
        case '\'':
            return readQuotedVariable();
        case '(':
            lastChar = 0;
            return ExprToken.OPEN_BRACKET;
        case ')':
            lastChar = 0;
            return ExprToken.CLOSE_BRACKET;
        case '{':
            lastChar = 0;
            return ExprToken.OPEN_BRACE;
        case '}':
            lastChar = 0;
            return ExprToken.CLOSE_BRACE;
        case ';':
            lastChar = 0;
            return ExprToken.SEMI_COLON;
        case ',':
            lastChar = 0;
            return ExprToken.COMMA;
        case '+':
            lastChar = 0;
            return ExprToken.PLUS;
        case '-':
            lastChar = 0;
            return ExprToken.MINUS;
        case '*':
            lastChar = 0;
            return ExprToken.MULTIPLY;
        case '/':
            lastChar = 0;
            return ExprToken.DIVIDE;
        case '^':
            lastChar = 0;
            return ExprToken.POWER;
        case '<':
        case '>':
        case '=':
        case '!':
        case '&':
        case '|':
            return readComparisonOperator();
        case '%':
            lastChar = 0;
            return ExprToken.REMAINDER;
        case '?':
            lastChar = 0;
            return ExprToken.QUESTION_MARK;
        case ':':
            lastChar = 0;
            return ExprToken.COLON;
        case -1:
        case 0xffff:
            return null;
        }

        if (!Character.isJavaIdentifierStart(lastChar)) {
            throw new IOException("Invalid token found: " + lastChar);
        }

        return readVariableOrFunction();
    }

    private ExprToken readNumber() throws IOException {
        var sb = new StringBuilder(); // Todo, more efficient number
        // builder (ie. shift bits)
        sb.append((char) lastChar);
        lastChar = reader.read();
        var decimal = false;
        while (Character.isDigit(lastChar) || '.' == lastChar) {
            sb.append((char) lastChar);
            if (lastChar == '.') {
                decimal = true;
            }
            lastChar = reader.read();
        }

        if (lastChar == 'E' || lastChar == 'e') {
            sb.append((char) lastChar);
            lastChar = reader.read();
            if (lastChar == '-' || lastChar == '+') {
                sb.append((char) lastChar);
                lastChar = reader.read();
            }
            while (Character.isDigit(lastChar)) {
                sb.append((char) lastChar);
                lastChar = reader.read();
            }
        }

        var val = sb.toString();
        if (decimal) {
            return new ExprToken(val, Double.parseDouble(val));
        } else {
            try {
                return new ExprToken(val, Integer.parseInt(val));
            } catch (NumberFormatException e) {
                // Catch very large numbers
                return new ExprToken(val, Double.parseDouble(val));
            }
        }
    }

    private ExprToken readQuotedVariable() throws IOException {
        var sb = new StringBuilder();

        var isComplete = false;
        sb.append('\'');
        while (lastChar != -1 && Character.isDefined((char) lastChar)) {
            lastChar = reader.read();
            if (lastChar == '\'') {
                lastChar = reader.read();
                if (lastChar == '\'') {
                    sb.append('\'');
                } else {
                    isComplete = true;
                    break;
                }
            } else {
                if (Character.isDefined((char) lastChar)) {
                    sb.append((char) lastChar);
                }
            }
        }
        if (isComplete) {
            sb.append('\'');
        }

        // Now read the rest of the variable
        // while (isVariablePart(lastChar)) {
        // sb.append((char) lastChar);
        // lastChar = reader.read();
        // }

        return new ExprToken(ExprTokenType.QuotedVariable, sb.toString());
    }

    private ExprToken readVariableOrFunction() throws IOException {
        var sb = new StringBuilder();

        while (isVariablePart(lastChar)) {
            sb.append((char) lastChar);
            lastChar = reader.read();
        }

        if (Character.isWhitespace(lastChar)) {
            lastChar = reader.ignoreWhitespace();
        }

        if (lastChar == '(') {
            lastChar = 0;
            return new ExprToken(ExprTokenType.Function, sb.toString());
        } else {
            return new ExprToken(ExprTokenType.Variable, sb.toString());
        }
    }

    private boolean isVariablePart(int lastChar) {
        return Character.isJavaIdentifierPart(lastChar) || lastChar == '!' || lastChar == ':';
    }

    private ExprToken readString() throws IOException {
        var str = unescapeString(reader);
        lastChar = 0;
        return new ExprToken(ExprTokenType.String, str);
    }

    public static String escapeString(String str) {
        var sb = new StringBuilder();
        var len = str.length();
        for (var i = 0; i < len; i++) {
            var c = str.charAt(i);
            switch (c) {
            case '\"':
                sb.append("\"\"");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

    public static String unescapeString(TokenReader r) throws IOException {
        var sb = new StringBuilder();
        char c = 0;
        while (c != '\"' && Character.isDefined(c)) {
            c = (char) r.read();
            switch (c) {
            case '\"':
                var v = r.peek();
                if (v == '\"') {
                    r.read();
                    sb.append('\"');
                    c = 0;
                }
                break;
            default:
                if (Character.isDefined(c)) {
                    sb.append(c);
                }
                break;
            }
        }
        return sb.toString();
    }

    private ExprToken readComparisonOperator() throws IOException {
        var current = lastChar;
        var peek = reader.peek();
        lastChar = 0;
        if (current == '<') {
            if (peek == '=') {
                reader.read();
                return ExprToken.LESS_THAN_EQUAL;
            } else {
                return ExprToken.LESS_THAN;
            }
        } else if (current == '>') {
            if (peek == '=') {
                reader.read();
                return ExprToken.GREATER_THAN_EQUAL;
            } else {
                return ExprToken.GREATER_THAN;
            }
        } else if (current == '=') {
            if (peek == '=') {
                reader.read();
                return ExprToken.EQUAL;
            } else {
                return ExprToken.SIMPLE_EQUAL;
            }
        } else if (current == '!') {
            if (peek == '=') {
                reader.read();
                return ExprToken.NOT_EQUAL;
            } else {
                return ExprToken.NOT;
            }
        } else if (current == '&') {
            if (peek == '&') {
                reader.read();
                return ExprToken.COND_AND;
            } else {
                return ExprToken.BIT_AND;
            }
        } else if (current == '|') {
            if (peek == '|') {
                reader.read();
                return ExprToken.COND_OR;
            } else {
                return ExprToken.BIT_OR;
            }
        }
        return null;
    }

    public int getCurrentIndex() {
        return reader.getCurrentIndex();
    }
}
