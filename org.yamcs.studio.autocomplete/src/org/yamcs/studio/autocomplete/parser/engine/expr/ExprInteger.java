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

public class ExprInteger extends ExprNumber {

    public static final ExprInteger ZERO = new ExprInteger(0);

    public final int value;

    public ExprInteger(int value) {
        super(ExprType.Integer);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExprInteger && value == ((ExprInteger) obj).value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
