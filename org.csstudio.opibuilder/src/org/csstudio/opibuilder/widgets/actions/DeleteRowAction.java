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

import org.csstudio.opibuilder.widgets.editparts.TableEditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Delete a row from the table widget.
 */
public class DeleteRowAction implements IObjectActionDelegate {

    private IStructuredSelection selection;

    public DeleteRowAction() {
    }

    @Override
    public void run(IAction action) {
        var tableEditPart = getSelectedWidget();
        tableEditPart.getTable().deleteRow(tableEditPart.getMenuTriggeredCell().x);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
        }
    }

    private TableEditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof TableEditPart) {
            return (TableEditPart) selection.getFirstElement();
        } else {
            return null;
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {

    }

}
