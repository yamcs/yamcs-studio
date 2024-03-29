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

import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The print mode selection dialog.
 */
public class PrintModeDialog extends Dialog {

    private Button tile, fitPage, fitWidth, fitHeight;

    public PrintModeDialog(Shell shell) {
        super(shell);
    }

    @Override
    protected void cancelPressed() {
        setReturnCode(-1);
        close();
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText("Select Print Mode");
        super.configureShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var composite = (Composite) super.createDialogArea(parent);

        tile = new Button(composite, SWT.RADIO);
        tile.setText("Tile");

        fitPage = new Button(composite, SWT.RADIO);
        fitPage.setText("Fit Page");
        fitPage.setSelection(true);

        fitWidth = new Button(composite, SWT.RADIO);
        fitWidth.setText("Fit Width");

        fitHeight = new Button(composite, SWT.RADIO);
        fitHeight.setText("Fit Height");

        return composite;
    }

    @Override
    protected void okPressed() {
        var returnCode = -1;
        if (tile.getSelection()) {
            returnCode = PrintFigureOperation.TILE;
        } else if (fitPage.getSelection()) {
            returnCode = PrintFigureOperation.FIT_PAGE;
        } else if (fitHeight.getSelection()) {
            returnCode = PrintFigureOperation.FIT_HEIGHT;
        } else if (fitWidth.getSelection()) {
            returnCode = PrintFigureOperation.FIT_WIDTH;
        }
        setReturnCode(returnCode);
        close();
    }
}
