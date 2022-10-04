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

public abstract class ExprNumber extends Expr {

    ExprNumber(ExprType type) {
        super(type);
    }

    @Override
    public void validate() throws ExprException {
    }

    public boolean booleanValue() {
        return intValue() != 0;
    }

    public abstract int intValue();

    public abstract double doubleValue();
}
