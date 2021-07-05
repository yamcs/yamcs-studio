package org.yamcs.studio.connect;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite contentArea = new Composite(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        contentArea.setLayoutData(gd);
        GridLayout gl = new GridLayout(3, false);
        gl.marginLeft = 100;
        contentArea.setLayout(gl);

        Label l = new Label(contentArea, SWT.NONE);
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
        Button okButton = getButton(IDialogConstants.OK_ID);
        boolean hasUsername = userText.getText() != null && !userText.getText().isEmpty();
        boolean hasPassword = passwordText.getText() != null && !passwordText.getText().isEmpty();
        okButton.setEnabled(hasUsername && hasPassword);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
