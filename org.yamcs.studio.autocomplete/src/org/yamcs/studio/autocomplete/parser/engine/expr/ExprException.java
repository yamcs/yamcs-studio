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

public class ExprException extends Exception {

    private static final long serialVersionUID = -1998822947453924659L;

    public ExprException() {
        super();
    }

    public ExprException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExprException(String message) {
        super(message);
    }

    public ExprException(Throwable cause) {
        super(cause);
    }
}
