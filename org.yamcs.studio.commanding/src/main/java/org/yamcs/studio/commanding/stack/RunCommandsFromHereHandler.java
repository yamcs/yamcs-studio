/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryView;

public class RunCommandsFromHereHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RunCommandsFromHereHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var part = window.getActivePage().findView(CommandStackView.ID);
        var commandStackView = (CommandStackView) part;
        var commandHistoryView = (CommandHistoryView) window.getActivePage().findView(CommandHistoryView.ID);

        // lock scroll during command stack execution, or this would slow down the UI
        // refresh too much
        var service = PlatformUI.getWorkbench().getService(ICommandService.class);
        var command = service.getCommand("org.yamcs.studio.commanding.cmdhist.scrollLockCommand");
        var oldState = HandlerUtil.toggleCommandState(command);
        if (commandHistoryView != null) {
            commandHistoryView.enableScrollLock(true);
        }

        try {
            var job = new StackExecutorJob(shell, commandStackView);
            job.schedule();
        } catch (Exception e) {
            log.severe("Failed to run commands: " + e.getMessage());
            MessageDialog.openError(shell, "Failed to run commands: ", e.getMessage());
        }

        // restore scroll state of the command history view
        if (commandHistoryView != null) {
            commandHistoryView.enableScrollLock(oldState);
        }

        return null;
    }
}
