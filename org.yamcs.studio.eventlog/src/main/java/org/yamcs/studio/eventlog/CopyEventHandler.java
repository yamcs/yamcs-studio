/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import java.time.Instant;
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
import org.yamcs.protobuf.Event.EventSeverity;

public class CopyEventHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
        if (sel != null && sel instanceof IStructuredSelection) {
            var selection = (IStructuredSelection) sel;

            var text = new StringBuilder("Severity\tMessage\tType\tSource\tGeneration\tReception\tSequence Number\n");
            Iterator<?> it = selection.iterator();
            while (it.hasNext()) {
                var rec = ((EventLogItem) it.next()).event;

                var generationTime = Instant.ofEpochSecond(rec.getGenerationTime().getSeconds(),
                        rec.getGenerationTime().getNanos());

                var receptionTime = Instant.ofEpochSecond(rec.getReceptionTime().getSeconds(),
                        rec.getReceptionTime().getNanos());

                var severity = rec.getSeverity();
                if (severity == EventSeverity.WARNING_NEW) {
                    severity = EventSeverity.WARNING;
                }

                text.append(severity).append("\t").append(rec.getMessage()).append("\t").append(rec.getType())
                        .append("\t").append(rec.getSource()).append("\t").append(generationTime.toString())
                        .append("\t").append(receptionTime.toString()).append("\t").append(rec.getSeqNumber())
                        .append("\n");
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
