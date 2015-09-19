package org.yamcs.studio.ui.commanding.stack;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Commanding.CommandSignificance;
import org.yamcs.protobuf.Rest.RestValidateCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandResponse;
import org.yamcs.studio.core.ui.utils.AbstractRestHandler;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.StackedState;

import com.google.protobuf.MessageLite;

public class ArmCommandHandler extends AbstractRestHandler {

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

    private void armCommand(Shell activeShell, CommandStackView view, StackedCommand command) {
        RestValidateCommandRequest req = RestValidateCommandRequest.newBuilder().addCommands(command.toRestCommandType()).build();

        RestClient restClient = checkRestClient(activeShell, "arm command");
        if (restClient == null)
            return;

        restClient.validateCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    RestValidateCommandResponse validateResponse = (RestValidateCommandResponse) response;

                    boolean doArm = false;
                    if (validateResponse.getCommandsSignificanceCount() > 0) {
                        CommandSignificance significance = validateResponse.getCommandsSignificance(0);
                        switch (significance.getConsequenceLevel()) {
                        case watch:
                        case warning:
                        case distress:
                        case critical:
                        case severe:
                            String level = Character.toUpperCase(significance.getConsequenceLevel().toString().charAt(0))
                                    + significance.getConsequenceLevel().toString().substring(1);
                            if (MessageDialog.openConfirm(activeShell, "Confirm",
                                    level + ": Are you sure you want to arm this command?\n" +
                                            "    " + command.toStyledString(view).getString() + "\n\n" +
                                            significance.getReasonForWarning())) {
                                doArm = true;
                            }
                            break;
                        default:
                            break;
                        }
                    } else {
                        doArm = true;
                    }

                    if (doArm) {
                        log.info(String.format("Command armed %s", command));
                        command.setStackedState(StackedState.ARMED);
                        view.refreshState();
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not arm command", e);
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    MessageDialog.openError(activeShell, "Could not arm command", e.getMessage());
                    view.clearArm();
                    view.refreshState();
                });
            }
        });
    }
}
