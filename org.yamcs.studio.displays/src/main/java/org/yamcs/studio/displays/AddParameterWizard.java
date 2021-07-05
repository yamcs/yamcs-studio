package org.yamcs.studio.displays;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.yamcs.protobuf.Mdb.ParameterInfo;

public class AddParameterWizard extends Wizard {

    AddParameterPage page;

    @Override
    public boolean performFinish() {
        if (page.getParameter() != null && !page.getParameter().isEmpty())
            return true;
        return false;
    }

    @Override
    public void addPages() {
        page = new AddParameterPage();
        addPage(page);

    }

    public List<ParameterInfo> getParameter() {
        return page.getParameter();
    }

}
