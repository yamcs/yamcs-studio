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

public enum ExprType {
    Double,
    Integer,
    Boolean,
    String,
    PV,
    Function,
    Variable,
    Array,
    Expression,
    BinaryOperation,
    ConditionalOperation,
    Missing,
    Error
}
