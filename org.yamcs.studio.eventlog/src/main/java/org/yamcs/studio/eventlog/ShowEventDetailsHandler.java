package org.yamcs.studio.eventlog;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowEventDetailsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var part = HandlerUtil.getActivePartChecked(event);
        var view = (EventLogView) part;

        var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            var selection = (IStructuredSelection) sel;
            Iterator<?> it = selection.iterator();
            if (it.hasNext()) {
                var rec = (EventLogItem) it.next();
                var shell = HandlerUtil.getActiveShellChecked(event);
                var dialog = new EventDetailsDialog(shell, view.getEventLog(), rec);
                dialog.create();
                dialog.open();
            }
        }
        return null;
    }
}
