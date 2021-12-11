/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util.helpers;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ComboHistoryHelperSample {
    private static Text text;
    private static Combo combo;
    private static ComboViewer comboViewer;

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        var display = Display.getDefault();
        var shell = new Shell();
        shell.setSize(450, 300);
        shell.setText("SWT Application");

        text = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
        text.setBounds(10, 44, 412, 201);

        comboViewer = new ComboViewer(shell, SWT.NONE);
        combo = comboViewer.getCombo();
        combo.setBounds(10, 10, 412, 28);

        new ComboHistoryHelper(null, "tag", combo) {
            @Override
            public void newSelection(String pvName) {
                // Need to use \r\n. Unbelievable!!!
                var newText = text.getText() + pvName + "\r\n";
                text.setText(newText);

            }
        };
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
