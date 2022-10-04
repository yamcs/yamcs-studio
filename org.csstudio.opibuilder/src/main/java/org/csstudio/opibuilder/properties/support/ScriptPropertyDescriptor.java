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

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.visualparts.ScriptsInputCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property descriptor for script.
 */
public class ScriptPropertyDescriptor extends TextPropertyDescriptor {

    private AbstractWidgetModel widgetModel;

    /**
     * Creates an property descriptor with the given id and display name.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public ScriptPropertyDescriptor(Object id, AbstractWidgetModel widgetModel, String displayName) {
        super(id, displayName);
        this.widgetModel = widgetModel;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new ScriptsInputCellEditor(parent, widgetModel, "Attach Scripts");
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
