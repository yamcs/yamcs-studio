/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
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

/**Dialog that allows user edit the macros.
 * @author Xihui Chen
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
        final Composite parent_composite = (Composite) super.createDialogArea(parent);
        final Composite composite = new Composite(parent_composite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));
        GridData gd;

        final Label titleLable = new Label(composite, 0);
        titleLable.setText("Name");
        titleLable.setLayoutData(new GridData());

        titleText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        titleText.setText(rowData[0]);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd.widthHint = 300;
        titleText.setLayoutData(gd);

        final Label detailsLable = new Label(composite, SWT.NONE);
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
