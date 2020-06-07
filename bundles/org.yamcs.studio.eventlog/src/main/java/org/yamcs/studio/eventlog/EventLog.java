package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.yamcs.client.EventSubscription;
import org.yamcs.client.archive.ArchiveClient;
import org.yamcs.protobuf.SubscribeEventsRequest;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.YamcsAware;
import org.yamcs.studio.core.ui.utils.Debouncer;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class EventLog extends Composite implements YamcsAware {

    public static final String CMD_SCROLL_LOCK = "org.yamcs.studio.eventlog.scrollLockCommand";
    public static final String CMD_EVENT_PROPERTIES = "org.yamcs.studio.eventlog.showDetailsCommand";
    public static final String CMDPARAM_EVENT_PROPERTY = "org.yamcs.studio.eventlog.copyDetails.property";
    public static final String STATE_SCROLL_LOCK = "org.eclipse.ui.commands.toggleState";

    private static final long TABLE_UPDATE_RATE = 1000;

    private EventLogTableViewer tableViewer;
    private EventLogContentProvider tableContentProvider;
    private MenuManager menuManager;
    private IPropertyChangeListener prefListener;

    private List<Event> realtimeEvents = new ArrayList<>();
    private ScheduledExecutorService tableUpdater = Executors.newSingleThreadScheduledExecutor();

    public EventLog(Composite parent, int style) {
        super(parent, style);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        setLayout(gl);

        Composite filterBar = new Composite(this, SWT.NONE);
        filterBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        filterBar.setLayout(gl);

        Label filterLabel = new Label(filterBar, SWT.NONE);
        filterLabel.setText("Filter:");

        Text searchbox = new Text(filterBar, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setMessage("type filter text");
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label severityLabel = new Label(filterBar, SWT.NONE);
        severityLabel.setText("Level:");
        severityLabel.setToolTipText("Severity Level");

        Combo severityCombo = new Combo(filterBar, SWT.DROP_DOWN | SWT.READ_ONLY);
        severityCombo.setItems("INFO", "WATCH", "WARNING", "DISTRESS", "CRITICAL", "SEVERE");
        severityCombo.select(0);

        Composite tableViewerWrapper = new Composite(this, SWT.NONE);
        tableViewerWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewerWrapper.setLayout(new FillLayout());

        tableViewer = new EventLogTableViewer(tableViewerWrapper);
        tableContentProvider = new EventLogContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);

        // Register context menu. Commands are added in plugin.xml
        menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);

        // Default action is to open Event properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(EventLog.CMD_EVENT_PROPERTIES);
        });

        // Listen to v_scroll to autotoggle scroll lock
        tableViewer.getTable().getVerticalBar().addListener(SWT.Selection, evt -> {

            String sortColumn = tableViewer.getTable().getSortColumn().getText();
            boolean up = (tableViewer.getTable().getSortDirection() == SWT.UP);

            if (!EventLogTableViewer.COL_GENERATION.equals(sortColumn)) {
                return;
            }

            // User controls sort direction, so events may be inserted anywhere really.
            // Probably the most intuitive, is to autolock if the user is either at the very top or the very bottom of
            // the scrollbar.
            int sel = tableViewer.getTable().getVerticalBar().getSelection();
            int thumb = tableViewer.getTable().getVerticalBar().getThumb();
            int min = tableViewer.getTable().getVerticalBar().getMinimum();
            int max = tableViewer.getTable().getVerticalBar().getMaximum();

            ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
            Command command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
            State lockState = command.getState(RegistryToggleState.STATE_ID);
            boolean locked = ((Boolean) lockState.getValue()).booleanValue();
            boolean onEdge = (sel <= min && !up) || (sel + thumb >= max && up);

            if (locked && onEdge) {
                lockState.setValue(false);
                enableScrollLock(false);
            } else if (!locked && !onEdge) {
                lockState.setValue(true);
                enableScrollLock(true);
            }
        });

        EventLogSearchBoxFilter searchBoxFilter = new EventLogSearchBoxFilter();
        tableViewer.addFilter(searchBoxFilter);
        Debouncer debouncer = new Debouncer(tableUpdater);
        searchbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.keyCode == SWT.ARROW_DOWN) {
                    TableItem[] items = tableViewer.getTable().getItems();
                    if (items.length > 0) {
                        tableViewer.getTable().setSelection(items[0]);
                        tableViewer.getTable().setFocus();
                    }
                } else {
                    String searchString = searchbox.getText();
                    debouncer.debounce(() -> {
                        searchBoxFilter.setSearchTerm(searchString);
                        getDisplay().syncExec(() -> tableViewer.refresh());
                    }, 400, TimeUnit.MILLISECONDS);
                }
            }
        });

        EventLogSeverityFilter severityFilter = new EventLogSeverityFilter();
        tableViewer.addFilter(severityFilter);
        severityCombo.addListener(SWT.Selection, evt -> {
            EventSeverity severity = EventSeverity.valueOf(severityCombo.getText());
            severityFilter.setMinimumSeverity(severity);
            tableViewer.refresh();
        });

        updateState();

        tableUpdater.scheduleWithFixedDelay(() -> {
            synchronized (realtimeEvents) {
                if (realtimeEvents.isEmpty() || isDisposed()) {
                    return;
                }
                List<Event> eventBatch = new ArrayList<>(realtimeEvents);
                realtimeEvents.clear();
                Display.getDefault().syncExec(() -> addEvents(eventBatch));
            }
        }, TABLE_UPDATE_RATE, TABLE_UPDATE_RATE, TimeUnit.MILLISECONDS);

        EventLogPlugin plugin = EventLogPlugin.getDefault();
        prefListener = evt -> {
            if (evt.getProperty().equals(PreferencePage.PREF_RULES)) {
                List<ColoringRule> rules = plugin.composeColoringRules((String) evt.getNewValue());
                for (EventLogItem item : tableContentProvider.getElements(null)) {
                    item.colorize(rules);
                }
                tableViewer.refresh();
            }
        };

        plugin.getPreferenceStore().addPropertyChangeListener(prefListener);
    }

    private void updateState() {
        ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
        State state = command.getState(EventLog.STATE_SCROLL_LOCK);
        enableScrollLock((Boolean) state.getValue());
    }

    public void attachToSite(IViewSite site) {
        site.registerContextMenu(menuManager, tableViewer);
        site.setSelectionProvider(tableViewer);
    }

    @Override
    public boolean setFocus() {
        return tableViewer.getTable().setFocus();
    }

    private EventSubscription subscription;

    @Override
    public void changeInstance(String instance) {
        if (subscription != null) {
            subscription.cancel(true);
            clear();
        }

        if (instance == null) {
            clear();
        } else {
            Display.getDefault().asyncExec(() -> {
                fetchLatestEvents();
            });
            subscription = YamcsPlugin.getYamcsClient().createEventSubscription();
            subscription.addMessageListener(event -> {
                Display.getDefault().asyncExec(() -> processEvent(event));
            });
            subscription.sendMessage(SubscribeEventsRequest.newBuilder()
                    .setInstance(instance)
                    .build());
        }
    }

    public EventLogItem getPreviousRecord(EventLogItem rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            int[] indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] > 0) {
                int prevIndex = indices[0] - 1;
                return (EventLogItem) tableViewer.getElementAt(prevIndex);
            }
        }
        return null;
    }

    public EventLogItem getNextRecord(EventLogItem rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            int[] indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] < tableViewer.getTable().getItemCount() - 1) {
                int nextIndex = indices[0] + 1;
                return (EventLogItem) tableViewer.getElementAt(nextIndex);
            }
        }
        return null;
    }

    private void fetchLatestEvents() {
        ArchiveClient archiveClient = YamcsPlugin.getArchiveClient();
        if (archiveClient != null) {
            archiveClient.listEvents().whenComplete((page, exc) -> {
                List<Event> eventList = new ArrayList<>();
                page.iterator().forEachRemaining(eventList::add);
                Collections.reverse(eventList); // Output is reverse chronological

                Display.getDefault().asyncExec(() -> {
                    addEvents(eventList);
                });
            });
        }
    }

    private void processEvent(Event event) {
        synchronized (realtimeEvents) {
            realtimeEvents.add(event);
        }
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    public void addEvents(List<Event> events) {
        if (isDisposed()) {
            return;
        }
        tableContentProvider.addEvents(events, false);
    }

    public void clear() {
        tableContentProvider.clearAll();
        tableViewer.setInput(null);
        tableViewer.setSelection(null);
    }

    /**
     * Returns the collection of currently visible events (sorted as is visible)
     */
    public List<Event> getSortedEvents() {
        EventLogSorter comparator = tableViewer.getComparator();

        EventLogItem[] allItems = tableContentProvider.getElements(null);
        Arrays.sort(allItems, (o1, o2) -> {
            return comparator.compare(tableViewer, o1, o2);
        });

        return Arrays.asList(allItems).stream().map(item -> item.event).collect(Collectors.toList());
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    @Override
    public void dispose() {
        tableUpdater.shutdown();
        if (subscription != null) {
            subscription.cancel(true);
        }
        EventLogPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(prefListener);
        super.dispose();
    }

    public void openConfigureColumnsDialog(Shell shell) {
        tableViewer.openConfigureColumnsDialog(shell);
    }
}
