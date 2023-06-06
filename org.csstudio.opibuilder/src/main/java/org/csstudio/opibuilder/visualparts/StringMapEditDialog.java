/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.csstudio.opibuilder.preferences.MacroEditDialog;
import org.csstudio.ui.util.swt.stringtable.StringTableEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jdom2.Verifier;

/**
 * The dialog for editing String Map.
 */
public class StringMapEditDialog extends Dialog {

    private String title;
    private List<String[]> contents;

    private StringTableEditor tableEditor;

    protected StringMapEditDialog(Shell parentShell, Map<String, String> inputData, String dialogTitle) {
        super(parentShell);
        title = dialogTitle;
        contents = new ArrayList<>();
        for (var entry : inputData.entrySet()) {
            contents.add(new String[] { entry.getKey(), entry.getValue() });
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
        tableEditor = new StringTableEditor(container, new String[] { "Name", "Value" }, new boolean[] { true, true },
                contents, new MacroEditDialog(getShell()), new int[] { 150, 150 });
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

    public Map<String, String> getResult() {
        var result = new LinkedHashMap<String, String>();
        for (var row : contents) {
            result.put(row[0], row[1]);
        }
        return result;
    }

    @Override
    protected void okPressed() {
        tableEditor.forceFocus(); // this can help the last edit value applied.
        String reason;
        for (var row : contents) {
            reason = Verifier.checkElementName(row[0]);
            if (reason != null) {
                MessageDialog.openError(getShell(), "Illegal Argument Name",
                        NLS.bind("{0} is not a valid argument name.\n {1}", row[0], reason));
                return;
            }
        }
        super.okPressed();
    }
}
