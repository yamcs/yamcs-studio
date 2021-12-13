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

import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Table;

/**
 * The {@link EditingSupport} for the value columns of the property table.
 */
public class PropertiesEditingSupport extends EditingSupport {

    private final Table table;

    public PropertiesEditingSupport(ColumnViewer viewer, Table table) {
        super(viewer);
        this.table = table;
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        AbstractWidgetProperty property;
        if ((property = getSelectedProperty()) != null) {
            return property.getPropertyDescriptor().createPropertyEditor(table);
        }
        return null;
    }

    private AbstractWidgetProperty getSelectedProperty() {
        var selection = (IStructuredSelection) getViewer().getSelection();
        if (selection.getFirstElement() instanceof AbstractWidgetProperty) {
            var property = (AbstractWidgetProperty) selection.getFirstElement();
            return property;
        }
        return null;
    }

    @Override
    protected Object getValue(Object element) {
        if (element instanceof AbstractWidgetProperty) {
            return ((AbstractWidgetProperty) element).getPropertyValue();
        }

        return null;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof AbstractWidgetProperty) {
            var prop = (AbstractWidgetProperty) element;
            prop.setPropertyValue(value);
            getViewer().refresh();
        }
    }
}
