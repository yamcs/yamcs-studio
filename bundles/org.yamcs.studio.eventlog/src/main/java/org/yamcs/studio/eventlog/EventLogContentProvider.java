package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogContentProvider implements IStructuredContentProvider {

    private static final Logger log = Logger.getLogger(EventLogContentProvider.class.getName());
    private EventLogViewerComparator eventLogViewerComparator = new EventLogViewerComparator();
    private ArrayList<Event> sortedEvents = new ArrayList<>();
    private Map<EventId, Event> eventsById = new LinkedHashMap<>();
    private Table table;
    private boolean scrollLock;
    private int nbMessageLineToDisplay;
    private int warningCount = 0;
    private int errorCount = 0;
    private int infoCount = 0;

    private Image errorIcon;
    private Image warnIcon;
    private Image infoIcon;

    private Color errorColor;
    private Color warningColor;

    public EventLogContentProvider(Table table) {
        this.table = table;
        if (PlatformUI.isWorkbenchRunning()) {
            errorIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            warnIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            infoIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
        errorColor = new Color(table.getDisplay(), new RGB(255, 221, 221));
        warningColor = new Color(table.getDisplay(), new RGB(248, 238, 199));

        nbMessageLineToDisplay = EventLogPreferences.getMessageLineCount();
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
        if (eventsById.containsKey(eventId)) {
            // event is already loaded, ignoring it
        } else {
            sortedEvents.add(event); // add to local event list
            sortedEvents.sort(eventLogViewerComparator); // and sort the sorted list
            int index = sortedEvents.indexOf(event);
            addItemFromEvent(event, index); // add to table
            eventsById.put(eventId, event); // add to the hash map

            switch (event.getSeverity()) {
            case INFO:
                infoCount++;
                break;
            case WARNING:
                warningCount++;
                break;
            case ERROR:
                errorCount++;
                break;
            default:
                log.warning("Unexpected event severity '" + event.getSeverity() + "'");
                break;
            }
        }

        maybeSelectAndReveal(event);
    }

    Event lastAddedEvent = null;

    public void addEvents(List<Event> events) {
        if (events.size() == 0)
            return;

        table.setRedraw(false);
        events.forEach(event -> {
            EventId eventId = new EventId(event);
            if (eventsById.containsKey(eventId)) {
                // event is already loaded, ignoring it
            } else {
                eventsById.put(eventId, event);
                sortedEvents.add(event);
                addItemFromEvent(event, -1);
                switch (event.getSeverity()) {
                case INFO:
                    infoCount++;
                    break;
                case WARNING:
                    warningCount++;
                    break;
                case ERROR:
                    errorCount++;
                    break;
                default:
                    log.warning("Unexpected event severity '" + event.getSeverity() + "'");
                    break;
                }
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
        if (index >= 0)
            item = new TableItem(table, SWT.NULL, index);
        else
            item = new TableItem(table, SWT.NULL);

        item.setText("Item " + event.getSeqNumber());

        // description
        String message = event.getMessage();
        if (nbMessageLineToDisplay > 0) {
            String lineSeparator = "\n";
            String[] messageLines = message.split(lineSeparator);
            message = "";
            int i = 0;
            for (; i < nbMessageLineToDisplay && i < messageLines.length; i++) {
                if (!message.isEmpty())
                    message += lineSeparator;
                message += messageLines[i];
            }
            if (i + 1 < messageLines.length)
                message += " [...]";
        }
        item.setText(0, message);
        // Install a monospaced font, because it works better with logs
        item.setFont(0, JFaceResources.getFont(JFaceResources.TEXT_FONT));
        item.setImage(getSeverityImage(event));
        item.setBackground(getSeverityColor(event));

        // source
        String source = "";
        if (event.hasType())
            source = event.getSource() + " :: " + event.getType();
        else
            source = event.getSource();
        item.setText(1, source);

        // reception time
        item.setText(2, event.getReceptionTimeUTC());

        // generation time
        item.setText(3, event.getGenerationTimeUTC());

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
                return warningColor;
            case ERROR:
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
            case WARNING:
                return warnIcon;
            case ERROR:
                return errorIcon;
            }
        }
        return null;
    }

    public int getNbEvents() {
        return eventsById.size();
    }

    public int getNbWarnings() {
        return warningCount;
    }

    public int getNbErrors() {
        return errorCount;
    }

    public int getNbInfo() {
        return infoCount;
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
            warningCount = 0;
            errorCount = 0;
            infoCount = 0;
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
        for (Event event : sortedEvents)
            addItemFromEvent(event, -1);
        table.setRedraw(true);
    }
}
