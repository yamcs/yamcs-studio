package org.yamcs.studio.archive;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;
import org.yamcs.client.archive.ArchiveClient.ListOptions;
import org.yamcs.protobuf.IndexGroup;
import org.yamcs.studio.archive.Histogram.HistogramKind;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.connections.ConnectionStateProvider;
import org.yamcs.studio.core.ui.processor.ProcessorStateProvider;

public class ArchiveView extends ViewPart implements YamcsAware, ISourceProviderListener {

    private Timeline timeline;
    private ReplayOptions replayOptions;
    private GridData replayOptionsGridData;

    private ProcessorStateProvider processorState;
    private ConnectionStateProvider connectionState;

    private Debouncer viewportChangeDebouncer;

    @Override
    public void createPartControl(Composite parent) {
        var contentArea = new Composite(parent, SWT.NONE);
        contentArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        var gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        contentArea.setLayout(gl);

        timeline = new Timeline(contentArea, SWT.NONE);
        timeline.setLayoutData(new GridData(GridData.FILL_BOTH));
        var start = Instant.now().atOffset(UTC).truncatedTo(DAYS);
        var stop = start.plusDays(1);
        timeline.setBounds(start, stop);

        new MouseTracker(timeline);
        new TimeLocator(timeline, () -> YamcsPlugin.getMissionTime(true).atOffset(UTC));
        var timeRuler = new TimeRuler(timeline);
        timeRuler.setFrozen(true);

        replayOptions = new ReplayOptions(contentArea, this);
        replayOptionsGridData = new GridData(GridData.FILL_HORIZONTAL);
        replayOptions.setLayoutData(replayOptionsGridData);

        var service = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
        processorState = (ProcessorStateProvider) service
                .getSourceProvider(ProcessorStateProvider.STATE_KEY_PROCESSING);
        processorState.addSourceProviderListener(this);
        connectionState = (ConnectionStateProvider) service
                .getSourceProvider(ConnectionStateProvider.STATE_KEY_CONNECTED);
        connectionState.addSourceProviderListener(this);

        viewportChangeDebouncer = new Debouncer();
        timeline.addViewportChangeListener(evt -> {
            viewportChangeDebouncer.debounce(() -> {
                Display.getDefault().asyncExec(() -> refreshData());
            }, 400, TimeUnit.MILLISECONDS);
        });

        timeline.addCanvasMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                var clickedTime = timeline.mouse2time(e.x);
                if (replayOptions.isVisible()) {
                    seekReplay(clickedTime);
                }
            }
        });

        createActions();
        YamcsPlugin.addListener(this);
        updateState();
    }

    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public void setFocus() {
        timeline.setFocus();
    }

    private void toggleReplayOptions(boolean enabled) {
        replayOptionsGridData.exclude = !enabled;
        replayOptions.setVisible(enabled);
        replayOptions.getParent().layout();
    }

    @Override
    public void updateTime(Instant time) {
        Display.getDefault().asyncExec(() -> {
            if (!timeline.isDisposed()) {
                timeline.redraw();
            }
        });
    }

    @Override
    public void changeInstance(String instance) {
        Display.getDefault().asyncExec(() -> {
            refreshData();

            var window = getViewSite().getWorkbenchWindow();
            var service = (ISourceProviderService) window.getService(ISourceProviderService.class);
            var commandState = (RefreshStateProvider) service.getSourceProvider(RefreshStateProvider.STATE_KEY_ENABLED);
            commandState.setEnabled(instance != null);
        });
    }

    public void seekReplay(OffsetDateTime newPosition) {
        var processorClient = YamcsPlugin.getProcessorClient();
        processorClient.seek(newPosition.toInstant());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
        updateState();
    }

    @Override
    public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
        updateState();
    }

    /*
     * Use the state available in the workbench-level state provider.
     */
    private void updateState() {

        // Refresh the current states
        var connected = (Boolean) connectionState.getCurrentState().get(ConnectionStateProvider.STATE_KEY_CONNECTED);
        var processing = (String) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_PROCESSING);
        var replay = (Boolean) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_REPLAY);
        var replaySpeed = (Float) processorState.getCurrentState().get(ProcessorStateProvider.STATE_KEY_REPLAY_SPEED);

        if (connected == null || processing == null || replay == null || replaySpeed == null) {
            return;
        }

        toggleReplayOptions(replay);
        replayOptions.updateState(connected, processing, replay, replaySpeed);
    }

    private void createActions() {
        var actionBars = getViewSite().getActionBars();
        var menuManager = actionBars.getMenuManager();
        menuManager.add(new Action("Today", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var start = YamcsPlugin.getMissionTime(true).truncatedTo(DAYS).atOffset(UTC);
                    var stop = start.plusDays(1);
                    timeline.setBounds(start, stop);
                }
            }
        });
        menuManager.add(new Action("Last week", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var missionTime = YamcsPlugin.getMissionTime(true);
                    var start = missionTime.atOffset(UTC).minus(7, DAYS).truncatedTo(DAYS);
                    var stop = missionTime.atOffset(UTC).truncatedTo(DAYS).plusDays(1);
                    timeline.setBounds(start, stop);
                }
            }
        });
        menuManager.add(new Action("Last month", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var missionTime = YamcsPlugin.getMissionTime(true);
                    var start = missionTime.atOffset(UTC).minus(30, DAYS).truncatedTo(DAYS);
                    var stop = missionTime.atOffset(UTC).truncatedTo(DAYS).plusDays(1);
                    timeline.setBounds(start, stop);
                }
            }
        });
        menuManager.add(new Action("Last 3 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var missionTime = YamcsPlugin.getMissionTime(true);
                    var start = missionTime.atOffset(UTC).minus(90, DAYS).truncatedTo(DAYS);
                    var stop = missionTime.atOffset(UTC).truncatedTo(DAYS).plusDays(1);
                    timeline.setBounds(start, stop);
                }
            }
        });
        menuManager.add(new Action("Last 12 months", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var missionTime = YamcsPlugin.getMissionTime(true);
                    var start = missionTime.atOffset(UTC).minus(365, DAYS).truncatedTo(DAYS);
                    var stop = missionTime.atOffset(UTC).truncatedTo(DAYS).plusDays(1);
                    timeline.setBounds(start, stop);
                }
            }
        });
        menuManager.add(new Action("Custom...", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    var dialog = new CustomizeRangeDialog(timeline.getDisplay().getActiveShell());
                    dialog.setInitialRange(timeline.getStart().toInstant(), timeline.getStop().toInstant());
                    if (dialog.open() == Window.OK) {
                        timeline.setBounds(dialog.getStartTime().atOffset(UTC), dialog.getStopTime().atOffset(UTC));
                    }
                }
            }
        });
    }

    public void refreshData() {
        var archive = YamcsPlugin.getArchiveClient();
        if (archive != null) {
            var range = MILLIS.between(timeline.getStart(), timeline.getStop());
            var start = timeline.getStart().toInstant().minus(range, MILLIS);
            var stop = timeline.getStop().toInstant().plus(range, MILLIS);

            var futures = new ArrayList<CompletableFuture<Void>>();
            var completenessGroups = new ArrayList<IndexGroup>();
            var tmGroups = new ArrayList<IndexGroup>();
            var ppGroups = new ArrayList<IndexGroup>();
            var tcGroups = new ArrayList<IndexGroup>();
            var evGroups = new ArrayList<IndexGroup>();
            futures.add(archive.listCompletenessIndex(start, stop, ListOptions.limit(1000))
                    .thenAccept(page -> page.forEach(completenessGroups::add)));
            futures.add(archive.listPacketIndex(start, stop, ListOptions.limit(1000))
                    .thenAccept(page -> page.forEach(tmGroups::add)));
            futures.add(archive.listParameterIndex(start, stop, ListOptions.limit(1000))
                    .thenAccept(page -> page.forEach(ppGroups::add)));
            futures.add(archive.listCommandIndex(start, stop, ListOptions.limit(1000))
                    .thenAccept(page -> page.forEach(tcGroups::add)));
            futures.add(archive.listEventIndex(start, stop, ListOptions.limit(1000))
                    .thenAccept(page -> page.forEach(evGroups::add)));

            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).whenComplete((v, err) -> {
                timeline.getDisplay().asyncExec(() -> {
                    timeline.clearLines();
                    new Header(timeline, "Completeness");
                    completenessGroups.forEach(group -> {
                        var histogram = new Histogram(timeline, HistogramKind.COMPLETENESS, group.getId().getName());
                        histogram.setData(group.getEntryList());
                    });
                    new Header(timeline, "TM Histogram");
                    tmGroups.forEach(group -> {
                        var histogram = new Histogram(timeline, HistogramKind.TM, group.getId().getName());
                        histogram.setData(group.getEntryList());
                    });
                    new Header(timeline, "PP Histogram");
                    ppGroups.forEach(group -> {
                        var histogram = new Histogram(timeline, HistogramKind.PP, group.getId().getName());
                        histogram.setData(group.getEntryList());
                    });
                    new Header(timeline, "TC Histogram");
                    tcGroups.forEach(group -> {
                        var histogram = new Histogram(timeline, HistogramKind.CMDHIST, group.getId().getName());
                        histogram.setData(group.getEntryList());
                    });
                    new Header(timeline, "EV Histogram");
                    evGroups.forEach(group -> {
                        var histogram = new Histogram(timeline, HistogramKind.EVENT, group.getId().getName());
                        histogram.setData(group.getEntryList());
                    });
                });
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (viewportChangeDebouncer != null) {
            viewportChangeDebouncer.shutdown();
        }
        YamcsPlugin.removeListener(this);
        processorState.removeSourceProviderListener(this);
        connectionState.removeSourceProviderListener(this);
    }
}
