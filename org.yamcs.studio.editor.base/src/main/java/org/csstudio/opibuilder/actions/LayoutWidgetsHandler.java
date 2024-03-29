/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.editor.OPIEditor;
import org.csstudio.opibuilder.editparts.AbstractLayoutEditpart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * Handler to handle the layout widgets command which has a key binding of Ctrl+L.
 */
public class LayoutWidgetsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        var page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        var activeEditor = page.getActiveEditor();

        if (activeEditor instanceof OPIEditor) {
            var currentSelection = ((GraphicalViewer) ((OPIEditor) activeEditor).getAdapter(GraphicalViewer.class))
                    .getSelection();
            if (currentSelection instanceof IStructuredSelection) {
                var element = ((IStructuredSelection) currentSelection).getFirstElement();
                if (element instanceof AbstractLayoutEditpart) {
                    var commandStack = (CommandStack) ((OPIEditor) activeEditor).getAdapter(CommandStack.class);
                    if (commandStack != null) {
                        LayoutWidgetsImp.run((AbstractLayoutEditpart) element, commandStack);
                    }
                }
            }
        } else {
            return null;
        }

        return null;
    }
}
