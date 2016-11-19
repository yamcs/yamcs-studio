package org.yamcs.studio.core.ui.connections;

import static org.yamcs.studio.core.ui.utils.TextUtils.forceString;
import static org.yamcs.studio.core.ui.utils.TextUtils.isBlank;
import static org.yamcs.studio.core.ui.utils.TextUtils.nvl;

import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.studio.core.ui.utils.RCPUtils;

/**
 * Uses Eclipse {@link ILoginContext} to perform a JAAS-based login. This dialog
 * stays open until the user either successfully connects, or the dialog is
 * cancelled.
 */
public class LoginDialog extends TitleAreaDialog {

    private YamcsConfiguration conf;

    private Text user;
    private Text password;

    String newUser = null;
    String newPassword = null;

    public String getUser() {
        return newUser;
    }

    public String getPassword() {
        return newPassword;
    }

    public LoginDialog(Shell shell, YamcsConfiguration conf) {
        super(shell);
        this.conf = conf;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Connect to " + nvl(conf.getName(), conf.getPrimaryConnectionString()));
        setMessage("Please provide your credentials");
        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), getShell());
        Image titleImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(LoginDialog.class, "icons/yamcs_banner.png"));
        setTitleImage(titleImage);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(conf.getPrimaryConnectionString());
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
        user = new Text(contentArea, SWT.BORDER);
        gd = new GridData();
        gd.widthHint = 100;
        user.setLayoutData(gd);
        user.setText(forceString(conf.getUser()));
        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        l = new Label(contentArea, SWT.NONE);
        l.setText("Password:");
        l.setLayoutData(new GridData());

        password = new Text(contentArea, SWT.BORDER | SWT.PASSWORD);
        gd = new GridData();
        gd.widthHint = 100;
        password.setLayoutData(gd);
        l = new Label(contentArea, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        l.setLayoutData(gd);

        if (isBlank(user.getText()))
            user.setFocus();
        else
            password.setFocus();

        return composite;
    }

    @Override
    protected void okPressed() {
        newUser = user.getText();
        newPassword = password.getText();
        super.okPressed();
    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
    }

}
