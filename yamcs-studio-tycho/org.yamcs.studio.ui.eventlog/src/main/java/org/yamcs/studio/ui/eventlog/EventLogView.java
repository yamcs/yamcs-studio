package org.yamcs.studio.ui.eventlog;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.utils.TimeEncoding;

public class EventLogView extends ViewPart implements EventListener {

    private static final Logger log = Logger.getLogger(EventLogView.class.getName());

    public static final String COL_SOURCE = "Source";
    public static final String COL_RECEIVED = "Received";
    public static final String COL_GENERATION = "Generation";
    public static final String COL_DESCRIPTION = "Message";
    public static final String COL_SEQNUM = "Sequ. #";

    private boolean showColumSeqNum = true;
    private boolean showColumReception = true;
    private boolean showColumnGeneration = true;
    private int nbMessageLineToDisplay = 1;

    private Image errorIcon;
    private Image warnIcon;
    private Image infoIcon;

    private TableViewer tableViewer;
    private EventLogViewerComparator tableViewerComparator;
    private TableColumnLayout tcl;

    private EventLogContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {

        // get preference from plugin
        if (YamcsUIPlugin.getDefault() != null)
        {
            showColumSeqNum = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumSeqNum");
            showColumReception = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumReception");
            showColumnGeneration = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumnGeneration");
            nbMessageLineToDisplay = YamcsUIPlugin.getDefault().getPreferenceStore().getInt("events.nbMessageLineToDisplay");
        }

        if (PlatformUI.isWorkbenchRunning())
        {
            errorIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            warnIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
            infoIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }

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

        // open a popup with event detail in case of double click
        tableViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                try {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    EventDetailsDialog dialog = new EventDetailsDialog(parent.getShell(), (Event) selection.getFirstElement());
                    dialog.create();
                    dialog.open();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to open event detail.", e);
                }
            }
        });

        if (getViewSite() != null)
            getViewSite().setSelectionProvider(tableViewer);

        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null)
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

    private void addFixedColumns() {

        // Seq. Number
        if (showColumSeqNum) {
            TableViewerColumn setnumColumb = new TableViewerColumn(tableViewer, SWT.NONE);
            setnumColumb.getColumn().setText(COL_SEQNUM);
            setnumColumb.getColumn().addSelectionListener(getSelectionAdapter(setnumColumb.getColumn()));
            setnumColumb.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((Event) element).getSeqNumber() + "";
                }
            });
            tcl.setColumnData(setnumColumb.getColumn(), new ColumnPixelData(80));
        }

        // Message
        TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        descriptionColumn.getColumn().setText(COL_DESCRIPTION);
        descriptionColumn.getColumn().addSelectionListener(getSelectionAdapter(descriptionColumn.getColumn()));
        descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {

                String message = ((Event) element).getMessage();

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
                return message;
            }

            @Override
            public Image getImage(Object element) {
                Event evt = (Event) element;
                return getSeverityImage(evt);
            }
        });
        tcl.setColumnData(descriptionColumn.getColumn(), new ColumnWeightData(200));

        // Source :: Type
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

        // Reception time
        if (showColumReception) {
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
            tableViewer.getTable().setSortColumn(receivedColumn.getColumn());
        }

        // Generation Time
        if (showColumnGeneration) {
            TableViewerColumn gererationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            gererationColumn.getColumn().setText(COL_GENERATION);
            gererationColumn.getColumn().addSelectionListener(getSelectionAdapter(gererationColumn.getColumn()));
            gererationColumn.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return TimeEncoding.toString(((Event) element).getGenerationTime());
                }
            });
            tcl.setColumnData(gererationColumn.getColumn(), new ColumnPixelData(150));
        }

        // TODO use IMemento or something
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

    public static void main(String args[])
    {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("Event Log test");
        shell.open();

        EventLogView lv = new EventLogView();
        lv.createPartControl(shell);
        shell.pack();

        Event event = Event.newBuilder()
                .setGenerationTime(new Date().getTime() - 10000)
                .setReceptionTime(new Date().getTime())
                .setMessage("test event\nline 2\nline 3")
                .setSeqNumber(1)
                .setSeverity(EventSeverity.INFO)
                .setSource("test_source")
                .setType("test_type")
                .build();

        Event event2 = Event.newBuilder()
                .setGenerationTime(new Date().getTime() - 20000)
                .setReceptionTime(new Date().getTime())
                .setMessage("test event2\nline a\nline b")
                .setSeqNumber(2)
                .setSeverity(EventSeverity.ERROR)
                .setSource("test_source2")
                .setType("test_type2")
                .build();

        Event event3 = Event
                .newBuilder()
                .setGenerationTime(new Date().getTime() - 20000)
                .setReceptionTime(new Date().getTime())
                .setMessage(
                        "test event3\nline *\nline ** - verylooooooooooooooooooooooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnggggggggggggggg")
                .setSeqNumber(3)
                .setSeverity(EventSeverity.ERROR)
                .setSource("test_source3")
                .setType("test_type3")
                .build();

        lv.processEvent(event);
        lv.processEvent(event2);
        lv.processEvent(event3);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
