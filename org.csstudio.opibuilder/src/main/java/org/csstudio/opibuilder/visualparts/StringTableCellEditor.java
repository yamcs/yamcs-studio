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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.properties.StringTableProperty.TitlesProvider;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * The cellEditor for macros property descriptor.
 */
public class StringTableCellEditor extends AbstractDialogCellEditor {

    private String[][] data;

    private TitlesProvider columnTitles;

    private CellEditorType[] cellEditorTypes;

    private Object[] cellEditorDatas;

    public StringTableCellEditor(Composite parent, String title, TitlesProvider columnTitles,
            CellEditorType[] cellEditorTypes, Object[] cellEditorDatas) {
        super(parent, title);
        this.columnTitles = columnTitles;
        this.cellEditorTypes = cellEditorTypes;
        this.cellEditorDatas = cellEditorDatas;
    }

    @Override
    protected void openDialog(Shell parentShell, String dialogTitle) {

        var dialog = new StringTableEditDialog(parentShell, arrayToList(data), dialogTitle, columnTitles.getTitles(),
                cellEditorTypes, cellEditorDatas);
        if (dialog.open() == Window.OK) {
            data = listToArray(dialog.getResult());
        }
    }

    @Override
    protected boolean shouldFireChanges() {
        return data != null;
    }

    @Override
    protected Object doGetValue() {
        return data;
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null || !(value instanceof String[][])) {
            data = new String[0][0];
        } else {
            data = (String[][]) value;
        }
    }

    private List<String[]> arrayToList(String[][] content) {
        List<String[]> input = new ArrayList<>();
        if (content.length <= 0) {
            return input;
        }
        var col = columnTitles.getTitles().length;
        for (var i = 0; i < content.length; i++) {
            var row = new String[col];
            for (var j = 0; j < col; j++) {
                if (j < content[i].length) {
                    row[j] = content[i][j];
                } else {
                    row[j] = "";
                }
            }
            input.add(row);
        }
        return input;
    }

    private String[][] listToArray(List<String[]> list) {
        var col = 0;
        if (list.size() > 0) {
            col = list.get(0).length;
        }
        var result = new String[list.size()][col];
        for (var i = 0; i < list.size(); i++) {
            for (var j = 0; j < col; j++) {
                result[i][j] = list.get(i)[j];
            }
        }
        return result;
    }
}
