package org.yamcs.studio.runtime;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class RuntimeView extends ViewPart {

    private Sidebar sidebar;

    @Override
    public void createPartControl(Composite parent) {
        SashForm horizontalSash = new SashForm(parent, SWT.HORIZONTAL);
        GridLayoutFactory.fillDefaults().generateLayout(horizontalSash); // delte?

        horizontalSash.setSashWidth(20);

        sidebar = new Sidebar(horizontalSash, SWT.NONE);
        GridLayoutFactory.fillDefaults().generateLayout(sidebar);

        Label b = new Label(horizontalSash, SWT.NONE);
        b.setText("b");

        horizontalSash.setWeights(new int[] { 20, 80 });
    }

    @Override
    public void setFocus() {
        sidebar.setFocus();
    }
}
