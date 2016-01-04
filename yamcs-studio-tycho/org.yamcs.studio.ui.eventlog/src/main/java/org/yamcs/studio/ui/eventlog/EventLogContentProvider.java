package org.yamcs.studio.ui.eventlog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.utils.TimeEncoding;

public class EventLogContentProvider implements IStructuredContentProvider {

    private EventLogViewerComparator eventLogViewerComparator = new EventLogViewerComparator();
    private ArrayList<Event> sortedEvents = new ArrayList<Event>();
    private Map<Long, Event> eventsBySequenceNumber = new LinkedHashMap<>();
    private Table tableViewer;
    private boolean scrollLock;
    private int nbMessageLineToDisplay;
    private int warnings = 0;
    private int errors = 0;

    private Image errorIcon;
    private Image warnIcon;
    private Image infoIcon;

    public EventLogContentProvider(Table tableViewer) {
        this.tableViewer = tableViewer;
        if (PlatformUI.isWorkbenchRunning())
        {
            errorIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            warnIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            infoIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
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

    public List<Event> getSortedEvents()
    {
        return sortedEvents;
    }

    public void addEvent(Event event) {
        if (eventsBySequenceNumber.containsKey(primaryKeyHash(event))) {
            // event is already loaded, ignoring it
        } else {
            sortedEvents.add(event); // add to local event list
            sortedEvents.sort(eventLogViewerComparator); // and sort the sorted list
            int index = sortedEvents.indexOf(event);
            addItemFromEvent(event, index); // add to table
            eventsBySequenceNumber.put(primaryKeyHash(event), event); // add to the hash map

            switch (event.getSeverity())
            {
            case WARNING:
                warnings++;
                break;
            case ERROR:
                errors++;
                break;
            default:
                break;
            }
        }

        maybeSelectAndReveal(event);
    }

    public void addEvents(List<Event> events) {
        if (events.size() == 0)
            return;

        tableViewer.setRedraw(false);
        events.forEach(event -> {
            if (eventsBySequenceNumber.containsKey(primaryKeyHash(event))) {
                // event is already loaded, ignoring it
            } else {

                eventsBySequenceNumber.put(primaryKeyHash(event), event);
                sortedEvents.add(event);
                addItemFromEvent(event, -1);
                switch (event.getSeverity())
                {
                case WARNING:
                    warnings++;
                    break;
                case ERROR:
                    errors++;
                    break;
                default:
                    break;
                }
            }
        });

        tableViewer.setItemCount(sortedEvents.size());
        tableViewer.setRedraw(true);

        // select last inserted event
        maybeSelectAndReveal(events.get(events.size() - 1));

    }

    private TableItem addItemFromEvent(Event event, int index)
    {
        TableItem item = null;
        if (index >= 0)
            item = new TableItem(tableViewer, SWT.NULL, index);
        else
            item = new TableItem(tableViewer, SWT.NULL);
        item.setText("Item " + event.getSeqNumber());

        // seq number
        item.setText(0, event.getSeqNumber() + "");

        // description
        String message = event.getMessage();
        if (nbMessageLineToDisplay > 0)
        {
            String lineSeparator = "\n";
            String[] messageLines = message.split(lineSeparator);
            message = "";
            int i = 0;
            for (; i < nbMessageLineToDisplay && i < messageLines.length; i++)
            {
                if (!message.isEmpty())
                    message += lineSeparator;
                message += messageLines[i];
            }
            if (i + 1 < messageLines.length)
                message += " [...]";
        }
        item.setText(1, message);
        item.setImage(getSeverityImage(event));

        // source
        String source = "";
        if (event.hasType())
            source = event.getSource() + " :: " + event.getType();
        else
            source = event.getSource();
        item.setText(2, source);

        // reception time
        item.setText(3, TimeEncoding.toString(event.getReceptionTime()));

        // generation time
        item.setText(4, TimeEncoding.toString(event.getGenerationTime()));

        // store the original event
        item.setData(event);

        return item;
    }

    private Image getSeverityImage(Event evt)
    {
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
        return eventsBySequenceNumber.size();
    }

    public int getNbWarnings()
    {
        return warnings;
    }

    public int getNbErrors()
    {
        return errors;
    }

    private long primaryKeyHash(Event event)
    {
        return event.getSource().hashCode() + event.getGenerationTime() + event.getSeqNumber();
    }

    private void maybeSelectAndReveal(Event event) {
        if (!scrollLock) {
            tableViewer.setSelection(sortedEvents.indexOf(event));
        }
    }

    public void clearAll() {
        // TODO not sure if this is the recommended way to delete all. Need to verify
        BusyIndicator.showWhile(tableViewer.getDisplay(), () -> {
            tableViewer.setRedraw(false);
            tableViewer.removeAll();
            sortedEvents.clear();
            eventsBySequenceNumber.clear();
            tableViewer.setRedraw(true);
            warnings = 0;
            errors = 0;
        });
    }

    public void enableScrollLock(boolean enabled) {
        scrollLock = enabled;
    }

    public void setNbLineToDisplay(int nb)
    {
        nbMessageLineToDisplay = nb;
    }

    public void sort(TableColumn column) {

        // sort the local copy of event according to the specified column
        eventLogViewerComparator.setColumn(column);

        tableViewer.setSortColumn(column);
        tableViewer.setSortDirection(eventLogViewerComparator.getDirection());

        sort();
    }

    public void sort()
    {
        tableViewer.setRedraw(false);
        sortedEvents.sort(eventLogViewerComparator);

        // remove rows from table
        tableViewer.removeAll();

        // insert sorted rows
        for (Event event : sortedEvents)
            addItemFromEvent(event, -1);
        tableViewer.setRedraw(true);
    }
}
