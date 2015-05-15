package org.yamcs.studio.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.ui.eventlog.EventLogView;

/**
 * Handels the enabled state for the tag command
 */
public class ClearEventLogHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        EventLogView view = (EventLogView) part;
        view.clear();
        return null;
    }
}
