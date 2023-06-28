/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cellEditor for string map property descriptor.
 */
public class StringMapCellEditor extends AbstractDialogCellEditor {

    private Map<String, String> data;

    public StringMapCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {

        var dialog = new StringMapEditDialog(parentShell, data, dialogTitle);
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

    @Override
    @SuppressWarnings("unchecked")
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof Map)) {
            data = new LinkedHashMap<>();
        } else {
            data = (Map<String, String>) value;
        }
    }
}
