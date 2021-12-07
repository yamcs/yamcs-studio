/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.csstudio.ui.util.swt.stringtable.RowEditDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that allows user edit the macros.
 */
public class MacroEditDialog extends RowEditDialog {

    private Text titleText, detailsText;

    public MacroEditDialog(Shell parentShell) {
        super(parentShell);

    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit Macro");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parent_composite = (Composite) super.createDialogArea(parent);
        var composite = new Composite(parent_composite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));
        GridData gd;

        var titleLable = new Label(composite, 0);
        titleLable.setText("Name");
        titleLable.setLayoutData(new GridData());

        titleText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        titleText.setText(rowData[0]);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd.widthHint = 300;
        titleText.setLayoutData(gd);

        var detailsLable = new Label(composite, SWT.NONE);
        detailsLable.setText("Value");
        detailsLable.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        detailsText = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd.widthHint = 300;
        detailsText.setLayoutData(gd);
        detailsText.setText(rowData[1]);

        return parent_composite;
    }

    @Override
    protected void okPressed() {
        rowData[0] = titleText == null ? "" : titleText.getText().trim();
        rowData[1] = detailsText == null ? "" : detailsText.getText().trim();
        super.okPressed();
    }

}
