/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

/**
 * The error handler for exception in job.
 */
public interface IJobErrorHandler {

    /**
     * Handle the exception.
     * 
     * @param exception
     *            the exception to be handled
     */
    public void handleError(Exception exception);

}
