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

public class ExprBoolean extends ExprNumber {

    public static final ExprBoolean TRUE = new ExprBoolean(true);
    public static final ExprBoolean FALSE = new ExprBoolean(false);

    public final boolean value;

    public ExprBoolean(boolean value) {
        super(ExprType.Boolean);
        this.value = value;
    }

    public boolean booleanValue() {
        return value;
    }

    public double doubleValue() {
        return intValue();
    }

    public int intValue() {
        return value ? 1 : 0;
    }

    public int hashCode() {
        return value ? 1 : 0;
    }

    public boolean equals(Object obj) {
        return obj instanceof ExprBoolean && value == ((ExprBoolean) obj).value;
    }

    public String toString() {
        return Boolean.toString(value).toUpperCase();
    }
}
