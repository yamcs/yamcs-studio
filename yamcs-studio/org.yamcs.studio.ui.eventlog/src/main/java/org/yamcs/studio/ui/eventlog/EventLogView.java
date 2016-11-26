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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Rest.ListEventsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.MessageLite;

public class EventLogView extends ViewPart implements StudioConnectionListener, InstanceListener, EventListener {

    private static final Logger log = Logger.getLogger(EventLogView.class.getName());

    public static final String COL_SOURCE = "Source";
    public static final String COL_RECEIVED = "Received";
    public static final String COL_GENERATION = "Generation";
    public static final String COL_DESCRIPTION = "Message";
    public static final String COL_SEQNUM = "Sequ. #";

    private boolean showColumnSeqNum = true;
    private boolean showColumnReception = true;
    private boolean showColumnGeneration = true;
    private int nbMessageLineToDisplay = 1;

    private Table table;
    private TableColumnLayout tcl;

    private EventLogContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {

        // get preference from plugin
        if (YamcsUIPlugin.getDefault() != null) {
            showColumnSeqNum = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumSeqNum");
            showColumnReception = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumReception");
            //showColumnGeneration = YamcsUIPlugin.getDefault().getPreferenceStore().getBoolean("events.showColumnGeneration");
            nbMessageLineToDisplay = YamcsUIPlugin.getDefault().getPreferenceStore().getInt("events.nbMessageLineToDisplay");
        }

        // create event table part
        tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION /*
                                                                                    * | SWT.VIRTUAL
                                                                                    */);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        addFixedColumns();
        tableContentProvider = new EventLogContentProvider(table);
        tableContentProvider.setNbLineToDisplay(nbMessageLineToDisplay);

        Listener propertiesHandler = (evt -> {
            if (table.isDisposed())
                return;

            TableItem[] sel = table.getSelection();
            if (sel == null || sel.length == 0)
                return;

            Event selectedEvent = (Event) sel[0].getData();
            EventDetailsDialog dialog = new EventDetailsDialog(parent.getShell(), selectedEvent);
            dialog.create();
            dialog.open();
        });

        // open a popup with event detail in case of double click
        table.addListener(SWT.MouseDoubleClick, propertiesHandler);

        updateSummaryLine();

        // Install context menu
        // Not registered with site, because it's not a jface table
        Menu tableMenu = new Menu(table);
        table.setMenu(tableMenu);

        MenuItem miProperties = new MenuItem(tableMenu, SWT.NONE);
        miProperties.setText("Properties");
        miProperties.addListener(SWT.Selection, propertiesHandler);

