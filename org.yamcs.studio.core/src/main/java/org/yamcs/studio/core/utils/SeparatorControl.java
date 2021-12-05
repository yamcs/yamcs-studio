package org.yamcs.studio.core.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * A custom separator. Eclipse don't seem to work in 4.3 when using menu contributions in the plugin.xml.
 *
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=413213
 */
public class SeparatorControl extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        Label bla = new Label(parent, SWT.VERTICAL | SWT.SEPARATOR);
        bla.setText("   ");
        return bla;
    }
}
