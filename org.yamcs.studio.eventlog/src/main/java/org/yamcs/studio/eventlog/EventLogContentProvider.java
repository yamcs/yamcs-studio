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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogContentProvider implements IStructuredContentProvider {

    private Set<EventLogItem> items = new HashSet<>();
    private TableViewer tableViewer;
    private boolean scrollLock;

    private EventLogItem lastAddedEvent;

    public EventLogContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public EventLogItem[] getElements(Object inputElement) {
        return items.toArray(new EventLogItem[0]);
    }

    public void addEvent(Event event) {
        var item = new EventLogItem(event);

        var plugin = EventLogPlugin.getDefault();
        item.colorize(plugin.loadColoringRules());

        var update = items.contains(item);
        items.add(item);

        if (update) {
            tableViewer.update(event, null); // Null, means all properties
            maybeSelectAndReveal(item);
        } else {
            tableViewer.add(event);
            maybeSelectAndReveal(item);
        }
    }

    public void addEvents(List<Event> events, boolean increment) {
        // 'increment' makes it a LOT slower. This is especially noticeable on batch imports.
        // Not sure if we need increment anywhere.

        if (events.isEmpty()) {
            return;
        }

        var plugin = EventLogPlugin.getDefault();

        List<EventLogItem> updated = new ArrayList<>();
        List<EventLogItem> added = new ArrayList<>();

        List<EventLogItem> newItems = events.stream().map(EventLogItem::new).collect(Collectors.toList());

        newItems.forEach(newItem -> {
            newItem.colorize(plugin.loadColoringRules());
            if (items.contains(newItem)) {
                updated.add(newItem);
            } else {
                added.add(newItem);
            }
            items.add(newItem);
        });

        if (increment) {
            tableViewer.add(added.toArray());
            tableViewer.update(updated.toArray(), null);
        } else {
            tableViewer.setInput("anything-except-null");
            tableViewer.refresh();
        }

        lastAddedEvent = newItems.get(newItems.size() - 1);

        maybeSelectAndReveal(lastAddedEvent);
    }

    private void maybeSelectAndReveal(EventLogItem event) {
        if (!scrollLock) {
            IStructuredSelection sel = new StructuredSelection(event);
            tableViewer.setSelection(sel, true);
        }
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }

    public void clearAll() {
        items.clear();
        tableViewer.setInput("anything-except-null");
        tableViewer.refresh();
    }
}
