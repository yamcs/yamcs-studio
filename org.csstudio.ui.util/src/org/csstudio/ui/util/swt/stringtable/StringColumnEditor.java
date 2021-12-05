/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.swt.stringtable;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;

/**
 * Editor for table with List<String>
 */
class StringColumnEditor extends EditingSupport {
    final private TableViewer table_viewer;

    public StringColumnEditor(final TableViewer viewer) {
        super(viewer);
        this.table_viewer = viewer;
    }

    @Override
    protected boolean canEdit(final Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(final Object element) {
        final Table parent = (Table) getViewer().getControl();
        return new TextCellEditor(parent);
    }

    @Override
    protected Object getValue(Object element) {
        if (element == StringTableContentProvider.ADD_ELEMENT)
            return "";
        final int index = ((Integer) element).intValue();

        @SuppressWarnings("unchecked")
        final List<String> items = (List<String>) table_viewer.getInput();
        return items.get(index);
    }

    @Override
    protected void setValue(Object element, Object value) {
        @SuppressWarnings("unchecked")
        final List<String> items = (List<String>) table_viewer.getInput();
        if (element == StringTableContentProvider.ADD_ELEMENT) {
            items.add(value.toString());
            getViewer().refresh();
            return;
        }
        // else
        final int index = ((Integer) element).intValue();
        items.set(index, value.toString());
        getViewer().refresh(element);
    }
}
