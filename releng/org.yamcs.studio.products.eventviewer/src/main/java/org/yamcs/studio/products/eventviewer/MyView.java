package org.yamcs.studio.products.eventviewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class MyView extends ViewPart {

    @Override
    public void createPartControl(Composite parent) {
        Text text = new Text(parent, SWT.BORDER);
        text.setText("Imagine a fantastic user interface here");
        System.out.println("err");
    }

    @Override
    public void setFocus() {
    }
}
