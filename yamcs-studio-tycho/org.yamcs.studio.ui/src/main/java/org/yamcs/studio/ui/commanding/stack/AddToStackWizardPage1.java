package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AddToStackWizardPage1 extends WizardPage {

    public AddToStackWizardPage1() {
        super("Choose a command");
        setTitle("Choose a command");
        setDescription("blabla");
    }

    @Override
    public void createControl(Composite parent) {
        Label lbl = new Label(parent, SWT.NONE);
        lbl.setText("abc");

        setControl(parent);
        setPageComplete(false);
    }
}
