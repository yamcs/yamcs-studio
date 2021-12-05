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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cellEditor for macros property descriptor.
 */
public class StringListCellEditor extends AbstractDialogCellEditor {

    private List<String> data;

    public StringListCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {

        StringListEditDialog dialog = new StringListEditDialog(parentShell, data, dialogTitle);
        if (dialog.open() == Window.OK) {
            data = dialog.getResult();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return data != null;
    }

    @Override
    protected Object doGetValue() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof List))
            data = new ArrayList<String>();
        else
            data = (List<String>) value;

    }

}
