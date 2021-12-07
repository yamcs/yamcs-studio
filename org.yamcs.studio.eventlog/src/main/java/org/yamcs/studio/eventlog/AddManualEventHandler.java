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
import org.eclipse.ui.handlers.HandlerUtil;

public class AddManualEventHandler extends AbstractHandler {

    public static final String EVENT_ADD_ACTION = "org.yamcs.studio.eventlog.addEvent.action";
    private static final String EVENT_ADD = "ADD";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var part = HandlerUtil.getActivePartChecked(event);

        var action = event.getParameter(EVENT_ADD_ACTION);

        if (action.equals(EVENT_ADD)) {
            var addManualEventDialog = new AddManualEventDialog(part.getSite().getShell());
            addManualEventDialog.open();
        } else { // action.equals(EVENT_INSERT)
            var sel = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

            // get selected event and open a manual event dialog using the selected generation time.
            if (sel != null && sel instanceof IStructuredSelection) {
                var selection = (IStructuredSelection) sel;
                Iterator<?> it = selection.iterator();
                var rec = ((EventLogItem) it.next()).event;
                var generationTime = Instant.ofEpochSecond(rec.getGenerationTime().getSeconds(),
                        rec.getGenerationTime().getNanos());

                var addManualEventDialog = new AddManualEventDialog(part.getSite().getShell(), generationTime);
                addManualEventDialog.open();
            }
        }

        return null;
    }
}
