/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIFont;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for OPIFont
 */
public class OPIFontCellEditor extends AbstractDialogCellEditor {

    private OPIFont opiFont;

    public OPIFontCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new OPIFontDialog(parentShell, opiFont, dialogTitle);
        if (dialog.open() == Window.OK) {
            opiFont = dialog.getOutput();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return opiFont != null;
    }

    @Override
    protected Object doGetValue() {
        return opiFont;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof OPIFont)) {
            opiFont = MediaService.getInstance().getOPIFont("unknown");
        } else {
            opiFont = (OPIFont) value;
        }
    }
}
