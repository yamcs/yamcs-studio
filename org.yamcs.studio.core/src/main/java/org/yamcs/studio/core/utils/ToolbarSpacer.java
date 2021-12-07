package org.yamcs.studio.core.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * A custom spacer. Eclipse separators look a bit crappy, and don't even seem to work in 4.3
 * 
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=413213
 */
public class ToolbarSpacer extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        var bla = new Label(parent, SWT.NONE);
        bla.setText("   ");
        return bla;
    }
}
