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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Insert a column on the table widget.
 */
public class InsertColumnAction implements IObjectActionDelegate {

    private class InsertColumnDialog extends Dialog {
        private boolean isBefore;
        private String columnTitle;

        /**
         * Create the dialog.
         *
         * @param parentShell
         */
        public InsertColumnDialog(Shell parentShell) {
            super(parentShell);
        }

        /**
         * Create contents of the dialog.
         *
         * @param parent
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            getShell().setText("Insert Column");
            var container = (Composite) super.createDialogArea(parent);

            var grpPosition = new Group(container, SWT.NONE);
            grpPosition.setText("Insert");
            var gd_grpPosition = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
            grpPosition.setLayoutData(gd_grpPosition);
            var fillLayout = new FillLayout(SWT.VERTICAL);
            fillLayout.marginHeight = 5;
            fillLayout.marginWidth = 5;
            fillLayout.spacing = 5;
            grpPosition.setLayout(fillLayout);

            var beforeRadio = new Button(grpPosition, SWT.RADIO);
            beforeRadio.setText("Before this column");
            beforeRadio.setSelection(true);
            isBefore = true;

            beforeRadio.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    isBefore = beforeRadio.getSelection();
                }
            });

            var afterRadio = new Button(grpPosition, SWT.RADIO);
            afterRadio.setText("After this column");

            var title = new Label(grpPosition, SWT.None);
            title.setText("Header");
            if (allowedHeaders == null) {
                var text = new Text(grpPosition, SWT.SINGLE | SWT.BORDER);
                text.addModifyListener(e -> columnTitle = text.getText());
            } else {
                var combo = new Combo(grpPosition, SWT.READ_ONLY);
                combo.setItems(allowedHeaders);
                combo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        columnTitle = combo.getText();
                    }
                });
            }

            return container;
        }

        public boolean isBefore() {
            return isBefore;
        }

        public String getColumnTitle() {
            return columnTitle;
        }
    }

    private IStructuredSelection selection;
    private IWorkbenchPart targetPart;
    private String[] allowedHeaders;

    public InsertColumnAction() {
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @Override
    public void run(IAction action) {
        var tableEditPart = getSelectedWidget();
        allowedHeaders = tableEditPart.getAllowedHeaders();
        var dialog = new InsertColumnDialog(targetPart.getSite().getShell());
        if (dialog.open() == Dialog.OK) {
            var before = dialog.isBefore();
            var index = 0;
            if (!tableEditPart.getTable().isEmpty()) {
                index = tableEditPart.getMenuTriggeredCell().y + (before ? 0 : 1);
            }
            tableEditPart.getTable().insertColumn(index);
            if (dialog.getColumnTitle() != null && !dialog.getColumnTitle().isEmpty()) {
                tableEditPart.getTable().setColumnHeader(index, dialog.getColumnTitle());
            }
        }
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
}
