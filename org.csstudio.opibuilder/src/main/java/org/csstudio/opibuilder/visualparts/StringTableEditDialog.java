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
import java.util.Arrays;
import java.util.List;

import org.csstudio.ui.util.swt.stringtable.StringTableEditor;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor.CellEditorType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog for editing String Table.
 */
public class StringTableEditDialog extends Dialog {

    private String title;
    private String[] columnTitles;
    private List<String[]> contents;

    private StringTableEditor tableEditor;
    private Object[] cellEditorDatas;
    private CellEditorType[] cellEditorTypes;

    public StringTableEditDialog(Shell parentShell, List<String[]> inputData, String dialogTitle, String[] columnTitles,
            CellEditorType[] cellEditorTypes, Object[] cellEditorDatas) {
        super(parentShell);
        title = dialogTitle;
        this.columnTitles = columnTitles;
        this.cellEditorTypes = cellEditorTypes;
        this.cellEditorDatas = cellEditorDatas;
        contents = new ArrayList<>();
        contents.addAll(inputData);
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var container = (Composite) super.createDialogArea(parent);
        // Table editor should stretch to fill the dialog space, but
        // at least on OS X, it has some minimum size below which it
        // doesn't properly shrink.
        var columnWidths = new int[columnTitles.length];
        Arrays.fill(columnWidths, 80);
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 200;
        tableEditor = new StringTableEditor(container, columnTitles, null, contents, null, columnWidths,
                cellEditorTypes, cellEditorDatas);
        tableEditor.setLayoutData(gd);

        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    public List<String[]> getResult() {
        return contents;
    }

    @Override
    protected void okPressed() {
        tableEditor.forceFocus();
        super.okPressed();
    }
}
