package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.Wizard;

public class AddToStackWizard extends Wizard {

    private AddToStackWizardPage1 page1;

    @Override
    public String getWindowTitle() {
        return "New Command";
    }

    @Override
    public void addPages() {
        page1 = new AddToStackWizardPage1();
        addPage(page1);
    }

    @Override
    public boolean performFinish() {
        return true;
    }
}
