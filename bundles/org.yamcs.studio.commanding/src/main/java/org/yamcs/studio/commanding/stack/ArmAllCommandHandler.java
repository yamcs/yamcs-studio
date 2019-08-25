package org.yamcs.studio.commanding.stack;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.IssueCommandRequest;
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.model.CommandingCatalogue;

public class ArmAllCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ArmAllCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        Shell shell = HandlerUtil.getActiveShellChecked(event);
        CommandStack stack = CommandStack.getInstance();

        int commandIndex = stack.getCommands().indexOf(stack.getActiveCommand());
        if (commandIndex < stack.getCommands().size()) {
            armAllCommands(shell, commandStackView, stack, commandIndex);
        }
        return null;
    }

    private void armAllCommands(Shell activeShell, CommandStackView view, CommandStack stack, int commandIndex)
            throws ExecutionException {
        StackedCommand command = stack.getCommands().get(commandIndex);
        IssueCommandRequest req = command.toIssueCommandRequest().setDryRun(true).build();

        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        String qname;
        try {
            qname = command.getSelectedAliasEncoded();
        } catch (UnsupportedEncodingException e1) {
            throw new ExecutionException(e1.getMessage());
        }

        catalogue.sendCommand("realtime", qname, req).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    boolean doArm = false;
                    SignificanceInfo significance = command.getMetaCommand().getSignificance();
                    switch (significance.getConsequenceLevel()) {
                    case WATCH:
                    case WARNING:
                    case DISTRESS:
                    case CRITICAL:
                    case SEVERE:
                        String level = Character.toUpperCase(significance.getConsequenceLevel().toString().charAt(0))
                                + significance.getConsequenceLevel().toString().substring(1);
                        view.refreshState();
                        if (MessageDialog.openConfirm(activeShell, "Confirm",
                                level + ": Are you sure you want to arm this command?\n" + "    "
                                        + command.toStyledString(view).getString() + "\n\n"
                                        + significance.getReasonForWarning())) {
                            doArm = true;
                        }
                        break;
                    case NONE:
                        doArm = true;
                        break;
                    default:
                        throw new IllegalStateException(
                                "Unexpected significance level " + significance.getConsequenceLevel());
                    }

                    if (doArm) {
                        log.info(String.format("Command armed %s", command));
                        command.setStackedState(StackedState.ARMED);

                        if (commandIndex + 1 == stack.getCommands().size()) {
                            view.refreshState();
                            MessageDialog.openInformation(activeShell, "Automatic Command Stack Armed",
                                    "The command stack is armed in automatic mode.");
                        } else {
                            try {
                                armAllCommands(activeShell, view, stack, commandIndex + 1);
                            } catch (ExecutionException e) {
                                log.severe("Not able to arm the command in automatic mode: " + e.toString());
                            }
                            // view.refreshState();
                        }
                    }
                });
            } else {
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    view.clearArm();
                    view.refreshState();
                });
            }
        });
    }

}
