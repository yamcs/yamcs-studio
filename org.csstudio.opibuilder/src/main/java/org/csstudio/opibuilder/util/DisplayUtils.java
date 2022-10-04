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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DisplayUtils {

    /**
     * Returns a non-null instance of Display object. Tries to find the Display object for the current thread first and
     * if it fails tries to get:
     * <ul>
     * <li>Workbench display if the workbench running</li>
     * <li>Default display object</li>
     * </ul>
     *
     * @return non-null Display object
     */
    public static Display getDisplay() {
        var display = Display.getCurrent();
        if (display == null && PlatformUI.isWorkbenchRunning()) {
            display = PlatformUI.getWorkbench().getDisplay();
        }
        return display != null ? display : Display.getDefault();
    }

    /**
     * Attempts to return the default shell. If it cannot return the default shell, it returns the shell of the first
     * workbench window that has shell.
     */
    public static Shell getDefaultShell() {
        Shell shell = null;

        try {
            shell = Display.getDefault().getActiveShell();
        } catch (Exception e) {
            // ignore
        }

        try {
            if (shell == null) {
                var activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (activeWindow != null) {
                    shell = activeWindow.getShell();
                }

            }
        } catch (Exception e) {
            // ignore
        }

        if (shell == null) {
            var windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (var i = 0; shell == null && i < windows.length; i++) {
                shell = windows[i].getShell();
            }
        }

        return shell;
    }
}
