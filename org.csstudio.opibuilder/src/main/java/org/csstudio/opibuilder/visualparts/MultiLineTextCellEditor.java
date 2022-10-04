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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog cell editor for multiline text editing.
 */
public class MultiLineTextCellEditor extends AbstractDialogCellEditor {

    private String stringValue;

    public MultiLineTextCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new MultilineTextEditDialog(parentShell, stringValue, dialogTitle);
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
