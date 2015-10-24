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
import org.yamcs.protobuf.Mdb.SignificanceInfo;
import org.yamcs.protobuf.Rest.ValidateCommandRequest;
import org.yamcs.protobuf.Rest.ValidateCommandResponse;
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
        ValidateCommandRequest req = ValidateCommandRequest.newBuilder().addCommand(command.toRestCommandType()).build();

        RestClient restClient = checkRestClient(activeShell, "arm command");
        if (restClient == null)
            return;

        restClient.validateCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    ValidateCommandResponse validateResponse = (ValidateCommandResponse) response;

                    boolean doArm = false;
                    if (validateResponse.getCommandSignificanceCount() > 0) {
                        SignificanceInfo significance = validateResponse.getCommandSignificance(0).getSignificance();
                        switch (significance.getConsequenceLevel()) {
                        case WATCH:
                        case WARNING:
                        case DISTRESS:
                        case CRITICAL:
                        case SEVERE:
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
