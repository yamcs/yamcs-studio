package org.yamcs.studio.ui.eventlog;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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

    private Label labelTotalEvents;
    private Label labelWarnings;
    private Label labelErrors;

    private Table tableViewer;
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

        GridLayout gl = new GridLayout();
        parent.setLayout(gl);
        gl.horizontalSpacing = 0;
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;

        // create event table part
        Composite tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tcl = new TableColumnLayout();
        tableComposite.setLayout(tcl);

        //  create status bar part
        Composite statusComposite = new Composite(parent, SWT.NONE);
        statusComposite.setLayout(new RowLayout());
        RowData gd = new RowData();
        gd.width = 150;
        labelTotalEvents = new Label(statusComposite, SWT.BORDER);
        labelTotalEvents.setText("Total Events: 0");
        labelTotalEvents.setLayoutData(gd);

        labelWarnings = new Label(statusComposite, SWT.BORDER);
        labelWarnings.setText("Warnings: 0");
        gd = new RowData();
        gd.width = 130;
        labelWarnings.setLayoutData(gd);
        labelErrors = new Label(statusComposite, SWT.BORDER);
        labelErrors.setText("Errors: 0");
        gd = new RowData();
        gd.width = 110;
        labelErrors.setLayoutData(gd);

        tableViewer = new Table(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
        tableViewer.setHeaderVisible(true);
        tableViewer.setLinesVisible(true);
        tableViewer.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                TableItem item = (TableItem) event.item;
                item.setText("Item " + tableViewer.indexOf(item));
            }
        });
        addFixedColumns();
        tableContentProvider = new EventLogContentProvider(tableViewer);
        tableContentProvider.setNbLineToDisplay(nbMessageLineToDisplay);

        // open a popup with event detail in case of double click
        tableViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    Point pt = new Point(event.x, event.y);
                    TableItem item = tableViewer.getItem(pt);
                    if (item == null)
                        return;

                    Event selectedEvent = (Event) item.getData();
                    if (selectedEvent == null)
                        return;
                    EventDetailsDialog dialog = new EventDetailsDialog(parent.getShell(), selectedEvent);
                    dialog.create();
                    dialog.open();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Unable to open event detail.", e);
                }
            }

            @Override
            public void mouseDown(MouseEvent e)
            {
            }

            @Override
            public void mouseUp(MouseEvent e)
            {
            }

        });

        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null)
            EventCatalogue.getInstance().addEventListener(this);

    }

    @Override
    public void processEvent(Event event) {
        if (tableViewer.isDisposed())
            return;
        Display.getDefault().asyncExec(() -> addEvent(event));
    }

    public void clear() {
        Display.getDefault().asyncExec(() -> {
            log.finest("clear started");
            tableContentProvider.clearAll();
            setStatusTotalEvents(0);
            setStatusWarnings(0);
            setStatusErrors(0);
            log.finest("clear done");
        });
        log.finest("clear queued");
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    public final Comparator<TableItem> BY_STR = new Comparator<TableItem>() {
        @Override
        public int compare(TableItem o1, TableItem o2) {
            Event e1 = (Event) o1.getData();
            Event e2 = (Event) o2.getData();
            if (e1.getSeqNumber() > e2.getSeqNumber())
                return 1;
            else
                return -1;
        }
    };

    private void addFixedColumns() {

        TableColumn seqNumColum = null;
        if (showColumSeqNum) {
            seqNumColum = new TableColumn(tableViewer, SWT.NULL);
            seqNumColum.setText(COL_SEQNUM);
            tcl.setColumnData(seqNumColum, new ColumnPixelData(80));
        }

        TableColumn descriptionColumn = new TableColumn(tableViewer, SWT.NULL);
        descriptionColumn.setText(COL_DESCRIPTION);
        tcl.setColumnData(descriptionColumn, new ColumnWeightData(200));

        TableColumn sourceColumn = new TableColumn(tableViewer, SWT.NULL);
        sourceColumn.setText(COL_SOURCE);
        tcl.setColumnData(sourceColumn, new ColumnPixelData(150));

        TableColumn receivedColumn = null;
        if (showColumReception) {
            receivedColumn = new TableColumn(tableViewer, SWT.NULL);
            receivedColumn.setText(COL_RECEIVED);
            tcl.setColumnData(receivedColumn, new ColumnPixelData(150));
        }

        TableColumn gererationColumn = null;
        if (showColumnGeneration) {
            gererationColumn = new TableColumn(tableViewer, SWT.NULL);
            gererationColumn.setText(COL_GENERATION);
            tcl.setColumnData(gererationColumn, new ColumnPixelData(150));
        }

        // sort listener common for all columns
        Listener sortListener = new Listener() {
            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event e) {

                TableColumn column = (TableColumn) e.widget;
                tableContentProvider.sort(column);

            }
        };
        if (seqNumColum != null)
            seqNumColum.addListener(SWT.Selection, sortListener);
        descriptionColumn.addListener(SWT.Selection, sortListener);
        sourceColumn.addListener(SWT.Selection, sortListener);
        if (receivedColumn != null)
            receivedColumn.addListener(SWT.Selection, sortListener);
        if (gererationColumn != null)
            gererationColumn.addListener(SWT.Selection, sortListener);

        // TODO use IMemento or something
        tableViewer.setSortDirection(SWT.DOWN);
    }

    @Override
    public void setFocus() {
        tableViewer.setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        // TODO remove EventListener
    }

    public void addEvent(Event event) {
        if (tableViewer.isDisposed())
            return;
        tableContentProvider.addEvent(event);

        setStatusTotalEvents(tableContentProvider.getNbEvents());
        setStatusWarnings(tableContentProvider.getNbWarnings());
        setStatusErrors(tableContentProvider.getNbErrors());
    }

    public void addEvents(List<Event> events) {
        log.info("add chunk of " + events.size());
        if (tableViewer.isDisposed())
            return;
        tableContentProvider.addEvents(events);

        setStatusTotalEvents(tableContentProvider.getNbEvents());
        setStatusWarnings(tableContentProvider.getNbWarnings());
        setStatusErrors(tableContentProvider.getNbErrors());
        log.finest("Events added");
    }

    // This method should be called when the stream of events to be imported is ended
    public void addedAllEvents()
    {
        log.finest("sort started");
        tableContentProvider.sort();
        log.finest("sort done");
    }

    public EventLogContentProvider getTableContentProvider()
    {
        return tableContentProvider;
    }

    private void setStatusTotalEvents(int eventNumbers) {
        labelTotalEvents.setText("Total Events: " + eventNumbers);
    }

    private void setStatusWarnings(int warnings) {
        labelWarnings.setText("Warnings: " + warnings);
    }

    private void setStatusErrors(int errors) {
        labelErrors.setText("Errors: " + errors);
    }

    public static void main(String args[]) throws InterruptedException
    {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("Event Log test");
        shell.open();

        EventLogView lv = new EventLogView();
        lv.createPartControl(shell);
        shell.pack();

        // insert and clear a lot of events
        final int NB_TEST_EVENTS = 100000;
        for (int i = 0; i < 3; i++)
        {
            // clear events
            lv.clear();
            insertTestEvents(lv, NB_TEST_EVENTS);
            Display.getDefault().asyncExec(() ->
                    lv.addedAllEvents());
            log.info("sort queued");
        }
        // insert a batch without clearing the previous one
        // should include only a few events with a different primary key
        insertTestEvents(lv, NB_TEST_EVENTS);
        Display.getDefault().asyncExec(() -> lv.addedAllEvents());

        // insert special events
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
                        "test event3\nline *\nline ** - very\"looooooooooooooooooooooooooooooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnggggggggggggggg")
                .setSeqNumber(2)
                .setSeverity(EventSeverity.ERROR)
                .setSource("test_source3")
                .setType("test_type3")
                .build();

        Event event4 = Event
                .newBuilder()
                .setGenerationTime(event2.getGenerationTime())
                .setReceptionTime(new Date().getTime())
                .setMessage(
                        "should not replace event2")
                .setSeqNumber(event2.getSeqNumber())
                .setSeverity(EventSeverity.ERROR)
                .setSource(event2.getSource())
                .setType("test_type4")
                .build();

        lv.processEvent(event);
        lv.processEvent(event2);
        lv.processEvent(event3);
        lv.processEvent(event4);

        // export to csv
        //        Display.getDefault().asyncExec(() -> {
        //            ExportEventsHandler eeh = new ExportEventsHandler();
        //            try {
        //                eeh.doExecute(lv, shell);
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void insertTestEvents(EventLogView lv, int nbEvents) {
        List<Event> events = new LinkedList<Event>();
        for (int i = 0; i < nbEvents; i++)
        {
            Event event = Event.newBuilder()
                    .setGenerationTime(new Date().getTime())
                    .setReceptionTime(new Date().getTime())
                    .setMessage("test event " + i)
                    .setSeqNumber(i)
                    .setSeverity(EventSeverity.WARNING)
                    .setSource("test_source")
                    .setType("test_type")
                    .build();
            events.add(event);
        }
        Display.getDefault().asyncExec(() -> lv.addEvents(events));
        log.info("addEvents queued");

    }

}
