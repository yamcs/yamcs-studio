/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ErrorHandlerUtil {

    /**
     * General error handle method.
     * 
     * @param message
     *            message of the error.
     * @param exception
     *            the exception.
     * @param writeToConsole
     *            true if message will output to console.
     * @param popErrorDialog
     *            true if an error dialog will popup. Must be called in UI thread if this is true.
     */
    public static void handleError(String message, Throwable exception, boolean writeToConsole,
            boolean popErrorDialog) {
        OPIBuilderPlugin.getLogger().log(Level.WARNING, message, exception);
        if (writeToConsole) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, message, exception);
        }
        if (popErrorDialog) {
            if (DisplayUtils.getDisplay() != null) {
                IStatus status = new Status(IStatus.ERROR, OPIBuilderPlugin.PLUGIN_ID, exception.getLocalizedMessage(),
                        exception);
                ExceptionDetailsErrorDialog.openError(DisplayUtils.getDisplay().getActiveShell(), "Error", message,
                        status);
            }
        }
    }

    /**
     * This method will call {@link #handleError(String, Throwable, boolean, boolean)} with writeToConsole as true and
     * popErrorDialog as false.
     */
    public static void handleError(String message, Throwable exception) {
        handleError(message, exception, true, false);
    }
}
