package org.csstudio.yamcs.commanding;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AddTelecommandDialog extends TitleAreaDialog {

    public AddTelecommandDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    public void create() {
        super.create();
        setTitle("Send a telecommand");
        //setMessage("informative message");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(layout);

        Label lblCommand = new Label(container, SWT.NONE);
        lblCommand.setText("Template");

        Combo commandCombo = new Combo(container, SWT.BORDER);
        commandCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        commandCombo.setItems(new String[] {"a", "b", "c"});
        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        //createButton(parent, , "Validate", true);
        createButton(parent, IDialogConstants.OK_ID, "Send", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}
