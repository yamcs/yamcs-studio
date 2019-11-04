package org.yamcs.studio.commanding.stack;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.IssueCommandRequest;
import org.yamcs.protobuf.IssueCommandResponse;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.model.CommandingCatalogue;

import com.google.protobuf.InvalidProtocolBufferException;

public class IssueCommandHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(IssueCommandHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        CommandStackView commandStackView = (CommandStackView) window.getActivePage().findView(CommandStackView.ID);
        StackedCommand command = CommandStack.getInstance().getActiveCommand();
        issueCommand(shell, commandStackView, command);
        return null;
    }

    private void issueCommand(Shell activeShell, CommandStackView view, StackedCommand command)
            throws ExecutionException {
        IssueCommandRequest req = command.toIssueCommandRequest().build();
        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        String qname;
        try {
            qname = command.getSelectedAliasEncoded();
        } catch (UnsupportedEncodingException e1) {
            throw new ExecutionException(e1.getMessage());
        }

        catalogue.sendCommand("realtime", qname, req).whenComplete((data, exc) -> {
            if (exc == null) {
                try {
                    IssueCommandResponse response = IssueCommandResponse.newBuilder()
                            .mergeFrom(data)
                            .build();
                    command.setCommandId(response.getId());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
                Display.getDefault().asyncExec(() -> {
                    log.info(String.format("Command issued. %s", req));
                    command.setStackedState(StackedState.ISSUED);
                    view.selectActiveCommand();
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
