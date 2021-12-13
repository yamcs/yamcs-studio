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

import org.csstudio.opibuilder.datadefinition.PropertyData;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog to edit an array of properties.
 */
public class PropertiesEditDialog extends Dialog {

    private TableViewer propertiesViewer;
    private PropertyData[] propertyDataArray;
    private String dialogTitle;

    public PropertiesEditDialog(Shell parentShell, AbstractWidgetProperty[] properties, String dialogTitle) {
        super(parentShell);
        this.dialogTitle = dialogTitle;
        propertyDataArray = new PropertyData[properties.length];
        var i = 0;
        for (var prop : properties) {
            propertyDataArray[i++] = new PropertyData(prop, prop.getPropertyValue());
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
        newShell.setText(dialogTitle);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parent_Composite = (Composite) super.createDialogArea(parent);
        var rightComposite = new Composite(parent_Composite, SWT.NONE);
        rightComposite.setLayout(new GridLayout(1, false));
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 320;
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
        tvColumn.getColumn().setWidth(150);
        EditingSupport editingSupport = new PropertyDataEditingSupport(viewer, viewer.getTable());
        tvColumn.setEditingSupport(editingSupport);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new PropertyDataLabelProvider());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }
}

//
//
/// **
// * The {@link EditingSupport} for the value columns of the property table.
// */
// class PropertyDataEditingSupport extends EditingSupport {
//
// /**
// * The {@link Table} where this {@link EditingSupport} is embedded.
// */
// private final Table table;
//
//
// /**
// * Constructor.
// *
// * @param viewer
// * The {@link ColumnViewer} for this
// * {@link EditingSupport}.
// * @param table
// * The {@link Table}
// */
// public PropertyDataEditingSupport(ColumnViewer viewer,
// final Table table) {
// super(viewer);
// this.table = table;
// }
//
// @Override
// protected boolean canEdit(Object element) {
// return true;
// }
//
// @Override
// protected CellEditor getCellEditor(Object element) {
// PropertyData propertyData;
// if((propertyData = getSelectedProperty()) != null){
// return propertyData.property.getPropertyDescriptor().createPropertyEditor(table);
// }
// return null;
// }
//
// private PropertyData getSelectedProperty(){
// IStructuredSelection selection = (IStructuredSelection) this
// .getViewer().getSelection();
// if(selection.getFirstElement() instanceof PropertyData){
// PropertyData property = (PropertyData) selection
// .getFirstElement();
// return property;
// }
// return null;
// }
//
//
// @Override
// protected Object getValue(Object element) {
// if (element instanceof PropertyData) {
// return ((PropertyData)element).tmpValue;
// }
//
// return null;
// }
//
// @Override
// protected void setValue(Object element, Object value) {
// if (element instanceof PropertyData) {
// PropertyData prop = (PropertyData) element;
// if (prop != null) {
// prop.tmpValue = value;
// getViewer().refresh();
// }
// }
// }
// }
//
//
/// **
// * The {@link LabelProvider} for the properties table.
// */
// class PropertyDataLabelProvider extends LabelProvider implements
// ITableLabelProvider {
//
// public Image getColumnImage(Object element,
// final int columnIndex) {
// if (columnIndex == 1 && element instanceof PropertyData) {
// PropertyData propertyData = (PropertyData) element;
//
// if (propertyData != null) {
// if (propertyData.property.getPropertyDescriptor().getLabelProvider() != null)
// return propertyData.property.getPropertyDescriptor().getLabelProvider().
// getImage(propertyData.tmpValue);
// }
// }
// return null;
// }
//
// public String getColumnText(Object element,
// final int columnIndex) {
// if (element instanceof PropertyData) {
// PropertyData propertyData = (PropertyData) element;
// if (columnIndex == 0) {
// return propertyData.property.getDescription();
// }
//
// if (propertyData != null && propertyData.property.getPropertyDescriptor().getLabelProvider() != null) {
// return propertyData.property.getPropertyDescriptor().getLabelProvider().getText(
// propertyData.tmpValue);
// }
// }
// if (element != null) {
// return element.toString();
// }
// return "error";
// }
//
//
// }
