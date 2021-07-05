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

public class CopyEventHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;

            StringBuilder text = new StringBuilder(
                    "Severity\tMessage\tType\tSource\tGeneration\tReception\tSequence Number\n");
            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                Event rec = ((EventLogItem) it.next()).event;

                Instant generationTime = Instant.ofEpochSecond(rec.getGenerationTime().getSeconds(),
                        rec.getGenerationTime().getNanos());

                Instant receptionTime = Instant.ofEpochSecond(rec.getReceptionTime().getSeconds(),
                        rec.getReceptionTime().getNanos());

                text.append(rec.getSeverity())
                        .append("\t").append(rec.getMessage())
                        .append("\t").append(rec.getType())
                        .append("\t").append(rec.getSource())
                        .append("\t").append(generationTime.toString())
                        .append("\t").append(receptionTime.toString())
                        .append("\t").append(rec.getSeqNumber())
                        .append("\n");
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
