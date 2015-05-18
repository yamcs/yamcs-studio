package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.part.ViewPart;

public class CommandStackView extends ViewPart {

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        ManagedForm managedForm = new ManagedForm(parent);
        CommandStackMasterDetailsBlock block = new CommandStackMasterDetailsBlock(this);
        block.createContent(managedForm);
    }

    @Override
    public void setFocus() {
    }
}
