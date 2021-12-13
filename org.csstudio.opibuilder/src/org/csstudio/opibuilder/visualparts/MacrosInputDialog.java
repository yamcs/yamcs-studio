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
import java.util.LinkedHashMap;
import java.util.List;

import org.csstudio.opibuilder.preferences.MacroEditDialog;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jdom.Verifier;

/**
 * The dialog for editing macros.
 */
public class MacrosInputDialog extends Dialog {

    private String title;
    private List<String[]> contents;
    private boolean includeParentMacros;

    private StringTableEditor tableEditor;

    protected MacrosInputDialog(Shell parentShell, MacrosInput macrosInput, String dialogTitle) {
        super(parentShell);
        title = dialogTitle;
        contents = new ArrayList<>();
        for (var key : macrosInput.getMacrosMap().keySet()) {
            contents.add(new String[] { key, macrosInput.getMacrosMap().get(key) });
        }
        includeParentMacros = macrosInput.isInclude_parent_macros();

        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var container = (Composite) super.createDialogArea(parent);
        // Table editor should stretch to fill the dialog space, but
        // at least on OS X, it has some minimum size below which it
        // doesn't properly shrink.
        tableEditor = new StringTableEditor(container, new String[] { "Name", "Value" }, new boolean[] { true, true },
                contents, new MacroEditDialog(getShell()), new int[] { 150, 150 });
        tableEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        var checkBox = new Button(container, SWT.CHECK);
        checkBox.setSelection(includeParentMacros);
        checkBox.setText("Include macros from parent.");
        checkBox.setLayoutData(new GridData(SWT.FILL, 0, true, false));
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                includeParentMacros = checkBox.getSelection();
            }
        });
        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    public MacrosInput getResult() {
        var macrosMap = new LinkedHashMap<String, String>();
        for (var row : contents) {
            macrosMap.put(row[0], row[1]);
        }
        return new MacrosInput(macrosMap, includeParentMacros);
    }

    @Override
    protected void okPressed() {
        tableEditor.forceFocus(); // this can help the last edit value applied.
        String reason;
        for (var row : contents) {
            reason = Verifier.checkElementName(row[0]);
            if (reason != null) {
                MessageDialog.openError(getShell(), "Illegal Macro Name",
                        NLS.bind("{0} is not a valid Macro name.\n {1}", row[0], reason));
                return;
            }
        }
        super.okPressed();
    }
}
