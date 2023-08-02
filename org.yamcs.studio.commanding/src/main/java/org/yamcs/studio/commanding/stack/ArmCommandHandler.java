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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.common.util.concurrent.MoreExecutors;

public class ArmCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ArmCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var part = window.getActivePage().findView(CommandStackView.ID);
        var commandStackView = (CommandStackView) part;

        var shell = HandlerUtil.getActiveShellChecked(event);
        var sel = HandlerUtil.getCurrentStructuredSelection(event);
        var ctx = new Context(sel.size());
        for (var o : sel.toArray()) {
            var command = (StackedCommand) o;
            armCommand(ctx, shell, commandStackView, command);
        }

        return null;
    }

    private void armCommand(Context ctx, Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {
        if (command.isArmed()) {
            return;
        }

        var processorClient = YamcsPlugin.getProcessorClient();
        var qname = command.getName();

        var builder = processorClient.prepareCommand(qname).withDryRun(true)
                .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());

        if (command.getComment() != null) {
            builder.withComment(command.getComment());
        }
        command.getExtra().forEach((option, value) -> {
            builder.withExtra(option, value);
        });
        command.getAssignments().forEach((arg, value) -> {
            if (!arg.hasInitialValue() || command.isDefaultChanged(arg)) {
                builder.withArgument(arg.getName(), value);
            }
        });

        builder.issue(MoreExecutors.directExecutor()).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().syncExec(() -> {
                    var doArm = false;

                    if (ctx.canceled) {
                        return;
                    } else if (ctx.yesToAll) {
                        doArm = true;
                    } else {
                        var significance = command.getMetaCommand().getSignificance();
                        switch (significance.getConsequenceLevel()) {
                        case WATCH:
                        case WARNING:
                        case DISTRESS:
                        case CRITICAL:
                        case SEVERE:
                            var level = Character.toUpperCase(significance.getConsequenceLevel().toString().charAt(0))
                                    + significance.getConsequenceLevel().toString().substring(1);
                            var message = String.format("%s: Are you sure you want to arm this command?\n    %s\n\n%s",
                                    level, command.toStyledString(view).getString(),
                                    significance.getReasonForWarning());
                            var showYesToAll = ctx.commandCount > 1;
                            var dialog = new ConfirmDialogWithCancelDefault(activeShell, message, showYesToAll);
                            dialog.open();
                            if (dialog.isYes()) {
                                doArm = true;
                            } else if (dialog.isYesToAll()) {
                                doArm = true;
                                ctx.yesToAll = true;
                            } else {
                                ctx.canceled = true;
                                return;
                            }
                            break;
                        case NONE:
                            doArm = true;
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Unexpected significance level " + significance.getConsequenceLevel());
                        }
                    }

                    if (doArm) {
                        log.info(String.format("Command armed %s", command));
                        command.setStackedState(StackedState.ARMED);
                        view.refreshState();
                    }
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.refreshState();
                });
            }
        });
    }

    private static class ConfirmDialogWithCancelDefault extends MessageDialog {

        private static String[] YES_TO_ALL_LABELS = new String[] {
                IDialogConstants.YES_LABEL,
                IDialogConstants.YES_TO_ALL_LABEL,
                IDialogConstants.CANCEL_LABEL,
        };

        private static String[] YES_LABELS = new String[] {
                IDialogConstants.YES_LABEL,
                IDialogConstants.CANCEL_LABEL,
        };

        private boolean showYesToAll;

        public ConfirmDialogWithCancelDefault(Shell parentShell, String message, boolean showYesToAll) {
            super(parentShell, "Confirm Significant Command", null, message, MessageDialog.QUESTION,
                    showYesToAll ? YES_TO_ALL_LABELS : YES_LABELS,
                    showYesToAll ? 2 : 1);
            this.showYesToAll = showYesToAll;
            setShellStyle(getShellStyle() | SWT.SHEET);
        }

        public boolean isYes() {
            return getReturnCode() == 0;
        }

        public boolean isYesToAll() {
            if (showYesToAll) {
                return getReturnCode() == 1;
            } else {
                return false;
            }
        }
    }

    /**
     * Shared context for the current selection of commands.
     */
    private static class Context {
        private int commandCount;

        // Set/get on UI thread. Signals to stop arming
        private boolean canceled = false;

        // Set/get on UI thread. Signal to arm without prompting (in case of significant commands)
        private boolean yesToAll = false;

        Context(int commandCount) {
            this.commandCount = commandCount;
        }
    }
}
