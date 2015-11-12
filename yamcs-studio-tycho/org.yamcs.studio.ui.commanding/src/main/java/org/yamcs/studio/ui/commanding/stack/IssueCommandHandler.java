package org.yamcs.studio.ui.commanding.stack;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Rest.IssueCommandRequest;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.ui.utils.AbstractRestHandler;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.StackedState;

import com.google.protobuf.MessageLite;

public class IssueCommandHandler extends AbstractRestHandler {

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

    private void issueCommand(Shell activeShell, CommandStackView view, StackedCommand command) {
        RestClient restClient = checkRestClient(activeShell, "issue command");
        if (restClient == null)
            return;

        IssueCommandRequest req = command.toIssueCommandRequest().build();
        String instance = ConnectionManager.getInstance().getYamcsInstance();
        String qname = command.getMetaCommand().getQualifiedName();
        restClient.sendCommand(instance, "realtime", qname, req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    log.info(String.format("Command issued. %s", req));
                    command.setStackedState(StackedState.ISSUED);
                    view.selectActiveCommand();
                    view.refreshState();
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not issue command", e);
                Display.getDefault().asyncExec(() -> {
                    command.setStackedState(StackedState.REJECTED);
                    MessageDialog.openError(activeShell, "Could not issue command", e.getMessage());
                    view.refreshState();
                });
            }
        });
    }
}
