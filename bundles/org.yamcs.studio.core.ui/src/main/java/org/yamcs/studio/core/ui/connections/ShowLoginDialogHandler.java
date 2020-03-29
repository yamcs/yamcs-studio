package org.yamcs.studio.core.ui.connections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.connect.ConnectionPreferences;

public class ShowLoginDialogHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        new LoginDialog(shell, ConnectionPreferences.getLastUsedConfiguration()).open();
        return null;
    }
}
