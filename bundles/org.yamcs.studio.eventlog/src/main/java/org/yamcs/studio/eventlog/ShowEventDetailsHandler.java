package org.yamcs.studio.eventlog;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.Yamcs.Event;

public class ShowEventDetailsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            Iterator<?> it = selection.iterator();
            if (it.hasNext()) {
                Event rec = (Event) it.next();
                Shell shell = HandlerUtil.getActiveShellChecked(event);
                EventDetailsDialog dialog = new EventDetailsDialog(shell, rec);
                dialog.create();
                dialog.open();
            }
        }
        return null;
    }
}
