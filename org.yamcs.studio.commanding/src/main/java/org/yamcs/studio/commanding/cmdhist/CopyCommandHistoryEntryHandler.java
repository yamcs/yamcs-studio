/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.commanding.stack.CommandClipboard;

public class CopyCommandHistoryEntryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            var selection = (IStructuredSelection) sel;

            var text = new StringBuilder("T\tCommand\tSource\tSequence Number\n");

            List<CommandHistoryRecord> recList = new ArrayList<>();

            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                var rec = (CommandHistoryRecord) it.next();
                recList.add(rec);

                var command = rec.getCommand();
                text.append(command.getGenerationTime()).append("\t").append(rec.getSource()).append("\t")
                        .append(command.getUsername() + "@" + command.getOrigin()).append("\t")
                        .append(command.getSequenceNumber()).append("\n");
            }

            if (!recList.isEmpty()) {
                CommandClipboard.addCommandHistoryRecords(recList);
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
