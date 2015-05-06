package org.yamcs.studio.core.eventlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogContentProvider implements IStructuredContentProvider {

    private Map<Integer, Event> eventsBySequenceNumber = new LinkedHashMap<>();
    private TableViewer tableViewer;

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
        if (eventsBySequenceNumber.containsKey(event.getSeqNumber())) {
            // Hmm not sure if this will do an equals
            tableViewer.update(event, null);
        } else {
            tableViewer.add(event);
            eventsBySequenceNumber.put(event.getSeqNumber(), event);
        }
    }

    public void addEvents(List<Event> events) {
        List<Object> needsUpdating = new ArrayList<>();
        events.forEach(evt -> {
            if (eventsBySequenceNumber.containsKey(evt.getSeqNumber()))
                needsUpdating.add(evt);
            eventsBySequenceNumber.put(evt.getSeqNumber(), evt);
        });
        tableViewer.update(needsUpdating.toArray(), null);

        events.removeAll(needsUpdating);
        tableViewer.add(events.toArray());
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
}
