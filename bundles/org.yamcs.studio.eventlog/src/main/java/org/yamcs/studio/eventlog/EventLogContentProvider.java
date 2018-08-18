package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogContentProvider implements IStructuredContentProvider {

    private Map<EventId, Event> eventsById = new LinkedHashMap<>();
    private TableViewer tableViewer;
    private boolean scrollLock;

    private Event lastAddedEvent;

    public EventLogContentProvider(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return eventsById.values().toArray();
    }

    public void addEvent(Event event) {
        EventId eventId = new EventId(event);

        boolean update = eventsById.containsKey(eventId);
        eventsById.put(eventId, event);

        if (update) {
            tableViewer.update(event, null); // Null, means all properties
            maybeSelectAndReveal(event);
        } else {
            tableViewer.add(event);
            maybeSelectAndReveal(event);
        }
    }

    public void addEvents(List<Event> events, boolean increment) {
        // 'increment' makes it a LOT slower. This is especially noticeable on batch imports.
        // Not sure if we need increment anywhere.

        if (events.isEmpty()) {
            return;
        }

        List<Event> updated = new ArrayList<>();
        List<Event> added = new ArrayList<>();

        events.forEach(event -> {
            EventId eventId = new EventId(event);
            Event prevEvent = eventsById.put(eventId, event);
            if (prevEvent == null) {
                added.add(event);
            } else {
                updated.add(event);
            }
        });

        if (increment) {
            tableViewer.add(added.toArray());
            tableViewer.update(updated.toArray(), null);
        } else {
            tableViewer.setInput("anything-except-null");
            tableViewer.refresh();
        }

        lastAddedEvent = events.get(events.size() - 1);

        maybeSelectAndReveal(lastAddedEvent);
    }

    private void maybeSelectAndReveal(Event event) {
        if (!scrollLock) {
            IStructuredSelection sel = new StructuredSelection(event);
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
            Collection<Event> events = eventsById.values();
            tableViewer.remove(events);
            eventsById.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }
}
