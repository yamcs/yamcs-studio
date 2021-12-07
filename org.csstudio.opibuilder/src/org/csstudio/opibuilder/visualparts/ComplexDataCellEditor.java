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

import org.csstudio.opibuilder.datadefinition.AbstractComplexData;
import org.csstudio.opibuilder.datadefinition.PropertyData;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cell editor for complex data.
 */
public class ComplexDataCellEditor extends AbstractDialogCellEditor {

    private AbstractComplexData complexData;

    public ComplexDataCellEditor(Composite parent, String title) {
        super(parent, title);
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {
        var dialog = new PropertiesEditDialog(parentShell, complexData.getAllProperties(), dialogTitle);

        if (dialog.open() == Window.OK) {
            complexData = complexData.getCopy();
            for (PropertyData propertyData : dialog.getOutput()) {
                complexData.setPropertyValue(propertyData.property.getPropertyID(), propertyData.tmpValue);
            }
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return complexData != null;
    }

    @Override
    protected Object doGetValue() {
        return complexData;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof AbstractComplexData)) {
            throw new RuntimeException(value + " is not instance of AbstractComplexData");
        } else {
            complexData = (AbstractComplexData) value;
        }
    }
}
