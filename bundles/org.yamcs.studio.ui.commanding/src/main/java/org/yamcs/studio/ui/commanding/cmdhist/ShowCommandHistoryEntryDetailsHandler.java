package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowCommandHistoryEntryDetailsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        CommandHistoryView view = (CommandHistoryView) part;

        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            Iterator<?> it = selection.iterator();
            if (it.hasNext()) {
                CommandHistoryRecord rec = (CommandHistoryRecord) it.next();
                Shell shell = HandlerUtil.getActiveShellChecked(event);
                CommandHistoryEntryDetailsDialog dialog = new CommandHistoryEntryDetailsDialog(shell, view, rec);
                dialog.create();
                dialog.open();
            }
        }
        return null;
    }
}
