package org.yamcs.studio.commanding.stack;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.client.processor.ProcessorClient.CommandBuilder;
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.YamcsPlugin;

public class ArmCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ArmCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        Shell shell = HandlerUtil.getActiveShellChecked(event);
        CommandStack stack = CommandStack.getInstance();
        StackedCommand command = stack.getActiveCommand();
        armCommand(shell, commandStackView, command);

        return null;
    }

    private void armCommand(Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {

        ProcessorClient processorClient = YamcsPlugin.getProcessorClient();
        String qname = command.getSelectedAlias();

        CommandBuilder builder = processorClient.prepareCommand(qname)
                .withDryRun(true)
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

        builder.issue().whenComplete((data, exc) -> {
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
                        String message = String.format("%s: Are you sure you want to arm this command?\n    %s\n\n%s",
                                level, command.toStyledString(view).getString(), significance.getReasonForWarning());
                        MessageDialog dialog = new ConfirmDialogWithCancelDefault(activeShell, "Confirm", message);
                        if (dialog.open() == MessageDialog.OK) {
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
                        view.refreshState();
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

    private static class ConfirmDialogWithCancelDefault extends MessageDialog {

        public ConfirmDialogWithCancelDefault(Shell parentShell, String title, String message) {
            super(parentShell, title, null, message, MessageDialog.CONFIRM, new String[] {
                    IDialogConstants.OK_LABEL,
                    IDialogConstants.CANCEL_LABEL,
            }, 1 /* cancel */);
            // setShellStyle(getShellStyle() | SWT.SHEET);
        }
    }
}
