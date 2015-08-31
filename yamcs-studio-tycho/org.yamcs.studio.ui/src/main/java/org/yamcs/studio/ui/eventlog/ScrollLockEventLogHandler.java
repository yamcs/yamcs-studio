package org.yamcs.studio.ui.eventlog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

public class ScrollLockEventLogHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        EventLogView view = (EventLogView) part;

        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = service.getCommand("org.yamcs.studio.ui.eventlog.scrollLockCommand");
        boolean oldState = HandlerUtil.toggleCommandState(command);
        view.enableScrollLock(!oldState);
        return null;
    }
}
