package org.yamcs.studio.ui.eventlog;

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

    private Map<Integer, Event> eventsBySequenceNumber = new LinkedHashMap<>();
    private TableViewer tableViewer;
    private boolean scrollLock;

    public EventLogContentProvider(TableViewer tableViewer) {
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
        return eventsBySequenceNumber.values().toArray();
    }

    public void addEvent(Event event) {
        if (eventsBySequenceNumber.containsKey(primaryKeyHash(event))) {
            tableViewer.remove(eventsBySequenceNumber.get(primaryKeyHash(event)));
        } else {
            eventsBySequenceNumber.put(primaryKeyHash(event), event);
        }
        tableViewer.add(event);
        maybeSelectAndReveal(event);
    }

    private int primaryKeyHash(Event event)
    {
        return event.getSource().hashCode() + Long.hashCode(event.getGenerationTime()) + event.getSeqNumber();
    }

    public void addEvents(List<Event> events) {
        List<Object> needsUpdating = new ArrayList<>();
        events.forEach(evt -> {
            if (eventsBySequenceNumber.containsKey(evt.getSeqNumber()))
                needsUpdating.add(evt);
            eventsBySequenceNumber.put(primaryKeyHash(evt), evt);
        });
        tableViewer.update(needsUpdating.toArray(), null);

        events.removeAll(needsUpdating);
        if (!events.isEmpty()) {
            tableViewer.add(events.toArray());
            maybeSelectAndReveal(events.get(events.size() - 1));
        }
    }

    private void maybeSelectAndReveal(Event event) {
        if (!scrollLock) {
            IStructuredSelection sel = new StructuredSelection(event);
            tableViewer.setSelection(sel, true);
        }
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getTable().getDisplay(), () -> {
            tableViewer.getTable().setRedraw(false);
            Collection<Event> events = eventsBySequenceNumber.values();
            tableViewer.remove(events.toArray());
            eventsBySequenceNumber.clear();
            tableViewer.getTable().setRedraw(true);
        });
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }
}
