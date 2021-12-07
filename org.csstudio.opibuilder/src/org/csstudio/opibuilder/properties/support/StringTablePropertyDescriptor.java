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

import java.util.Arrays;

import org.csstudio.opibuilder.properties.StringTableProperty.TitlesProvider;
import org.csstudio.opibuilder.visualparts.StringTableCellEditor;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Descriptor for a property that has a value which should be edited with a String Table cell editor.
 */
public final class StringTablePropertyDescriptor extends TextPropertyDescriptor {

    private String displayName;
    private TitlesProvider columnTitles;
    private CellEditorType[] cellEditorTypes;
    private Object[] cellEditorDatas;

    /**
     * Standard constructor.
     *
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public StringTablePropertyDescriptor(Object id, String displayName, TitlesProvider tilesProvider,
            CellEditorType[] cellEditorTypes, Object[] cellEditorDatas) {
        super(id, displayName);
        this.displayName = displayName;
        this.columnTitles = tilesProvider;
        this.cellEditorTypes = cellEditorTypes;
        this.cellEditorDatas = cellEditorDatas;
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element == null) {
                    return "";
                } else if (!(element instanceof String[][])) {
                    return element.toString();
                }
                var stringTable = (String[][]) element;
                if (stringTable.length > 0) {
                    return Arrays.toString(stringTable[0]) + (stringTable.length > 1 ? "..." : "");
                }
                return "";
            }
        });
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new StringTableCellEditor(parent, "Edit " + displayName, columnTitles, cellEditorTypes,
                cellEditorDatas);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
