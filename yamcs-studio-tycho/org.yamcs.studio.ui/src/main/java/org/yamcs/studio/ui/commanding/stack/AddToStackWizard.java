package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

public class AddToStackWizard extends Wizard {

    private Telecommand command;

    @Override
    public String getWindowTitle() {
        return "Add Command";
    }

    @Override
    public void addPages() {
        command = new Telecommand();
        addPage(new AddToStackWizardPage1(command));
        addPage(new AddToStackWizardPage2(command));
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        // NOP. Page2 is dynamic, and needs to be created *after* page1
    }

    public Telecommand getTelecommand() {
        return command;
    }
}
