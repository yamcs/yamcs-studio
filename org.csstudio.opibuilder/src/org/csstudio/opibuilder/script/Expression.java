/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

/**
 * The expression data for a rule.
 */
public class Expression {

    private String booleanExpression;
    private Object value;

    public Expression(String booleanExpression, Object value) {
        this.booleanExpression = booleanExpression;
        this.value = value;
    }

    /**
     * @return the booleanExpression
     */
    public final String getBooleanExpression() {
        return booleanExpression;
    }

    /**
     * @param booleanExpression
     *            the booleanExpression to set
     */
    public void setBooleanExpression(String booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    /**
     * @return the value
     */
    public final Object getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    public Expression getCopy() {
        return new Expression(booleanExpression, value);
    }

}
