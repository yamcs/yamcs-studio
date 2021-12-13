/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.connect;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StorePasswordDialog extends TitleAreaDialog {

    private YamcsConfiguration connection;

    private Text passwordText;

    private String password;

    public StorePasswordDialog(Shell parentShell, YamcsConfiguration connection) {
        super(parentShell);
        this.connection = connection;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Store Password");
        setMessage("Enter the password for the following Yamcs URL");
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Store Password");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var composite = (Composite) super.createDialogArea(parent);

        var contentArea = new Composite(composite, SWT.NONE);
        var gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        contentArea.setLayoutData(gd);
        var gl = new GridLayout(3, false);
        gl.marginLeft = 100;
        contentArea.setLayout(gl);

        var l = new Label(contentArea, SWT.NONE);
        l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        l.setText("Server URL:");
        l.setLayoutData(new GridData());
        l = new Label(contentArea, SWT.NONE);
        l.setText(connection.getURL());
        l.setLayoutData(new GridData());
        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        l = new Label(contentArea, SWT.NONE);
        l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        l.setText("User:");
        l.setLayoutData(new GridData());
        l = new Label(contentArea, SWT.NONE);
        l.setText(connection.getUser());
        l.setLayoutData(new GridData());
        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        l = new Label(contentArea, SWT.NONE);
        l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        l.setText("Password:");
        l.setLayoutData(new GridData());

        passwordText = new Text(contentArea, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData();
        gd.widthHint = 200;
        passwordText.setLayoutData(gd);
        passwordText.addListener(SWT.KeyUp, evt -> updateState());
        passwordText.setFocus();
        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);
        return composite;
    }

    @Override
    protected void okPressed() {
        password = passwordText.getText();
        super.okPressed();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateState();
    }

    private void updateState() {
        var okButton = getButton(IDialogConstants.OK_ID);
        var hasPassword = passwordText.getText() != null && !passwordText.getText().isEmpty();
        okButton.setEnabled(hasPassword);
    }

    public String getPassword() {
        return password;
    }
}
