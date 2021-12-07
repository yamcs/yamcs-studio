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

public class ExprError extends Expr {

    public static final Expr NULL = new ExprError("#NULL!", "Null Error");
    public static final Expr DIV0 = new ExprError("#DIV/0!", "Divide by Zero Error");
    public static final Expr VALUE = new ExprError("#VALUE", "Error in Value");
    public static final Expr REF = new ExprError("#REF!", "Reference Error");
    public static final Expr NAME = new ExprError("#NAME?", "Invalid Name Error");
    public static final Expr NUM = new ExprError("#NUM!", "Number Error");
    public static final Expr NA = new ExprError("#N/A", "Value not Available");

    private String errType;
    private String message;

    public ExprError(String type, String message) {
        super(ExprType.Error);
        this.errType = type;
        this.message = message;
    }

    public String getErrType() {
        return errType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "#" + message;
    }
}
