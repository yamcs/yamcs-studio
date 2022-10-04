/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class RefreshAllDisplaysHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RefreshAllDisplaysHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var workbench = PlatformUI.getWorkbench();
        for (var window : workbench.getWorkbenchWindows()) {
            for (var page : window.getPages()) {
                for (var reference : page.getViewReferences()) {
                    var viewPart = reference.getView(false);
                    if (viewPart instanceof IOPIRuntime) {
                        refreshDisplay((IOPIRuntime) viewPart);
                    }
                }
                for (var reference : page.getEditorReferences()) {
                    var editorPart = reference.getEditor(false);
                    if (editorPart instanceof IOPIRuntime) {
                        refreshDisplay((IOPIRuntime) editorPart);
                    }
                }
            }
        }
        return null;
    }

    private static void refreshDisplay(IOPIRuntime opiRuntime) {
        try {
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            log.log(Level.SEVERE, "Failed to refresh OPI", e);
        }
    }
}
