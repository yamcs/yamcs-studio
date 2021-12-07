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

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.script.ScriptsInput;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for scripts input.
 */
public class ScriptsInputCellEditor extends AbstractDialogCellEditor {

    private ScriptsInput scriptsInput;

    private AbstractWidgetModel widgetModel;

    public ScriptsInputCellEditor(Composite parent, AbstractWidgetModel widgetModel, String title) {
        super(parent, title);
        this.widgetModel = widgetModel;
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new ScriptsInputDialog(parentShell, scriptsInput, dialogTitle, widgetModel);
        if (dialog.open() == Window.OK) {
            scriptsInput = new ScriptsInput(dialog.getScriptDataList());
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return scriptsInput != null;
    }

    @Override
    protected Object doGetValue() {
        return scriptsInput;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof ScriptsInput)) {
            scriptsInput = new ScriptsInput();
        } else {
            scriptsInput = (ScriptsInput) value;
        }
    }
}
