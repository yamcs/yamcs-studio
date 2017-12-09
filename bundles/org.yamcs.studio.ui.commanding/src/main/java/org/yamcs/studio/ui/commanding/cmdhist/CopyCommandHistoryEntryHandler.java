package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.ui.commanding.stack.CommandClipboard;

public class CopyCommandHistoryEntryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;

            StringBuilder text = new StringBuilder(
                    "T\tCommand\tSource\tSequence Number\n");

            List<CommandHistoryRecord> recList = new ArrayList<>();

            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                CommandHistoryRecord rec = (CommandHistoryRecord) it.next();
                recList.add(rec);

                text.append(rec.getGenerationTime()).append("\t").append(rec.getCommandString())
                        .append("\t").append(rec.getUsername() + "@" + rec.getOrigin())
                        .append("\t").append(rec.getSequenceNumber())
                        .append("\n");
            }

            if (!recList.isEmpty()) {
                CommandClipboard.addCommandHistoryRecords(recList);
            }

            Display display = Display.getCurrent();
            Clipboard clipboard = new Clipboard(display);
            Transfer[] transfers = new Transfer[] { TextTransfer.getInstance() };

            clipboard.setContents(new Object[] { text.toString() }, transfers);
            clipboard.dispose();
        }
        return null;
    }
}
