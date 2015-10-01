package org.yamcs.studio.ui.eventlog;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;
import org.yamcs.utils.TimeEncoding;

public class EventLogView extends ViewPart implements EventListener {

    private static final Logger log = Logger.getLogger(EventLogView.class.getName());

    public static final String COL_SOURCE = "Source";
    public static final String COL_RECEIVED = "Received";
    public static final String COL_DESCRIPTION = "Message";

    private Image errorIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
    private Image warnIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
    private Image infoIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);

    private TableViewer tableViewer;
    private EventLogViewerComparator tableViewerComparator;
    private TableColumnLayout tcl;

    private EventLogContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
        addFixedColumns();
        tableContentProvider = new EventLogContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new EventLogViewerComparator();
        tableViewer.setComparator(tableViewerComparator);
        getViewSite().setSelectionProvider(tableViewer);

        EventCatalogue.getInstance().addEventListener(this);
    }

    @Override
    public void processEvent(Event event) {
        if (tableViewer.getTable().isDisposed())
            return;
        Display.getDefault().asyncExec(() -> addEvent(event));
    }

    public void clear() {
        tableContentProvider.clearAll();
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    private void addFixedColumns() {
        TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        descriptionColumn.getColumn().setText(COL_DESCRIPTION);
        descriptionColumn.getColumn().addSelectionListener(getSelectionAdapter(descriptionColumn.getColumn()));
        descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Event) element).getMessage();
            }

            @Override
            public Image getImage(Object element) {
                Event evt = (Event) element;
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
        });
        tcl.setColumnData(descriptionColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn sourceColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addSelectionListener(getSelectionAdapter(sourceColumn.getColumn()));
        sourceColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Event evt = (Event) element;
                if (evt.hasType())
                    return evt.getSource() + " :: " + evt.getType();
                else
                    return evt.getSource();
            }
        });
        tcl.setColumnData(sourceColumn.getColumn(), new ColumnPixelData(150));

        TableViewerColumn receivedColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        receivedColumn.getColumn().setText(COL_RECEIVED);
        receivedColumn.getColumn().addSelectionListener(getSelectionAdapter(receivedColumn.getColumn()));
        receivedColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return TimeEncoding.toString(((Event) element).getReceptionTime());
            }
        });
        tcl.setColumnData(receivedColumn.getColumn(), new ColumnPixelData(150));

        // TODO use IMemento or something
        tableViewer.getTable().setSortColumn(receivedColumn.getColumn());
        tableViewer.getTable().setSortDirection(SWT.DOWN);
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        // TODO remove EventListener
    }

    public void addEvents(List<Event> events) {
        log.info("add chunk of " + events.size());
        if (tableViewer.getTable().isDisposed())
            return;
        tableContentProvider.addEvents(events);
    }

    public void addEvent(Event event) {
        if (tableViewer.getTable().isDisposed())
            return;
        tableContentProvider.addEvent(event);
    }

    private SelectionAdapter getSelectionAdapter(TableColumn column) {
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableViewerComparator.setColumn(column);
                int dir = tableViewerComparator.getDirection();
                tableViewer.getTable().setSortDirection(dir);
                tableViewer.getTable().setSortColumn(column);
                tableViewer.refresh();
            }
        };
        return selectionAdapter;
    }
}