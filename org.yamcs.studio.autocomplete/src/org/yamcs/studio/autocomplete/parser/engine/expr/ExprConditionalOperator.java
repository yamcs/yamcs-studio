/********************************************************************************
 * Copyright (c) 2009 Peter Smith and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.parser.engine.expr;

public class ExprConditionalOperator extends Expr {

    protected Expr condition;
    protected Expr valueIfTrue;
    protected Expr valueIfFalse;

    public ExprConditionalOperator(Expr condition, Expr valueIfTrue, Expr valueIfFalse) {
        super(ExprType.ConditionalOperation);
        this.condition = condition;
        this.valueIfTrue = valueIfTrue;
        this.valueIfFalse = valueIfFalse;
    }

    public Expr getCondition() {
        return condition;
    }

    public void setCondition(Expr condition) {
        this.condition = condition;
    }

    public Expr getValueIfTrue() {
        return valueIfTrue;
    }

    public void setValueIfTrue(Expr valueIfTrue) {
        this.valueIfTrue = valueIfTrue;
    }

    public Expr getValueIfFalse() {
        return valueIfFalse;
    }

    public void setValueIfFalse(Expr valueIfFalse) {
        this.valueIfFalse = valueIfFalse;
    }

}
