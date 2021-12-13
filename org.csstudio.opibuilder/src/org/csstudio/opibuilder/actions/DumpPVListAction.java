/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import java.util.Arrays;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogLabelKeys;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Dump all PVs in the OPI.
 */
public class DumpPVListAction implements IObjectActionDelegate {

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;

    @Override
    public void run(IAction action) {
        var o = getSelectedWidget().getViewer().getEditPartRegistry()
                .get(getSelectedWidget().getWidgetModel().getRootDisplayModel());
        if (o instanceof DisplayEditpart) {
            var allRuntimePVNames = ((DisplayEditpart) o).getAllRuntimePVNames().toArray();

            Arrays.sort(allRuntimePVNames);
            new PVListDialog(targetPart.getSite().getShell(), allRuntimePVNames).open();
        }
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

    private AbstractBaseEditPart getSelectedWidget() {
        if (selection.getFirstElement() instanceof AbstractBaseEditPart) {
            return (AbstractBaseEditPart) selection.getFirstElement();
        } else {
            return null;
        }
    }

    private final class PVListDialog extends Dialog {

        private Object[] allPVNames;
        private String pvsText;

        protected PVListDialog(Shell parentShell, Object[] allRuntimePVNames) {
            super(parentShell);
            this.allPVNames = allRuntimePVNames;
            setShellStyle(getShellStyle() | SWT.RESIZE);
            var sb = new StringBuilder();
            var i = 0;
            for (var pv : allPVNames) {
                sb.append(pv);
                if (i < allPVNames.length - 1) {
                    sb.append("\n");
                }
                i++;
            }

            pvsText = sb.toString();
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText("PV List");
            getShell().setMinimumSize(200, 300);
            var container = (Composite) super.createDialogArea(parent);
            var layout = new FillLayout();
            layout.marginHeight = 10;
            layout.marginWidth = 5;
            container.setLayout(layout);
            var text = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            text.setText(pvsText);
            return container;
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            var copyButton = createButton(parent, IDialogConstants.DETAILS_ID, "Copy to Clipboard", false);
            copyButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    var cb = new Clipboard(parent.getDisplay());
                    var textTransfer = TextTransfer.getInstance();
                    cb.setContents(new Object[] { pvsText }, new Transfer[] { textTransfer });

                }
            });
            if (pvsText.isEmpty()) {
                copyButton.setEnabled(false);
            }

            // create OK button
            createButton(parent, IDialogConstants.OK_ID, JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY), true);
        }
    }
}
