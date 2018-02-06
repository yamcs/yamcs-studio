package org.yamcs.studio.commanding.cmdhist;

import java.util.Iterator;

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

public class CopyCommandHistoryEntryDetailsHandler extends AbstractHandler {

    private static final String PARAM_GENTIME = "GENTIME";
    private static final String PARAM_COMMAND = "COMMAND";
    private static final String PARAM_SOURCE = "SOURCE";
    private static final String PARAM_SEQNO = "SEQNO";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;

            String property = event.getParameter(CommandHistory.CMDPARAM_EVENT_PROPERTY);

            StringBuilder text = new StringBuilder();
            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                CommandHistoryRecord rec = (CommandHistoryRecord) it.next();
                switch (property) {
                case PARAM_GENTIME:
                    text.append(rec.getGenerationTime());
                    break;
                case PARAM_COMMAND:
                    text.append(rec.getCommandString());
                    break;
                case PARAM_SOURCE:
                    text.append(rec.getUsername() + "@" + rec.getOrigin());
                    break;
                case PARAM_SEQNO:
                    text.append(rec.getSequenceNumber());
                    break;
                default:
                    throw new IllegalStateException("Unexpected property: " + property);
                }

                if (it.hasNext()) {
                    text.append("\n");
                }
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
