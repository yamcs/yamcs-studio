package org.yamcs.studio.eventlog;

import java.time.Instant;
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
import org.yamcs.protobuf.Yamcs.Event;

public class CopyEventDetailsHandler extends AbstractHandler {

    private static final String PARAM_MESSAGE = "MESSAGE";
    private static final String PARAM_SOURCE = "SOURCE";
    private static final String PARAM_TYPE = "TYPE";
    private static final String PARAM_GENTIME = "GENTIME";
    private static final String PARAM_RECTIME = "RECTIME";
    private static final String PARAM_SEQNO = "SEQNO";
    private static final String PARAM_SEVERITY = "SEVERITY";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;

            String property = event.getParameter(EventLog.CMDPARAM_EVENT_PROPERTY);

            StringBuilder text = new StringBuilder();
            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                Event rec = ((EventLogItem) it.next()).event;
                switch (property) {
                case PARAM_MESSAGE:
                    text.append(rec.getMessage());
                    break;
                case PARAM_SOURCE:
                    text.append(rec.getSource());
                    break;
                case PARAM_TYPE:
                    text.append(rec.getSource());
                    break;
                case PARAM_GENTIME:
                    Instant generationTime = Instant.ofEpochSecond(rec.getGenerationTime().getSeconds(),
                            rec.getGenerationTime().getNanos());
                    text.append(generationTime.toString());
                    break;
                case PARAM_RECTIME:
                    Instant receptionTime = Instant.ofEpochSecond(rec.getReceptionTime().getSeconds(),
                            rec.getReceptionTime().getNanos());
                    text.append(receptionTime.toString());
                    break;
                case PARAM_SEQNO:
                    text.append(rec.getSeqNumber());
                    break;
                case PARAM_SEVERITY:
                    text.append(rec.getSeverity());
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
