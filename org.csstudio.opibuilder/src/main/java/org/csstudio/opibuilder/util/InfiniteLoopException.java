/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

/**
 * Infinite loop detected.
 */
public class InfiniteLoopException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 569430280936384743L;

    public InfiniteLoopException() { // NOP
    }

    public InfiniteLoopException(String message) {
        super(message);
    }

    public InfiniteLoopException(Throwable cause) {
        super(cause);
    }

    public InfiniteLoopException(String message, Throwable cause) {
        super(message, cause);
    }
}
