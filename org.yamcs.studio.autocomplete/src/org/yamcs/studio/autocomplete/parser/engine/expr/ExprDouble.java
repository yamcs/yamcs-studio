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

public class ExprDouble extends ExprNumber {

    public static final ExprDouble ZERO = new ExprDouble(0);
    public static final ExprDouble PI = new ExprDouble(Math.PI);
    public static final ExprDouble E = new ExprDouble(Math.E);

    public double value;

    public ExprDouble(double value) {
        super(ExprType.Double);
        this.value = value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public int hashCode() {
        return (int) value * 100;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExprDouble && Math.abs(value - ((ExprDouble) obj).value) < 1.0e-10;
    }
}
