/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.swt.stringtable;

import java.util.Arrays;
import java.util.List;

import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;

/**
 * Editor for table with multiple columns (List<String[]>)
 */
class StringMultiColumnsEditor extends EditingSupport {
    final private TableViewer table_viewer;
    final private int columnNo;
    final private int numOfColumns;
    private CellEditorType cellEditorType;
    private Object cellEditorData;

    public StringMultiColumnsEditor(TableViewer viewer, int numOfColumns, int columnNo,
            CellEditorType cellEditorType, Object cellData) {
        super(viewer);
        table_viewer = viewer;
        this.columnNo = columnNo;
        this.numOfColumns = numOfColumns;
        this.cellEditorType = cellEditorType;
        cellEditorData = cellData;
        if (cellEditorType == CellEditorType.CHECKBOX) {
            if (cellEditorData == null || !(cellEditorData instanceof String[])
                    || ((String[]) cellEditorData).length < 2) {
                cellEditorData = new String[] { "Yes", "No" };
            }
        }
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        var parent = (Table) getViewer().getControl();
        switch (cellEditorType) {
        case CHECKBOX:
            return new CheckboxCellEditor(parent) {
                @Override
                protected Object doGetValue() {
                    return (Boolean) super.doGetValue() ? ((String[]) cellEditorData)[1]
                            : ((String[]) cellEditorData)[0];
                }

                @Override
                protected void doSetValue(Object value) {
                    if (value.toString().toLowerCase().equals(((String[]) cellEditorData)[1].toLowerCase())) {
                        super.doSetValue(true);
                    } else {
                        super.doSetValue(false);
                    }
                }
            };
        case DROPDOWN:
            return new ComboBoxCellEditor(parent, (String[]) cellEditorData, SWT.NONE) {
                @Override
                protected Object doGetValue() {
                    return ((CCombo) getControl()).getText();
                }

                @Override
                protected void doSetValue(Object value) {
                    ((CCombo) getControl()).setText(value.toString());
                }
            };

        default:
            return new TextCellEditor(parent);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getValue(Object element) {

        if (element == StringTableContentProvider.ADD_ELEMENT) {
            return "";
        }
        var index = ((Integer) element).intValue();
        var items = (List<String[]>) table_viewer.getInput();
        if (columnNo < items.get(index).length) {
            return items.get(index)[columnNo];
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setValue(Object element, Object value) {
        var items = (List<String[]>) table_viewer.getInput();
        String[] rowData;
        if (element == StringTableContentProvider.ADD_ELEMENT) {
            rowData = new String[numOfColumns];
            Arrays.fill(rowData, "");
            rowData[columnNo] = value.toString();
            items.add(rowData);
            getViewer().refresh();
            return;
        }
        // else
        var index = ((Integer) element).intValue();
        rowData = items.get(index);
        if (columnNo >= rowData.length) {
            var newRowData = new String[columnNo + 1];
            var i = 0;
            for (; i < rowData.length; i++) {
                newRowData[i] = rowData[i];
            }
            for (; i < newRowData.length; i++) {
                newRowData[i] = "";
            }
            rowData = newRowData;
        }
        rowData[columnNo] = value.toString();
        items.set(index, rowData);
        getViewer().refresh(element);
    }
}
