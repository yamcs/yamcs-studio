/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.widgets.editparts.GroupingContainerEditPart;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler to handle lock/unlock grouping container children which has a key binding of Ctrl+L.
 */
public class LockUnlockChildrenHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        var viewer = HandlerUtil.getActivePart(event).getAdapter(GraphicalViewer.class);
        if (viewer == null) {
            return null;
        }

        var currentSelection = viewer.getSelection();
        if (currentSelection instanceof IStructuredSelection) {
            var element = ((IStructuredSelection) currentSelection).getFirstElement();
            if (element instanceof GroupingContainerEditPart) {
                var commandStack = HandlerUtil.getActivePart(event).getAdapter(CommandStack.class);
                if (commandStack != null) {
                    commandStack.execute(LockUnlockChildrenAction
                            .createLockUnlockCommand(((GroupingContainerEditPart) element).getWidgetModel()));
                }
            }
        }

        return null;
    }

}
