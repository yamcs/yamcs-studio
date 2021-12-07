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

import org.yamcs.studio.autocomplete.parser.engine.Reflect;

public class ExprBinaryOperator extends Expr implements IBinaryOperator {

    protected Expr lhs;
    protected Expr rhs;

    public ExprBinaryOperator(ExprType type, Expr lhs, Expr rhs) {
        super(type);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public Expr getLHS() {
        return lhs;
    }

    @Override
    public void setLHS(Expr lhs) {
        this.lhs = lhs;
    }

    @Override
    public Expr getRHS() {
        return rhs;
    }

    @Override
    public void setRHS(Expr rhs) {
        this.rhs = rhs;
    }

    @Override
    public void validate() throws ExprException {
        if (lhs == null) {
            throw new ExprException("LHS of operator missing");
        }
        lhs.validate();
        if (rhs == null) {
            throw new ExprException("RHS of operator missing");
        }
        rhs.validate();
    }

    @Override
    public int hashCode() {
        var hc = type.ordinal();
        if (lhs != null) {
            hc ^= lhs.hashCode();
        }
        if (rhs != null) {
            hc ^= rhs.hashCode();
        }
        return hc;
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(getClass())) {
            return false;
        }

        var b = (ExprBinaryOperator) obj;
        return Reflect.equals(b.lhs, lhs) && Reflect.equals(b.rhs, rhs);
    }
}
