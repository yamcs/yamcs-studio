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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends TitleAreaDialog {

    private String description;
    private String username;

    private Text userText;
    private Text passwordText;

    private String user;
    private String password;

    public LoginDialog(Shell parentShell, String description, String username) {
        super(parentShell);
        this.description = description;
        this.username = username;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Connect to " + description);
        setMessage("Please provide your credentials");
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(description);
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
        l.setText("User:");
        l.setLayoutData(new GridData());
        userText = new Text(contentArea, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 200;
        userText.setLayoutData(gd);
        userText.setText(username);
        userText.addListener(SWT.KeyUp, evt -> this.updateState());

        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        l = new Label(contentArea, SWT.NONE);
        l.setText("Password:");
        l.setLayoutData(new GridData());

        passwordText = new Text(contentArea, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData();
        gd.widthHint = 200;
        passwordText.setLayoutData(gd);
        passwordText.addListener(SWT.KeyUp, evt -> this.updateState());

        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        passwordText.setFocus();
        return composite;
    }

    @Override
    protected void okPressed() {
        user = userText.getText();
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
        var hasUsername = userText.getText() != null && !userText.getText().isEmpty();
        var hasPassword = passwordText.getText() != null && !passwordText.getText().isEmpty();
        okButton.setEnabled(hasUsername && hasPassword);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
