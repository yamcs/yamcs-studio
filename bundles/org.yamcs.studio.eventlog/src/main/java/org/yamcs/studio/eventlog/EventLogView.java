package org.yamcs.studio.eventlog;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Rest.ListEventsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.RCPUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public class EventLogView extends ViewPart implements StudioConnectionListener, InstanceListener, EventListener {

    private static final Logger log = Logger.getLogger(EventLogView.class.getName());

    public static final String COL_SOURCE = "Source";
    public static final String COL_GENERATION = "Generation";
    public static final String COL_RECEPTION = "Reception";
    public static final String COL_MESSAGE = "Message";
    public static final String COL_SEQNUM = "Seq.Nr.";

    private boolean showColumnSeqNum;
    private boolean showColumnReception;
    private boolean showColumnGeneration;
    private int nbMessageLineToDisplay;

    private TableViewer tableViewer;
    private TableLayout tableLayout;

    private EventLogContentProvider tableContentProvider;

    @Override
    public void createPartControl(Composite parent) {

        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        showColumnSeqNum = store.getBoolean(PreferencePage.PREF_SHOW_SEQNUM_COL);
        showColumnReception = store.getBoolean(PreferencePage.PREF_SHOW_RECTIME_COL);
        showColumnGeneration = store.getBoolean(PreferencePage.PREF_SHOW_GENTIME_COL);
        nbMessageLineToDisplay = store.getInt(PreferencePage.PREF_LINECOUNT);

        tableViewer = new TableViewer(parent,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        tableLayout = new TableLayout();
        tableViewer.getTable().setLayout(tableLayout);

        addFixedColumns();
        tableContentProvider = new EventLogContentProvider(tableViewer.getTable());
        tableViewer.setContentProvider(tableContentProvider);
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
            tableContentProvider.clearAll();
            tableViewer.setInput(null);
            tableViewer.setSelection(null);
            updateSummaryLine();
        });
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    private void addFixedColumns() {
        // sort listener common for all columns
        Listener sortListener = event -> {
            TableColumn column = (TableColumn) event.widget;
            tableContentProvider.sort(column);
        };

        TableViewerColumn messageColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        messageColumn.getColumn().setText(COL_MESSAGE);
        messageColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(300));

        TableViewerColumn sourceColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        sourceColumn.getColumn().setText(COL_SOURCE);
        sourceColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn generationColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        generationColumn.getColumn().setText(COL_GENERATION);
        generationColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn receptionColumn = new TableViewerColumn(tableViewer, SWT.NULL);
        receptionColumn.getColumn().setText(COL_RECEPTION);
        receptionColumn.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(150));

        TableViewerColumn seqNumColum = new TableViewerColumn(tableViewer, SWT.RIGHT);
        seqNumColum.getColumn().setText(COL_SEQNUM);
        seqNumColum.getColumn().addListener(SWT.Selection, sortListener);
        tableLayout.addColumnData(new ColumnPixelData(80));

        if (!showColumnSeqNum) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            seqNumColum.getColumn().setResizable(false);
        }
        if (!showColumnGeneration) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            generationColumn.getColumn().setResizable(false);
        }
        if (!showColumnReception) {
            tableLayout.addColumnData(new ColumnPixelData(0));
            receptionColumn.getColumn().setResizable(false);
        }

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
        tableViewer.getTable().setSortColumn(receptionColumn.getColumn());
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
        log.finer("add chunk of " + events.size());
        if (tableViewer.getTable().isDisposed())
            return;
        tableContentProvider.addEvents(events);

        updateSummaryLine();
        log.finest("Events added");
    }

    // This method should be called when the stream of events to be imported is ended
    public void addedAllEvents() {
        tableContentProvider.addedAllEvents();
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
}
