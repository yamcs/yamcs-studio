package org.yamcs.studio.ui.alphanum;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

public class ShowColumnsWizard extends Wizard {

    private ChooseColumnsPage page;
    private List<String> columns;
    
    public ShowColumnsWizard(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public boolean performFinish() {
        if (page.getColumns() != null && !page.getColumns().isEmpty())
            return true;
        return false;
    }

    @Override
    public void addPages() {
        page = new ChooseColumnsPage(columns);
        addPage(page);

    }

    public List<String> getColumns() {
        return page.getColumns();
    }


}
