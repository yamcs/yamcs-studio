/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.properties;

import org.csstudio.opibuilder.visualparts.AbstractDialogCellEditor;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel;
import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for {@link ColorMap}
 */
public class ColorMapCellEditor extends AbstractDialogCellEditor {

    private ColorMap colorMap;
    private IntensityGraphModel widgetModel;

    public ColorMapCellEditor(Composite parent, String title, IntensityGraphModel widgetModel) {
        super(parent, title);
        this.widgetModel = widgetModel;
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new ColorMapEditDialog(parentShell, colorMap, dialogTitle, widgetModel.getMinimum(),
                widgetModel.getMaximum());
        if (dialog.open() == Window.OK) {
            colorMap = dialog.getOutput();
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return colorMap != null;
    }

    @Override
    protected Object doGetValue() {
        return colorMap;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof ColorMap)) {
            colorMap = new ColorMap(PredefinedColorMap.GrayScale, true, true);
        } else {
            colorMap = (ColorMap) value;
        }
    }

}
