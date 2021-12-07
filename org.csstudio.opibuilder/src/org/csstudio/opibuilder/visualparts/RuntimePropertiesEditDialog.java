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

import org.csstudio.opibuilder.datadefinition.PropertyData;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Editing runtime properties dialog.
 */
public class RuntimePropertiesEditDialog extends Dialog {

    private TableViewer propertiesViewer;
    private PropertyData[] propertyDataArray;

    public RuntimePropertiesEditDialog(Shell parentShell, AbstractWidgetModel widgetModel) {
        super(parentShell);
        var runningPropertyList = widgetModel.getRuntimePropertyList();
        if (runningPropertyList != null) {
            var i = 0;
            for (AbstractWidgetProperty prop : runningPropertyList) {
                if (prop.isVisibleInPropSheet()) {
                    i++;
                }
            }
            propertyDataArray = new PropertyData[i];
            i = 0;
            for (AbstractWidgetProperty prop : runningPropertyList) {
                if (prop.isVisibleInPropSheet()) {
                    propertyDataArray[i++] = new PropertyData(prop, prop.getPropertyValue());
                }
            }
        } else {
            propertyDataArray = new PropertyData[0];
        }
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    public PropertyData[] getOutput() {
        return propertyDataArray;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Configure runtime properties");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parent_Composite = (Composite) super.createDialogArea(parent);
        var rightComposite = new Composite(parent_Composite, SWT.NONE);
        rightComposite.setLayout(new GridLayout(1, false));
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 350;
        rightComposite.setLayoutData(gd);

        propertiesViewer = createPropertiesViewer(rightComposite);

        propertiesViewer.setInput(propertyDataArray);

        return parent_Composite;

    }

    private TableViewer createPropertiesViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        var tvColumn = new TableViewerColumn(viewer, SWT.NONE);
        tvColumn.getColumn().setText("Property");
        tvColumn.getColumn().setMoveable(false);
        tvColumn.getColumn().setWidth(150);
        tvColumn = new TableViewerColumn(viewer, SWT.NONE);
        tvColumn.getColumn().setText("Value");
        tvColumn.getColumn().setMoveable(false);
        tvColumn.getColumn().setWidth(200);
        EditingSupport editingSupport = new PropertyDataEditingSupport(viewer, viewer.getTable());
        tvColumn.setEditingSupport(editingSupport);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new PropertyDataLabelProvider());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

}

/**
 * The {@link EditingSupport} for the value columns of the property table.
 */
class PropertyDataEditingSupport extends EditingSupport {

    /**
     * The {@link Table} where this {@link EditingSupport} is embedded.
     */
    private final Table table;

    public PropertyDataEditingSupport(ColumnViewer viewer, Table table) {
        super(viewer);
        this.table = table;
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        PropertyData propertyData;
        if ((propertyData = getSelectedProperty()) != null) {
            return propertyData.property.getPropertyDescriptor().createPropertyEditor(table);
        }
        return null;
    }

    private PropertyData getSelectedProperty() {
        var selection = (IStructuredSelection) this.getViewer().getSelection();
        if (selection.getFirstElement() instanceof PropertyData) {
            var property = (PropertyData) selection.getFirstElement();
            return property;
        }
        return null;
    }

    @Override
    protected Object getValue(Object element) {
        if (element instanceof PropertyData) {
            return ((PropertyData) element).tmpValue;
        }

        return null;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof PropertyData) {
            var prop = (PropertyData) element;
            prop.tmpValue = value;
            getViewer().refresh();
        }
    }
}

/**
 * The {@link LabelProvider} for the properties table.
 */
class PropertyDataLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 1 && element instanceof PropertyData) {
            var propertyData = (PropertyData) element;

            try {
                return propertyData.property.getPropertyDescriptor().getLabelProvider().getImage(propertyData.tmpValue);
            } catch (NullPointerException e) {
            }

        }
        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof PropertyData) {
            var propertyData = (PropertyData) element;
            if (columnIndex == 0) {
                return propertyData.property.getDescription();
            }
            try {
                return propertyData.property.getPropertyDescriptor().getLabelProvider().getText(propertyData.tmpValue);
            } catch (NullPointerException e) {
            }
        }
        if (element != null) {
            return element.toString();
        }
        return "error";
    }
}
