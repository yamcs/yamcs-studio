package org.yamcs.studio.eventlog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.yamcs.protobuf.Rest.ListEventsResponse;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.RCPUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public class EventLog extends Composite implements YamcsConnectionListener, InstanceListener, EventListener {

    public static final String CMD_SCROLL_LOCK = "org.yamcs.studio.eventlog.scrollLockCommand";
    public static final String CMD_EVENT_PROPERTIES = "org.yamcs.studio.eventlog.showDetailsCommand";
    public static final String CMDPARAM_EVENT_PROPERTY = "org.yamcs.studio.eventlog.copyDetails.property";
    public static final String STATE_SCROLL_LOCK = "org.eclipse.ui.commands.toggleState";

    private static final long TABLE_UPDATE_RATE = 1000;

    private static final Logger log = Logger.getLogger(EventLog.class.getName());

    private EventLogTableViewer tableViewer;
    private EventLogContentProvider tableContentProvider;
    private MenuManager menuManager;

    private List<Event> realtimeEvents = new ArrayList<>();
    private ScheduledExecutorService tableUpdater = Executors.newSingleThreadScheduledExecutor();

    public EventLog(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());

        tableViewer = new EventLogTableViewer(this);
        tableContentProvider = new EventLogContentProvider(tableViewer.getTable());
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
            boolean onEdge = sel <= min || sel + thumb >= max;

            if (locked && onEdge) {
                lockState.setValue(false);
                enableScrollLock(false);
            } else if (!locked && !onEdge) {
                lockState.setValue(true);
                enableScrollLock(true);
            }
        });

        updateState();

        tableUpdater.scheduleWithFixedDelay(() -> {
            synchronized (realtimeEvents) {
                if (realtimeEvents.isEmpty() || isDisposed()) {
                    return;
                }
                List<Event> eventBatch = new ArrayList<>(realtimeEvents);
                realtimeEvents.clear();
                Display.getDefault().asyncExec(() -> addEvents(eventBatch, false));
            }
        }, TABLE_UPDATE_RATE, TABLE_UPDATE_RATE, TimeUnit.MILLISECONDS);
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

    public void connect() {
        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null) {
            EventCatalogue.getInstance().addEventListener(this);
        }
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public boolean setFocus() {
        return tableViewer.getTable().setFocus();
    }

    @Override
    public void onYamcsConnected() {
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
    public void onYamcsDisconnected() {
        // NOP for now
    }

    private void fetchLatestEvents() {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        EventCatalogue.getInstance().fetchLatestEvents(instance).whenComplete((data, exc) -> {
            try {
                ListEventsResponse response = ListEventsResponse.parseFrom(data);
                Display.getDefault().asyncExec(() -> {
                    addEvents(response.getEventList(), true);
                });
            } catch (InvalidProtocolBufferException e) {
                log.log(Level.SEVERE, "Failed to decode server message", e);
            }
        });
    }

    @Override
    public void processEvent(Event event) {
        synchronized (realtimeEvents) {
            realtimeEvents.add(event);
        }
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    public void addEvent(Event event) {
        if (isDisposed()) {
            return;
        }
        tableContentProvider.addEvent(event);
    }

    public void addEvents(List<Event> events, boolean forceNoReveal) {
        if (isDisposed()) {
            return;
        }
        tableContentProvider.addEvents(events, forceNoReveal);
    }

    // This method should be called when the stream of events to be imported is ended
    public void addedAllEvents() {
        tableContentProvider.addedAllEvents();
    }

    public void clear() {
        tableContentProvider.clearAll();
        tableViewer.setInput(null);
        tableViewer.setSelection(null);
    }

    public List<Event> getEvents() {
        return tableContentProvider.getSortedEvents();
    }

    @Override
    public void dispose() {
        tableUpdater.shutdown();
        EventCatalogue.getInstance().addEventListener(this);
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        super.dispose();
    }
}
