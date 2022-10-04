/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.script;

import org.csstudio.opibuilder.util.DisplayUtils;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * The utility class to facilitate script programming in GUI operation.
 */
public class GUIUtil {

    /**
     * Open a password dialog to allow user to input password.
     *
     * @param dialogMessage
     *            the message on the dialog.
     * @param password
     *            the password
     * @return true if user has input the correct password and clicked OK button. false otherwise.
     */
    public static boolean openPasswordDialog(String dialogMessage, String password) {
        var shell = Display.getCurrent().getActiveShell();
        var dlg = new InputDialog(shell, "Password Input Dialog", dialogMessage, "", newText -> {
            if (newText.equals(password)) {
                return null;
            } else {
                return "Password error!";
            }
        }) {
            @Override
            protected int getInputTextStyle() {
                return SWT.SINGLE | SWT.PASSWORD;
            }
        };
        dlg.setBlockOnOpen(true);
        return dlg.open() == Window.OK;
    }

    /**
     * Open a dialog to ask for confirmation.
     *
     * @param dialogMessage
     *            the message on the dialog.
     * @return true if user has clicked the YES button. False otherwise.
     */
    public static boolean openConfirmDialog(String dialogMessage) {
        var mb = new MessageBox(DisplayUtils.getDefaultShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        mb.setMessage(dialogMessage);
        mb.setText("Confirm Dialog");
        var val = mb.open();
        return val == SWT.YES;
    }

    public static void openInformationDialog(String dialogMessage) {
        var shell = Display.getCurrent().getActiveShell();
        MessageDialog.openInformation(shell, "Information", dialogMessage);
    }

    public static void openWarningDialog(String dialogMessage) {
        var shell = Display.getCurrent().getActiveShell();
        MessageDialog.openWarning(shell, "Warning", dialogMessage);
    }

    public static void openErrorDialog(String dialogMessage) {
        var shell = Display.getCurrent().getActiveShell();
        MessageDialog.openError(shell, "Error", dialogMessage);
    }

    /**
     * Enter or exit full screen.
     */
    public static void fullScreen() {
        ScriptUtil.executeEclipseCommand("org.csstudio.opibuilder.actions.fullscreen");
    }
}