        table.addListener(SWT.MenuDetect, evt -> {
            evt.doit = (table.getSelectionCount() > 0);
        });

        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null)
            EventCatalogue.getInstance().addEventListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public void onStudioConnect() {
        Display.getDefault().asyncExec(() -> {
            clear();
            // TODO make optional? fetchLatestEvents();
        });
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> {
            clear();
            // TODO make optional? fetchLatestEvents();
        });
    }

    @Override
    public void onStudioDisconnect() {
        // NOP for now
    }

    private void fetchLatestEvents() {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        EventCatalogue.getInstance().fetchLatestEvents(instance, new ResponseHandler() {

            @Override
            public void onMessage(MessageLite responseMsg) {
                ListEventsResponse response = (ListEventsResponse) responseMsg;
                Display.getDefault().asyncExec(() -> {
                    addEvents(response.getEventList());
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Failed to retrieve latests events", e);
            }
        });
    }

    @Override
    public void processEvent(Event event) {
        if (table.isDisposed())
            return;
        Display.getDefault().asyncExec(() -> addEvent(event));
    }

    public void clear() {
        Display.getDefault().asyncExec(() -> {
            log.finest("clear started");
            tableContentProvider.clearAll();
            updateSummaryLine();
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

        TableColumn seqNumColum = new TableColumn(table, SWT.RIGHT);
        seqNumColum.setText(COL_SEQNUM);
        tcl.setColumnData(seqNumColum, new ColumnPixelData(80));

        TableColumn descriptionColumn = new TableColumn(table, SWT.NULL);
        descriptionColumn.setText(COL_DESCRIPTION);
        tcl.setColumnData(descriptionColumn, new ColumnWeightData(200));

        TableColumn sourceColumn = new TableColumn(table, SWT.NULL);
        sourceColumn.setText(COL_SOURCE);
        tcl.setColumnData(sourceColumn, new ColumnPixelData(150));

        TableColumn receivedColumn = new TableColumn(table, SWT.NULL);
        receivedColumn.setText(COL_RECEIVED);
        tcl.setColumnData(receivedColumn, new ColumnPixelData(150));

        TableColumn gererationColumn = new TableColumn(table, SWT.NULL);
        gererationColumn.setText(COL_GENERATION);
        tcl.setColumnData(gererationColumn, new ColumnPixelData(150));

        // sort listener common for all columns
        Listener sortListener = new Listener() {
            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event e) {

                TableColumn column = (TableColumn) e.widget;
                tableContentProvider.sort(column);

            }
        };
        seqNumColum.addListener(SWT.Selection, sortListener);
        descriptionColumn.addListener(SWT.Selection, sortListener);
        sourceColumn.addListener(SWT.Selection, sortListener);
        receivedColumn.addListener(SWT.Selection, sortListener);
        gererationColumn.addListener(SWT.Selection, sortListener);

        if (!showColumnSeqNum) {
            tcl.setColumnData(seqNumColum, new ColumnPixelData(0));
            seqNumColum.setResizable(false);
        }
        if (!showColumnReception) {
            tcl.setColumnData(receivedColumn, new ColumnPixelData(0));
            receivedColumn.setResizable(false);
        }
        if (!showColumnGeneration) {
            tcl.setColumnData(gererationColumn, new ColumnPixelData(0));
            gererationColumn.setResizable(false);
        }

        for (TableColumn tableColumn : table.getColumns()) {
            tableColumn.setMoveable(true);
            // prevent resize to 0
            tableColumn.addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (tableColumn.getWidth() < 5)
                        tableColumn.setWidth(5);
                }
            });
        }

        // TODO use IMemento or something
        // !! Keep these values in sync with EventLogViewerComparator constructor
        table.setSortColumn(receivedColumn);
        table.setSortDirection(SWT.UP);

    }

    @Override
    public void setFocus() {
        table.setFocus();
    }

    @Override
    public void dispose() {
        EventCatalogue.getInstance().addEventListener(this);
        ConnectionManager.getInstance().removeStudioConnectionListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        super.dispose();
    }

    public void addEvent(Event event) {
        if (table.isDisposed())
            return;
        tableContentProvider.addEvent(event);

        updateSummaryLine();
    }

    public void addEvents(List<Event> events) {
        log.info("add chunk of " + events.size());
        if (table.isDisposed())
            return;
        tableContentProvider.addEvents(events);

        updateSummaryLine();
        log.finest("Events added");
    }

    // This method should be called when the stream of events to be imported is ended
    public void addedAllEvents() {
        log.finest("sort started");
        tableContentProvider.addedAllEvents();
        log.finest("sort done");
    }

    public EventLogContentProvider getTableContentProvider() {
        return tableContentProvider;
    }

    private void updateSummaryLine() {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        String summaryLine = "";
        if (yamcsInstance != null) {
            summaryLine = "Showing events for Yamcs instance " + yamcsInstance + ". ";
        }
        setContentDescription(summaryLine + String.format("%d errors, %d warnings, %d others (no filter)",
                tableContentProvider.getNbErrors(),
                tableContentProvider.getNbWarnings(),
                tableContentProvider.getNbInfo()));
    }

    // test function
    public static void main(String args[]) throws InterruptedException {
        TimeEncoding.setUp();
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("Event Log test");
        shell.open();

        EventLogView lv = new EventLogView();
        lv.createPartControl(shell);
        shell.pack();

        // insert and clear a lot of events
        //final int NB_TEST_EVENTS = 10;
        final int NB_TEST_EVENTS = 100000;
        final int BLOCK_SIZE = 500;
        for (int i = 0; i < 1; i++) {
            // clear events
            lv.clear();
            insertTestEvents(lv, NB_TEST_EVENTS, BLOCK_SIZE);

            Display.getDefault().asyncExec(() -> lv.addedAllEvents());
            log.info("sort queued");
        }
        //         insert a batch without clearing the previous one
        //         should include only a few events with a different primary key
        //        insertTestEvents(lv, NB_TEST_EVENTS, BLOCK_SIZE);
        //        Display.getDefault().asyncExec(() -> lv.addedAllEvents());

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
        Display.getDefault().asyncExec(() -> {
            ExportEventsHandler eeh = new ExportEventsHandler();
            try {
                eeh.doExecute(lv, shell);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void insertTestEvents(EventLogView lv, int nbEvents, int blockSize) {

        int eventId = 0;
        while (eventId < nbEvents) {
            List<Event> events = new LinkedList<>();
            for (int j = 0; j < blockSize; j++) {
                eventId++;
                if (eventId > nbEvents)
                    break;
                Event event = Event.newBuilder()
                        .setGenerationTime(new Date().getTime())
                        .setReceptionTime(new Date().getTime())
                        .setMessage("test event " + eventId)
                        .setSeqNumber(eventId)
                        .setSeverity(EventSeverity.WARNING)
                        .setSource("test_source")
                        .setType("test_type")
                        .build();
                events.add(event);
            }
            Display.getDefault().asyncExec(() -> lv.addEvents(events));
        }
        log.info("addEvents queued");

    }

}
