/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class EditStackedCommandDialog extends TitleAreaDialog implements CommandOptionsValidityListener {

    private StackedCommand command;
    private CommandOptionsComposite composite;

    public EditStackedCommandDialog(Shell parentShell, StackedCommand command) {
        super(parentShell);
        this.command = command;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Edit Stacked Command");
        setMessage(AddToStackWizardPage1.getMessage(command.getMetaCommand()));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        composite = new CommandOptionsComposite(parent, SWT.NONE, command, this);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    @Override
    protected void okPressed() {
        composite.getAssignments().forEach((assignmentInfo, value) -> {
            command.addAssignment(assignmentInfo, value);
        });
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 500);
    }

    @Override
    public void validityUpdated(String invalidMessage) {
        setErrorMessage(invalidMessage);
        var okButton = getButton(OK);
        if (okButton != null) { // Null during initial setup
            okButton.setEnabled(invalidMessage == null);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(OK).setEnabled(getErrorMessage() == null);
    }
}
