package org.yamcs.studio.core.eventlog;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
}
