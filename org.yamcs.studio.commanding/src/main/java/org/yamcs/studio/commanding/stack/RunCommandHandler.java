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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(RunCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShellChecked(event);
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var part = window.getActivePage().findView(CommandStackView.ID);
        var view = (CommandStackView) part;

        var sel = HandlerUtil.getCurrentStructuredSelection(event);

        try {
            var stack = CommandStack.getInstance();
            @SuppressWarnings("unchecked")
            var job = new RunCommandJob(shell, stack, sel.toList(), view);
            job.schedule();

            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(IJobChangeEvent event) {
                    Display.getDefault().asyncExec(() -> {
                        view.setFocus();
                    });
                }
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to run commands: " + e.getMessage(), e);
            MessageDialog.openError(shell, "Failed to run commands: ", e.getMessage());
        }

        return null;
    }
}
