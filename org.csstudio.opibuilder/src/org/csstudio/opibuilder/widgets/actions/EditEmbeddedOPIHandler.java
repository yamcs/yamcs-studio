/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.editparts.LinkingContainerEditpart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class EditEmbeddedOPIHandler extends AbstractHandler implements IHandler {

    private static final String OPI_EDITOR_ID = "org.csstudio.opibuilder.OPIEditor";

    /**
     * Determine the widget that was the object of the mouse click. If it can be established to be a
     * LinkingContainerEditpart, extract the path of the embedded opi and request opening an OPIEditor with this file.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IPath path = null;
        LinkingContainerEditpart linkingContainer = null;

        var selection = HandlerUtil.getActiveMenuSelection(event);
        if (selection instanceof IStructuredSelection) {
            var structuredSelection = (IStructuredSelection) selection;
            var o = structuredSelection.getFirstElement();
            if (o instanceof LinkingContainerEditpart) {
                linkingContainer = (LinkingContainerEditpart) o;
                path = linkingContainer.getWidgetModel().getOPIFilePath();
            }
        }

        if (path != null) {
            var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                var page = window.getActivePage();
                if (page != null) {
                    try {
                        var editorInput = ResourceUtil.editorInputFromPath(path);
                        page.openEditor(editorInput, OPI_EDITOR_ID, true,
                                IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
                    } catch (PartInitException ex) {
                        ErrorHandlerUtil.handleError("Failed to open embedded OPI in editor", ex);
                    }
                }
            }
        }
        // required return value
        return null;
    }
}
