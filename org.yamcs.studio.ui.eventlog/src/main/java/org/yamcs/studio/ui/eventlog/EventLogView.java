package org.yamcs.studio.ui.eventlog;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
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
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.InvalidProtocolBufferException;

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

    private TableViewer tableViewer;
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

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        addFixedColumns();
        tableContentProvider = new EventLogContentProvider(tableViewer.getTable());
        tableContentProvider.setNbLineToDisplay(nbMessageLineToDisplay);

        // Register context menu. Commands are added in plugin.xml
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewer);

        // Default action is to open Event properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(EventLog.CMD_EVENT_PROPERTIES);
        });

        updateSummaryLine();

        getViewSite().setSelectionProvider(tableViewer);

        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null) {
            EventCatalogue.getInstance().addEventListener(this);
        }
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);

        updateState();
    }

    private void updateState() {
        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
        State state = command.getState(EventLog.STATE_SCROLL_LOCK);
        enableScrollLock((Boolean) state.getValue());
    }

    @Override
    public void onStudioConnect() {
        Display.getDefault().asyncExec(() -> {
            clear();
            fetchLatestEvents();
        });
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> {
            clear();
            fetchLatestEvents();
        });
    }

    @Override
    public void onStudioDisconnect() {
        // NOP for now
    }

    private void fetchLatestEvents() {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        EventCatalogue.getInstance().fetchLatestEvents(instance).whenComplete((data, exc) -> {
            try {
                ListEventsResponse response = ListEventsResponse.parseFrom(data);
                Display.getDefault().asyncExec(() -> {
                    addEvents(response.getEventList());
                });
            } catch (InvalidProtocolBufferException e) {
                log.log(Level.SEVERE, "Failed to decode server message", e);
            }
        });
    }

    @Override
    public void processEvent(Event event) {
        if (tableViewer.getTable().isDisposed())
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
        // sort listener common for all columns
        Listener sortListener = event -> {
            TableColumn column = (TableColumn) event.widget;
            tableContentProvider.sort(column);
        };

        TableViewerColumn seqNumColum = new TableViewerColumn(tableViewer, SWT.RIGHT);
        seqNumColum.getColumn().setText(COL_SEQNUM);
        seqNumColum.getColumn().addListener(SWT.Selection, sortListener);
        tcl.setColumnData(seqNumColum.getColumn(), new ColumnPixelData(80));

        TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        descriptionColumn.getColumn().setText(COL_DESCRIPTION);
        descriptionColumn.getColumn().addListener(SWT.Selection, sortListener);
        tcl.setColumnData(descriptionColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn sourceColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addListener(SWT.Selection, sortListener);
        tcl.setColumnData(sourceColumn.getColumn(), new ColumnPixelData(150));

        TableViewerColumn receivedColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        receivedColumn.getColumn().setText(COL_RECEIVED);
        receivedColumn.getColumn().addListener(SWT.Selection, sortListener);
        tcl.setColumnData(receivedColumn.getColumn(), new ColumnPixelData(150));

        TableViewerColumn gererationColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        gererationColumn.getColumn().setText(COL_GENERATION);
        gererationColumn.getColumn().addListener(SWT.Selection, sortListener);
        tcl.setColumnData(gererationColumn.getColumn(), new ColumnPixelData(150));

        /*if (!showColumnSeqNum) {
            tcl.setColumnData(seqNumColum.getColumn(), new ColumnPixelData(0));
            seqNumColum.getColumn().setResizable(false);
        }
        if (!showColumnReception) {
            tcl.setColumnData(receivedColumn.getColumn(), new ColumnPixelData(0));
            receivedColumn.getColumn().setResizable(false);
        }
        if (!showColumnGeneration) {
            tcl.setColumnData(gererationColumn.getColumn(), new ColumnPixelData(0));
            gererationColumn.getColumn().setResizable(false);
        }*/

        for (TableColumn tableColumn : tableViewer.getTable().getColumns()) {
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
        tableViewer.getTable().setSortColumn(receivedColumn.getColumn());
        tableViewer.getTable().setSortDirection(SWT.UP);
    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        EventCatalogue.getInstance().addEventListener(this);
        ConnectionManager.getInstance().removeStudioConnectionListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        super.dispose();
    }

    public void addEvent(Event event) {
        if (tableViewer.getTable().isDisposed())
            return;
        tableContentProvider.addEvent(event);

        updateSummaryLine();
    }

    public void addEvents(List<Event> events) {
        log.info("add chunk of " + events.size());
        if (tableViewer.getTable().isDisposed())
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
