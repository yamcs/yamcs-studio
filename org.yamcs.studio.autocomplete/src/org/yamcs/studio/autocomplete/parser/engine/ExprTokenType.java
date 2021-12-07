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

public enum ExprTokenType {
    Decimal,
    Integer,
    String,
    Variable,
    QuotedVariable,
    Function,
    OpenBracket,
    CloseBracket,
    OpenBrace,
    CloseBrace,
    SemiColon,
    Comma,
    Plus,
    Minus,
    Multiply,
    Divide,
    Power,
    LessThan,
    LessThanOrEqualTo,
    GreaterThan,
    GreaterThanOrEqualTo,
    Equal,
    NotEqual,
    Not,
    CondAnd,
    CondOr,
    BitAnd,
    BitOr,
    Remainder,
    QuestionMark,
    Colon,
    SimpleEqual
}
