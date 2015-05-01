package org.yamcs.studio.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * This is a HACK because Eclipse 4.3 doesn't seem to show separators on the toolbar whatever I try
 */
public class ToolbarSpacer extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        Label bla = new Label(parent, SWT.NONE);
        bla.setText("   ");
        return bla;
    }
}
