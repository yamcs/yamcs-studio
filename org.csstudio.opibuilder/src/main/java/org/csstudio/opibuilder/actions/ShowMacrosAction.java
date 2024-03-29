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

import java.util.logging.Logger;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Show the predefined macros of the selected widget in console and message dialog.
 */
public class ShowMacrosAction implements IObjectActionDelegate {

    private static final Logger log = Logger.getLogger(ShowMacrosAction.class.getName());

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;

    @Override
    public void run(IAction action) {
        var widget = (AbstractWidgetModel) getSelectedWidget().getModel();
        var message = NLS.bind("The predefined macros of {0}:\n", widget.getName());
        var sb = new StringBuilder(message);
        var macroMap = OPIBuilderMacroUtil.getWidgetMacroMap(widget);
        for (var entry : macroMap.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + "\n");
        }
        sb.append("\n");
        sb.append("Note: Macros are loaded during OPI opening, so this won't reflect the macro changes after opening."
                + "To reflect the latest changes, please reopen the OPI and show macros again.");
        // show the dialog first, because on some linux systems the console print brings the workbench window to top,
        // blocking entire CSS
        MessageDialog.openInformation(targetPart.getSite().getShell(), "Predefined Macros", sb.toString());
        log.info(sb.toString());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    private EditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof EditPart) {
            return (EditPart) selection.getFirstElement();
        } else {
            return null;
        }
    }
}
