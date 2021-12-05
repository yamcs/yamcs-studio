/********************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.logging.Logger;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler to handle the select parent command which has a key binding of Ctrl+R.
 */
public class SelectParentHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(SelectParentHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        GraphicalViewer viewer = HandlerUtil.getActivePart(event).getAdapter(
                GraphicalViewer.class);
        if (viewer == null) {
            return null;
        }

        ISelection currentSelection = viewer.getSelection();

        if (currentSelection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) currentSelection)
                    .getFirstElement();
            if (element instanceof AbstractBaseEditPart
                    && !(element instanceof DisplayEditpart)) {
                if (((AbstractBaseEditPart) element).getParent().isSelectable()) {
                    ((AbstractBaseEditPart) element).getViewer().select(
                            ((AbstractBaseEditPart) element).getParent());
                } else {
                    log.warning("Parent of the selected widget is unselectable. Its grandparent may be locked.");
                }
            }
        }

        return null;
    }

}
