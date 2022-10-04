/*******************************************************************************
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class PythonScriptCellEditor extends AbstractDialogCellEditor {

    private String stringValue;

    public PythonScriptCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new PythonScriptEditDialog(parentShell, stringValue, dialogTitle);
        if (dialog.open() == Window.OK) {
            stringValue = dialog.getResult();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return stringValue != null;
    }

    @Override
    protected Object doGetValue() {
        return stringValue;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null) {
            stringValue = "";
        } else {
            stringValue = value.toString();
        }
    }
}
