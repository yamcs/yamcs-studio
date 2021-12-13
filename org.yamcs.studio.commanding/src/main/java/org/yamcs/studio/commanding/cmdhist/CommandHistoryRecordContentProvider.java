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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.client.Command;

public class CommandHistoryRecordContentProvider implements IStructuredContentProvider {

    public static final String GREEN = "icons/obj16/ok.png";
    public static final String RED = "icons/obj16/nok.png";

    public static final String ACKNOWLEDGE_PREFIX = "Acknowledge_";
    public static final String VERIFIER_PREFIX = "Verifier_";

    private Map<String, CommandHistoryRecord> recordsByCommandId = new LinkedHashMap<>();
    private TableViewer tableViewer;
    private boolean scrollLock;

    public CommandHistoryRecordContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO could happen when switching channels
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return recordsByCommandId.values().toArray();
    }

    public void processCommand(Command command) {
        if (recordsByCommandId.containsKey(command.getId())) {
            var rec = recordsByCommandId.get(command.getId());
            rec.merge(command);
            tableViewer.update(rec, null); // Null, means all properties
            maybeSelectAndReveal(rec);
        } else {
            var rec = new CommandHistoryRecord(command);
            recordsByCommandId.put(command.getId(), rec);

            tableViewer.add(rec);
            maybeSelectAndReveal(rec);
        }
    }

    public void addCommands(List<Command> commands) {
        if (commands.isEmpty()) {
            return;
        }

        Collections.reverse(commands);
        CommandHistoryRecord rec = null;
        for (var command : commands) {
            if (recordsByCommandId.containsKey(command.getId())) {
                rec = recordsByCommandId.get(command.getId());
                rec.merge(command);
            } else {
                rec = new CommandHistoryRecord(command);
                recordsByCommandId.put(command.getId(), rec);
            }
        }

        tableViewer.setInput("anything-except-null");
        tableViewer.refresh();
        maybeSelectAndReveal(rec);
    }

    public void maybeSelectAndReveal(CommandHistoryRecord rec) {
        if (!scrollLock) {
            var sel = new StructuredSelection(rec);
            tableViewer.setSelection(sel, true);
        }
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            var recs = recordsByCommandId.values();
            tableViewer.remove(recs.toArray());
            recordsByCommandId.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }
}
