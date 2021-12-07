/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.ContextSwitcher;

public class ChooseProcessorDialogHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ChooseProcessorDialogHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShellChecked(event);
        var dialog = new SwitchProcessorDialog(shell);
        if (dialog.open() == Window.OK) {
            var info = dialog.getProcessorInfo();
            if (info != null) {
                try {
                    new ProgressMonitorDialog(shell).run(true, true,
                            new ContextSwitcher(info.getInstance(), info.getName()));
                } catch (InvocationTargetException e) {
                    var cause = e.getCause();
                    log.log(Level.SEVERE, "Failed to switch processor", cause);
                    MessageDialog.openError(shell, "Failed to switch processor", cause.getMessage());
                } catch (InterruptedException e) {
                    log.info("Processor switch cancelled");
                }
            }
        }

        return null;
    }
}
