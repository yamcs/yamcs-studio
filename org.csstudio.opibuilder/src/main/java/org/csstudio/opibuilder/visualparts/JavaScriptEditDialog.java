/*******************************************************************************
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.languages.TMViewer;

/**
 * The dialog for editing multiline text.
 */
public class JavaScriptEditDialog extends Dialog {

    private String title;
    private String contents;
    private TMViewer text;

    protected JavaScriptEditDialog(Shell parentShell, String stringValue, String dialogTitle) {
        super(parentShell);
        title = dialogTitle;
        contents = stringValue;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var container = (Composite) super.createDialogArea(parent);
        var gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 600;
        gridData.heightHint = convertHeightInCharsToPixels(12);

        text = new TMViewer(container, null, null, false, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        var control = text.getControl();
        control.setLayoutData(gridData);

        text.setText(contents);
        text.loadJavaScriptGrammar();

        control.setFocus();
        control.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == '\r') { // Return key
                    if ((e.stateMask & SWT.CTRL) != 0) {
                        okPressed();
                    }
                }
            }
        });
        return container;
    }

    @Override
    protected void okPressed() {
        contents = text.getText();
        super.okPressed();
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    public String getResult() {
        return contents;
    }
}
