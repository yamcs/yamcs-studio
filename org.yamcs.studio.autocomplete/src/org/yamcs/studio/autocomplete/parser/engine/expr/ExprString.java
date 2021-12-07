/********************************************************************************
 * Copyright (c) 2009, 2021 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

import org.yamcs.studio.autocomplete.parser.engine.ExprLexer;

public class ExprString extends Expr {

    public static final String EMPTY = "";

    public final String str;

    public ExprString(String str) {
        super(ExprType.String);
        this.str = str;
    }

    @Override
    public String toString() {
        return ExprLexer.escapeString(str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExprString && str.equals(((ExprString) obj).str);
    }
}
