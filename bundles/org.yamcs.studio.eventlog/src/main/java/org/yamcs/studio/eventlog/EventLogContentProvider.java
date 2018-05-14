package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class EventLogContentProvider implements IStructuredContentProvider {

    private EventLogViewerComparator eventLogViewerComparator = new EventLogViewerComparator();
    private ArrayList<Event> sortedEvents = new ArrayList<>();
    private Map<EventId, Event> eventsById = new LinkedHashMap<>();
    private Table table;
    private boolean scrollLock;
    private int nbMessageLineToDisplay;

    private Image warningIcon;
    private Image infoIcon;
    private Image distressIcon;
    private Image criticalIcon;
    private Image severeIcon;
    private Image watchIcon;

    private Color errorColor;
    private Color warningColor;

    private Event lastAddedEvent;

    public EventLogContentProvider(Table table) {
        this.table = table;
        if (PlatformUI.isWorkbenchRunning()) {
            infoIcon = getImage("icons/eview16/level0s.png");
            watchIcon = getImage("icons/eview16/level1s.png");
            warningIcon = getImage("icons/eview16/level2s.png");
            distressIcon = getImage("icons/eview16/level3s.png");
            criticalIcon = getImage("icons/eview16/level4s.png");
            severeIcon = getImage("icons/eview16/level5s.png");
        }

        errorColor = new Color(table.getDisplay(), new RGB(255, 221, 221));
        warningColor = new Color(table.getDisplay(), new RGB(248, 238, 199));

        nbMessageLineToDisplay = EventLogPreferences.getMessageLineCount();
    }

    private Image getImage(String path) {
        return ImageDescriptor.createFromURL(FileLocator
                .find(Platform.getBundle("org.yamcs.studio.eventlog"),
                        new Path(path), null))
                .createImage();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO could happen when switching processors
    }

    @Override
    public void dispose() {
        if (errorColor != null) {
            errorColor.dispose();
        }
        if (warningColor != null) {
            warningColor.dispose();
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return eventsById.values().toArray();
    }

    public List<Event> getSortedEvents() {
        return sortedEvents;
    }

    public void addEvent(Event event) {
        EventId eventId = new EventId(event);
        if (!eventsById.containsKey(eventId)) {
            sortedEvents.add(event);
            sortedEvents.sort(eventLogViewerComparator);
            int index = sortedEvents.indexOf(event);
            addItemFromEvent(event, index);
            eventsById.put(eventId, event);
        }

        maybeSelectAndReveal(event);
    }

    public void addEvents(List<Event> events) {
        if (events.size() == 0) {
            return;
        }

        table.setRedraw(false);
        events.forEach(event -> {
            EventId eventId = new EventId(event);
            if (eventsById.containsKey(eventId)) {
                // event is already loaded, ignoring it
            } else {
                eventsById.put(eventId, event);
                sortedEvents.add(event);
                addItemFromEvent(event, -1);
            }
        });
        lastAddedEvent = events.get(events.size() - 1);
        table.setItemCount(sortedEvents.size());
        table.setRedraw(true);
    }

    public void addedAllEvents() {
        sort();
        // select last inserted event
        maybeSelectAndReveal(lastAddedEvent);
    }

    private TableItem addItemFromEvent(Event event, int index) {
        TableItem item = null;
        if (index >= 0) {
            item = new TableItem(table, SWT.NULL, index);
        } else {
            item = new TableItem(table, SWT.NULL);
        }

        item.setText("Item " + event.getSeqNumber());

        // description
        String message = event.getMessage();
        if (nbMessageLineToDisplay > 0) {
            String lineSeparator = "\n";
            String[] messageLines = message.split(lineSeparator);
            message = "";
            int i = 0;
            for (; i < nbMessageLineToDisplay && i < messageLines.length; i++) {
                if (!message.isEmpty()) {
                    message += lineSeparator;
                }
                message += messageLines[i];
            }
            if (i + 1 < messageLines.length) {
                message += " [...]";
            }
        }
        item.setText(0, message);
        // Install a monospaced font, because it works better with logs
        item.setFont(0, JFaceResources.getFont(JFaceResources.TEXT_FONT));
        item.setImage(getSeverityImage(event));
        item.setBackground(getSeverityColor(event));

        // source
        String source = "";
        if (event.hasType()) {
            source = event.getSource() + " :: " + event.getType();
        } else {
            source = event.getSource();
        }
        item.setText(1, source);

        // generation time
        item.setText(2, YamcsUIPlugin.getDefault().formatInstant(event.getGenerationTime()));

        // reception time
        item.setText(3, YamcsUIPlugin.getDefault().formatInstant(event.getReceptionTime()));

        // seq number
        item.setText(4, event.getSeqNumber() + "");

        // store the original event
        item.setData(event);

        return item;
    }

    private Color getSeverityColor(Event evt) {
        if (evt.hasSeverity()) {
            switch (evt.getSeverity()) {
            case INFO:
                return null;
            case WARNING:
            case WATCH:
                return warningColor;
            case ERROR:
            case CRITICAL:
            case SEVERE:
            case DISTRESS:
                return errorColor;
            }
        }
        return null;
    }

    private Image getSeverityImage(Event evt) {
        if (evt.hasSeverity()) {
            switch (evt.getSeverity()) {
            case INFO:
                return infoIcon;
            case WATCH:
                return watchIcon;
            case WARNING:
                return warningIcon;
            case DISTRESS:
                return distressIcon;
            case CRITICAL:
                return criticalIcon;
            case SEVERE:
            case ERROR:
                return severeIcon;
            }
        }
        return null;
    }

    private void maybeSelectAndReveal(Event event) {
        if (!scrollLock) {
            table.setSelection(sortedEvents.indexOf(event));
        }
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(table.getDisplay(), () -> {
            table.setRedraw(false);
            table.removeAll();
            sortedEvents.clear();
            eventsById.clear();
            table.setRedraw(true);
        });
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }

    public void sort(TableColumn column) {

        // sort the local copy of event according to the specified column
        eventLogViewerComparator.setColumn(column);

        table.setSortColumn(column);
        table.setSortDirection(eventLogViewerComparator.getDirection());

        sort();
    }

    public void sort() {
        table.setRedraw(false);
        sortedEvents.sort(eventLogViewerComparator);

        // remove rows from table
        table.removeAll();

        // insert sorted rows
        for (Event event : sortedEvents) {
            addItemFromEvent(event, -1);
        }
        table.setRedraw(true);
    }
}
