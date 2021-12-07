package org.yamcs.studio.commanding.cmdhist;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.data.yamcs.StringConverter;

public class CopyCommandHistoryEntryDetailsHandler extends AbstractHandler {

    private static final String PARAM_GENTIME = "GENTIME";
    private static final String PARAM_COMMAND = "COMMAND";
    private static final String PARAM_SOURCE = "SOURCE";
    private static final String PARAM_SEQNO = "SEQNO";
    private static final String PARAM_BIN = "BIN";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            var selection = (IStructuredSelection) sel;

            var property = event.getParameter(CommandHistory.CMDPARAM_EVENT_PROPERTY);

            var text = new StringBuilder();
            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                var rec = (CommandHistoryRecord) it.next();
                var command = rec.getCommand();
                switch (property) {
                case PARAM_GENTIME:
                    text.append(command.getGenerationTime());
                    break;
                case PARAM_COMMAND:
                    text.append(command.getSource());
                    break;
                case PARAM_SOURCE:
                    text.append(command.getUsername() + "@" + command.getOrigin());
                    break;
                case PARAM_SEQNO:
                    text.append(command.getSequenceNumber());
                    break;
                case PARAM_BIN:
                    var hexString = StringConverter.arrayToHexString(command.getBinary());
                    text.append(hexString);
                    break;
                default:
                    throw new IllegalStateException("Unexpected property: " + property);
                }

                if (it.hasNext()) {
                    text.append("\n");
                }
            }

            var display = Display.getCurrent();
            var clipboard = new Clipboard(display);
            var transfers = new Transfer[] { TextTransfer.getInstance() };

            clipboard.setContents(new Object[] { text.toString() }, transfers);
            clipboard.dispose();
        }
        return null;
    }
}
