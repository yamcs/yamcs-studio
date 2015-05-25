package org.yamcs.studio.ui.commanding.stack;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;
import org.yamcs.studio.ui.handlers.AbstractRestHandler;

import com.google.protobuf.MessageLite;

public class FireCommandHandler extends AbstractRestHandler {

    private static final Logger log = Logger.getLogger(FireCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!checkRestClient(event, "fire command"))
            return null;
        Shell shell = HandlerUtil.getActiveShell(event);
        StackedCommand command = CommandStack.getInstance().getActiveCommand();
        fireCommand(shell, command);
        return null;
    }

    private void fireCommand(Shell activeShell, StackedCommand command) {
        RestSendCommandRequest req = RestSendCommandRequest.newBuilder().addCommands(command.toRestCommandType()).build();
        restClient.sendCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    if (response instanceof RestExceptionMessage) {
                        RestExceptionMessage exc = (RestExceptionMessage) response;
                        command.setState(State.REJECTED);
                        MessageDialog.openError(activeShell, "Could not fire command", exc.getMsg());
                    } else {
                        log.fine(String.format("Command fired", req));
                        command.setState(State.ISSUED);
                        CommandStack.getInstance().incrementAndGet();
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fire command", e);
                Display.getDefault().asyncExec(() -> {
                    command.setState(State.REJECTED);
                    MessageDialog.openError(activeShell, "Could not fire command", e.getMessage());
                });
            }
        });
    }
}
