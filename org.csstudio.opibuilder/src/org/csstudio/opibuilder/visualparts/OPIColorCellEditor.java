/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.util.OPIColor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for OPIColor.
 */
public class OPIColorCellEditor extends AbstractDialogCellEditor {

    private OPIColor opiColor;

    public OPIColorCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new OPIColorDialog(parentShell, opiColor, dialogTitle);
        if (dialog.open() == Window.OK) {
            opiColor = dialog.getOutput();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return opiColor != null;
    }

    @Override
    protected Object doGetValue() {
        return opiColor;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof OPIColor)) {
            opiColor = new OPIColor("unknown");
        } else {
            opiColor = (OPIColor) value;
        }
    }

}
