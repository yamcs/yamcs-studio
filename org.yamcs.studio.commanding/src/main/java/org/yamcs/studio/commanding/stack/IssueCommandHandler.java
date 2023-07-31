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

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.client.Command;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class IssueCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        var shell = HandlerUtil.getActiveShellChecked(event);

        var sel = HandlerUtil.getCurrentStructuredSelection(event);
        var futures = new ArrayList<CompletableFuture<?>>();
        for (var o : sel.toArray()) {
            var command = (StackedCommand) o;
            try {
                var future = issueCommand(shell, commandStackView, command);
                // Await response, also for sequence reasons
                future.get();
                futures.add(future);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new ExecutionException("Failed to issue command", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).whenComplete((allDone, exc) -> {
                if (exc == null) {
                    commandStackView.selectNextCommand();
                }
            });
        }

        return null;
    }

    private CompletableFuture<Command> issueCommand(Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {
        var qname = command.getName();

        var processor = YamcsPlugin.getProcessorClient();
        var builder = processor.prepareCommand(qname).withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());

        if (command.getComment() != null) {
            builder.withComment(command.getComment());
        }
        command.getExtra().forEach((option, value) -> {
            builder.withExtra(option, value);
        });
        command.getAssignments().forEach((argument, value) -> {
            if (!argument.hasInitialValue() || command.isDefaultChanged(argument)) {
                builder.withArgument(argument.getName(), value);
            }
        });

        return builder.issue().whenComplete((response, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    log.info("Issued " + qname);
                    command.setStackedState(StackedState.ISSUED);
                    command.updateExecutionState(response);

                    var alreadyReceivedUpdate = view.getCommandExecution(response.getId());
                    if (alreadyReceivedUpdate != null) {
                        command.updateExecutionState(alreadyReceivedUpdate);
                    }

                    view.refreshState();
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.refreshState();
                });
            }
        });
    }
}
