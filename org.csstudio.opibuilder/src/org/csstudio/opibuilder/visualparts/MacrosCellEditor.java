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

import java.util.LinkedHashMap;

import org.csstudio.opibuilder.util.MacrosInput;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cellEditor for macros property descriptor.
 */
public class MacrosCellEditor extends AbstractDialogCellEditor {

    private MacrosInput macrosInput;

    public MacrosCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {

        MacrosInputDialog dialog = new MacrosInputDialog(parentShell, macrosInput, dialogTitle);
        if (dialog.open() == Window.OK) {
            macrosInput = dialog.getResult();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return macrosInput != null;
    }

    @Override
    protected Object doGetValue() {
        return macrosInput;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof MacrosInput))
            macrosInput = new MacrosInput(new LinkedHashMap<String, String>(), true);
        else
            macrosInput = (MacrosInput) value;

    }

}
