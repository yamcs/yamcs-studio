package org.yamcs.studio.eventlog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

public class ScrollLockHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var part = HandlerUtil.getActivePartChecked(event);
        var view = (EventLogView) part;

        var service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        var command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
        var oldState = HandlerUtil.toggleCommandState(command);
        view.getEventLog().enableScrollLock(!oldState);
        return null;
    }
}
