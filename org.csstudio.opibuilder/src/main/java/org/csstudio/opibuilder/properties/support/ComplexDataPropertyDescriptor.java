/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties.support;

import org.csstudio.opibuilder.visualparts.ComplexDataCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property descriptor for complex data property.
 */
public class ComplexDataPropertyDescriptor extends TextPropertyDescriptor {

    private String dialogTitle;

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     * @param dialogTitle
     *            title of the dialog.
     */
    public ComplexDataPropertyDescriptor(Object id, String displayName, String dialogTitle) {
        super(id, displayName);
        this.dialogTitle = dialogTitle;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new ComplexDataCellEditor(parent, dialogTitle);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
