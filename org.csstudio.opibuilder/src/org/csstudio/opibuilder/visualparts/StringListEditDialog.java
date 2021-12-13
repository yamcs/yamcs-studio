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

import org.csstudio.ui.util.swt.stringtable.StringTableEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The dialog for editing String List.
 */
public class StringListEditDialog extends Dialog {

    private String title;
    private List<String> contents;

    private StringTableEditor tableEditor;

    public StringListEditDialog(Shell parentShell, List<String> inputData, String dialogTitle) {
        super(parentShell);
        this.title = dialogTitle;
        this.contents = new ArrayList<String>();
        for (var item : inputData) {
            this.contents.add(item);
        }
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var container = (Composite) super.createDialogArea(parent);
        // Table editor should stretch to fill the dialog space, but
        // at least on OS X, it has some minimum size below which it
        // doesn't properly shrink.
        tableEditor = new StringTableEditor(container, contents);
        tableEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    public List<String> getResult() {
        return contents;
    }

    @Override
    protected void okPressed() {
        tableEditor.forceFocus();
        super.okPressed();
    }
}
