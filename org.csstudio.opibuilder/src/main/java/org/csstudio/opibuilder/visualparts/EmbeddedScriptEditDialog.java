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

import org.csstudio.opibuilder.script.ScriptData;
import org.csstudio.opibuilder.script.ScriptService.ScriptType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.studio.languages.TMViewer;

/**
 * The dialog for embedded script editing.
 */
public class EmbeddedScriptEditDialog extends TrayDialog {

    private ScriptData scriptData;

    private Text nameText;
    private TMViewer scriptText;

    private Combo scriptTypeCombo;

    /**
     * Constructor.
     *
     * @param parentShell
     * @param scriptData
     *            the scriptData to be edited. null if a new scriptdata to be created.
     */
    public EmbeddedScriptEditDialog(Shell parentShell, ScriptData scriptData) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        if (scriptData != null) {
            this.scriptData = scriptData.getCopy();
        }
    }

    @Override
    protected void okPressed() {
        if (nameText.getText().trim().isEmpty()) {
            MessageDialog.openError(getShell(), "Error", "Script name cannot be empty");
            return;
        }
        if (scriptData == null) {
            scriptData = new ScriptData();
        }

        scriptData.setEmbedded(true);
        scriptData.setScriptName(nameText.getText());
        scriptData.setScriptText(scriptText.getText());
        scriptData.setScriptType(ScriptType.values()[scriptTypeCombo.getSelectionIndex()]);
        super.okPressed();
    }

    public ScriptData getResult() {

        return scriptData;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Edit Script");
    }

    /**
     * Creates 'wrapping' label with the given text.
     *
     * @param parent
     *            The parent for the label
     * @param text
     *            The text for the label
     */
    private void createLabel(Composite parent, String text) {
        var label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var dialogArea = (Composite) super.createDialogArea(parent);
        dialogArea.setLayout(new GridLayout(2, false));
        createLabel(dialogArea, "Name: ");
        var gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        nameText = new Text(dialogArea, SWT.BORDER);
        if (scriptData != null) {
            nameText.setText(scriptData.getScriptName());
        } else {
            nameText.setText("EmbeddedScript");
            nameText.selectAll();
        }
        nameText.setLayoutData(gd);
        createLabel(dialogArea, "Script Type: ");
        scriptTypeCombo = new Combo(dialogArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        scriptTypeCombo.setItems(ScriptType.stringValues());
        if (scriptData != null) {
            scriptTypeCombo.select(scriptData.getScriptType().ordinal());
        } else {
            scriptTypeCombo.select(0);
        }

        scriptTypeCombo.setLayoutData(gd);

        scriptText = new TMViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        var control = scriptText.getControl();
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.widthHint = 400;
        gd.heightHint = convertHeightInCharsToPixels(5);
        control.setLayoutData(gd);

        if (scriptData != null) {
            scriptText.setText(scriptData.getScriptText());
        } else {
            scriptText.setText("");
        }
        scriptText.loadJavaScriptGrammar();

        scriptTypeCombo.addListener(SWT.Selection, evt -> {
            if (scriptTypeCombo.getText().equals(ScriptType.PYTHON.toString())) {
                scriptText.loadPythonGrammar();
            } else {
                scriptText.loadJavaScriptGrammar();
            }
        });

        return this.dialogArea;
    }
}
