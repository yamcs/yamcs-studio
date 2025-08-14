/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.yamcs.client.EventSubscription;
import org.yamcs.protobuf.Event;
import org.yamcs.protobuf.Event.EventSeverity;
import org.yamcs.protobuf.SubscribeEventsRequest;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.Debouncer;
import org.yamcs.studio.core.utils.RCPUtils;

public class EventLog extends Composite implements YamcsAware {

    public static final String CMD_SCROLL_LOCK = "org.yamcs.studio.eventlog.scrollLockCommand";
    public static final String CMD_EVENT_PROPERTIES = "org.yamcs.studio.eventlog.showDetailsCommand";
    public static final String CMDPARAM_EVENT_PROPERTY = "org.yamcs.studio.eventlog.copyDetails.property";
    public static final String STATE_SCROLL_LOCK = "org.eclipse.ui.commands.toggleState";
    private static final Logger log = Logger.getLogger(EventLog.class.getName());

    private static final long TABLE_UPDATE_RATE = 1000;

    private ResourceManager resourceManager;
    private EventLogTableViewer tableViewer;
    private EventLogContentProvider tableContentProvider;
    private EventLogSourceFilter sourceFilter;
    private MenuManager menuManager;
    private IPropertyChangeListener prefListener;

    private FormToolkit tk;
    private ScrolledForm detailForm;

    private Label detailSeverityLabel;
    private Label gentimeLabel;
    private Label rectimeLabel;
    private Label sourceLabel;
    private Label typeLabel;
    private Label messageLabel;

    private Image level0Image;
    private Image level1Image;
    private Image level2Image;
    private Image level3Image;
    private Image level4Image;
    private Image level5Image;

    private LinkedBlockingQueue<Event> realtimeEvents = new LinkedBlockingQueue<>();
    private ScheduledExecutorService tableUpdater = Executors.newSingleThreadScheduledExecutor();

    private EventSubscription subscription;

    public EventLog(Composite parent, int style) {
        super(parent, style);

        var plugin = EventLogPlugin.getDefault();
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        level0Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level0s.png"));
        level1Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level1s.png"));
        level2Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level2s.png"));
        level3Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level3s.png"));
        level4Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level4s.png"));
        level5Image = resourceManager.create(plugin.getImageDescriptor("/icons/eview16/level5s.png"));
        tk = new FormToolkit(parent.getDisplay());

        var gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        setLayout(gl);

        var filterBar = new Composite(this, SWT.NONE);
        filterBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        gl = new GridLayout(6, false);
        gl.marginHeight = 0;
        gl.verticalSpacing = 0;
        filterBar.setLayout(gl);

        var filterLabel = new Label(filterBar, SWT.NONE);
        filterLabel.setText("Filter:");

        var searchbox = new Text(filterBar, SWT.SEARCH | SWT.BORDER | SWT.ICON_CANCEL);
        searchbox.setMessage("type filter text");
        searchbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        var severityLabel = new Label(filterBar, SWT.NONE);
        severityLabel.setText("Level:");
        severityLabel.setToolTipText("Severity Level");

        var severityCombo = new Combo(filterBar, SWT.DROP_DOWN | SWT.READ_ONLY);
        severityCombo.setItems("INFO", "WATCH", "WARNING", "DISTRESS", "CRITICAL", "SEVERE");
        severityCombo.select(0);

        var sourceLabel = new Label(filterBar, SWT.NONE);
        sourceLabel.setText("Source:");
        var sourceCombo = new Combo(filterBar, SWT.DROP_DOWN | SWT.READ_ONLY);

        var tableDetailSplit = new SashForm(this, SWT.VERTICAL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableDetailSplit);

        var tableViewerWrapper = new Composite(tableDetailSplit, SWT.NONE);
        tableViewerWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewerWrapper.setLayout(new FillLayout());

        tableViewer = new EventLogTableViewer(tableViewerWrapper);
        tableContentProvider = new EventLogContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);

        // Register context menu. Commands are added in plugin.xml
        menuManager = new MenuManager();
        var menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);

        // Default action is to open Event properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(EventLog.CMD_EVENT_PROPERTIES);
        });

        tableViewer.addSelectionChangedListener(evt -> {
            var sel = evt.getStructuredSelection();
            if (sel.isEmpty()) {
                detailForm.setVisible(false);
            } else if (sel.getFirstElement() instanceof EventLogItem item) {
                detailForm.setVisible(true);
                updateEventDetail(item);
            }
        });

        // Listen to v_scroll to autotoggle scroll lock
        tableViewer.getTable().getVerticalBar().addListener(SWT.Selection, evt -> {
            var sortColumn = tableViewer.getTable().getSortColumn().getText();
            var up = (tableViewer.getTable().getSortDirection() == SWT.UP);

            if (!EventLogTableViewer.COL_GENERATION.equals(sortColumn)) {
                return;
            }

            // User controls sort direction, so events may be inserted anywhere really.
            // Probably the most intuitive, is to autolock if the user is either at the very top or the very bottom of
            // the scrollbar.
            var sel = tableViewer.getTable().getVerticalBar().getSelection();
            var thumb = tableViewer.getTable().getVerticalBar().getThumb();
            var min = tableViewer.getTable().getVerticalBar().getMinimum();
            var max = tableViewer.getTable().getVerticalBar().getMaximum();

            var service = PlatformUI.getWorkbench().getService(ICommandService.class);
            var command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
            var lockState = command.getState(RegistryToggleState.STATE_ID);
            var locked = ((Boolean) lockState.getValue()).booleanValue();
            var onEdge = (sel <= min && !up) || (sel + thumb >= max && up);

            if (locked && onEdge) {
                lockState.setValue(false);
                enableScrollLock(false);
            } else if (!locked && !onEdge) {
                lockState.setValue(true);
                enableScrollLock(true);
            }
        });

        var searchBoxFilter = new EventLogSearchBoxFilter();
        tableViewer.addFilter(searchBoxFilter);
        var debouncer = new Debouncer(tableUpdater);
        searchbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.keyCode == SWT.ARROW_DOWN) {
                    var items = tableViewer.getTable().getItems();
                    if (items.length > 0) {
                        tableViewer.getTable().setSelection(items[0]);
                        tableViewer.getTable().setFocus();
                    }
                } else {
                    var searchString = searchbox.getText();
                    debouncer.debounce(() -> {
                        searchBoxFilter.setSearchTerm(searchString);
                        getDisplay().syncExec(() -> tableViewer.refresh());
                    }, 400, TimeUnit.MILLISECONDS);
                }
            }
        });

        // The DefaultSelection event is generated when the cancel icon
        // is clicked.
        searchbox.addListener(SWT.DefaultSelection, evt -> {
            searchbox.setText("");
            searchBoxFilter.setSearchTerm("");
            tableViewer.refresh();
        });

        var severityFilter = new EventLogSeverityFilter();
        tableViewer.addFilter(severityFilter);
        severityCombo.addListener(SWT.Selection, evt -> {
            var severity = EventSeverity.valueOf(severityCombo.getText());
            severityFilter.setMinimumSeverity(severity);
            tableViewer.refresh();
        });

        sourceFilter = new EventLogSourceFilter(sourceCombo);
        tableViewer.addFilter(sourceFilter);
        sourceCombo.addListener(SWT.Selection, evt -> {
            tableViewer.refresh();
        });

        var entryDetail = new Composite(tableDetailSplit, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(entryDetail);
        entryDetail.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        entryDetail.setLayout(new FillLayout());
        createEventDetail(entryDetail);

        layoutSashForm(tableDetailSplit, UIPreferences.EVENT_DETAIL_SPLIT);

        updateState();

        tableUpdater.scheduleWithFixedDelay(() -> {
            if (realtimeEvents.isEmpty() || isDisposed()) {
                return;
            }

            var eventBatch = new ArrayList<Event>();
            realtimeEvents.drainTo(eventBatch);
            if (!eventBatch.isEmpty()) {
                Display.getDefault().syncExec(() -> addEvents(eventBatch));
            }
        }, TABLE_UPDATE_RATE, TABLE_UPDATE_RATE, TimeUnit.MILLISECONDS);

        prefListener = evt -> {
            if (evt.getProperty().equals(PreferencePage.PREF_RULES)) {
                var rules = plugin.composeColoringRules((String) evt.getNewValue());
                for (var item : tableContentProvider.getElements(null)) {
                    item.colorize(rules);
                }
                tableViewer.refresh();
            }
        };

        YamcsPlugin.addListener(this);
        plugin.getPreferenceStore().addPropertyChangeListener(prefListener);
    }

    private void createEventDetail(Composite parent) {
        detailForm = tk.createScrolledForm(parent);
        detailForm.setVisible(false);

        var body = detailForm.getBody();
        var layout = new TableWrapLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        body.setLayout(layout);

        var leftColumn = tk.createComposite(body, SWT.NONE);
        leftColumn.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        leftColumn.setLayout(new TableWrapLayout());

        var infoSection = tk.createSection(leftColumn, Section.TITLE_BAR);
        infoSection.setText("General Information");
        var infoSectionData = new TableWrapData(TableWrapData.FILL_GRAB);
        infoSection.setLayoutData(infoSectionData);

        var detailInfoSectionClient = tk.createComposite(infoSection);
        var infoLayout = new TableWrapLayout();
        infoLayout.numColumns = 2;
        detailInfoSectionClient.setLayout(infoLayout);
        infoSection.setClient(detailInfoSectionClient);

        tk.createLabel(detailInfoSectionClient, "Severity:");
        detailSeverityLabel = tk.createLabel(detailInfoSectionClient, "");
        detailSeverityLabel.setImage(level0Image);
        var wrapData = new TableWrapData(TableWrapData.LEFT, TableWrapData.MIDDLE);
        wrapData.grabHorizontal = true;
        wrapData.grabVertical = true;
        detailSeverityLabel.setLayoutData(wrapData);

        tk.createLabel(detailInfoSectionClient, "Generation Time:");
        gentimeLabel = tk.createLabel(detailInfoSectionClient, "");
        tk.createLabel(detailInfoSectionClient, "Reception Time:");
        rectimeLabel = tk.createLabel(detailInfoSectionClient, "");
        tk.createLabel(detailInfoSectionClient, "Source:");
        sourceLabel = tk.createLabel(detailInfoSectionClient, "");
        tk.createLabel(detailInfoSectionClient, "Type:");
        typeLabel = tk.createLabel(detailInfoSectionClient, "");

        var rightColumn = tk.createComposite(body, SWT.NONE);
        rightColumn.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        rightColumn.setLayout(new TableWrapLayout());

        var messageSection = tk.createSection(rightColumn, Section.TITLE_BAR);
        messageSection.setText("Event Message");
        var messageSectionData = new TableWrapData(TableWrapData.FILL_GRAB);
        messageSection.setLayoutData(messageSectionData);

        var detailMessageSectionClient = tk.createComposite(messageSection);
        detailMessageSectionClient.setLayout(new TableWrapLayout());
        messageSection.setClient(detailMessageSectionClient);
        messageLabel = tk.createLabel(detailMessageSectionClient, "");
        var terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        messageLabel.setFont(terminalFont);
    }

    private void updateEventDetail(EventLogItem evt) {
        var levelImage = level0Image;
        if (evt.event.hasSeverity()) {
            var level = evt.event.getSeverity();
            switch (level) {
            case INFO:
                levelImage = level0Image;
                break;
            case WATCH:
                levelImage = level1Image;
                break;
            case WARNING:
                levelImage = level2Image;
                break;
            case DISTRESS:
                levelImage = level3Image;
                break;
            case CRITICAL:
                levelImage = level4Image;
                break;
            case SEVERE:
                levelImage = level5Image;
                break;
            default:
                levelImage = level0Image;
            }
        }
        detailSeverityLabel.setImage(levelImage);

        if (evt.event.hasGenerationTime()) {
            var generationTime = Instant.ofEpochSecond(evt.event.getGenerationTime().getSeconds(),
                    evt.event.getGenerationTime().getNanos());
            gentimeLabel.setText(YamcsPlugin.getDefault().formatInstant(generationTime));
        } else {
            gentimeLabel.setText("");
        }

        if (evt.event.hasReceptionTime()) {
            var receptionTime = Instant.ofEpochSecond(evt.event.getReceptionTime().getSeconds(),
                    evt.event.getReceptionTime().getNanos());
            rectimeLabel.setText(YamcsPlugin.getDefault().formatInstant(receptionTime));
        } else {
            rectimeLabel.setText("");
        }

        if (evt.event.hasSource()) {
            sourceLabel.setText(evt.event.getSource());
        } else {
            sourceLabel.setText("");
        }

        if (evt.event.hasType()) {
            typeLabel.setText(evt.event.getType());
        } else {
            typeLabel.setText("");
        }

        if (evt.event.hasMessage()) {
            messageLabel.setText(evt.event.getMessage());
        } else {
            messageLabel.setText("");
        }

        detailForm.layout(true, true);
        detailForm.reflow(true);
    }

    private void updateState() {
        var service = PlatformUI.getWorkbench().getService(ICommandService.class);
        var command = service.getCommand(EventLog.CMD_SCROLL_LOCK);
        var state = command.getState(EventLog.STATE_SCROLL_LOCK);
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

    @Override
    public void changeInstance(String instance) {
        if (subscription != null) {
            subscription.cancel(true);
        }
        Display.getDefault().syncExec(this::clear);

        if (instance != null) {
            Display.getDefault().asyncExec(() -> {
                fetchLatestEvents();
            });
            subscription = YamcsPlugin.getYamcsClient().createEventSubscription();
            subscription.addMessageListener(realtimeEvents::offer);
            subscription.sendMessage(SubscribeEventsRequest.newBuilder().setInstance(instance).build());
        }
    }

    public EventLogItem getPreviousRecord(EventLogItem rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            var indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] > 0) {
                var prevIndex = indices[0] - 1;
                return (EventLogItem) tableViewer.getElementAt(prevIndex);
            }
        }
        return null;
    }

    public EventLogItem getNextRecord(EventLogItem rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            var indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] < tableViewer.getTable().getItemCount() - 1) {
                var nextIndex = indices[0] + 1;
                return (EventLogItem) tableViewer.getElementAt(nextIndex);
            }
        }
        return null;
    }

    private void fetchLatestEvents() {
        var archiveClient = YamcsPlugin.getArchiveClient();
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

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    public void addEvents(List<Event> events) {
        if (isDisposed()) {
            return;
        }
        tableContentProvider.addEvents(events);
    }

    public void clear() {
        sourceFilter.clear();
        tableContentProvider.clearAll();
        tableViewer.setInput(null);
        tableViewer.setSelection(null);
    }

    /**
     * Returns the collection of currently visible events (sorted as is visible)
     */
    public List<Event> getSortedEvents() {
        var comparator = tableViewer.getComparator();

        var allItems = tableContentProvider.getElements(null);
        Arrays.sort(allItems, (o1, o2) -> comparator.compare(tableViewer, o1, o2));

        return Arrays.asList(allItems).stream().map(item -> item.event).collect(Collectors.toList());
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    private void layoutSashForm(SashForm sf, String key) {
        var store = EventLogPlugin.getDefault().getPreferenceStore();

        sf.addDisposeListener(e -> {
            var w = sf.getWeights();
            store.putValue(key, UIPreferences.intArrayToString(w));
            saveStoreIfNeeded();

        });
        var weights = UIPreferences.stringToIntArray(store.getString(key), 2);
        if (weights == null) {
            // Corrupted preferences?
            weights = UIPreferences.stringToIntArray(store.getDefaultString(key), 2);
        }
        sf.setWeights(weights);
    }

    private void saveStoreIfNeeded() {
        var store = EventLogPlugin.getDefault().getPreferenceStore();
        if (store.needsSaving() && store instanceof IPersistentPreferenceStore pStore) {
            try {
                pStore.save();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public void dispose() {
        tableUpdater.shutdown();
        if (resourceManager != null) {
            resourceManager.dispose();
        }
        if (subscription != null) {
            subscription.cancel(true);
        }
        if (tk != null) {
            tk.dispose();
        }
        EventLogPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(prefListener);
        YamcsPlugin.removeListener(this);
        super.dispose();
    }

    public void openConfigureColumnsDialog(Shell shell) {
        tableViewer.openConfigureColumnsDialog(shell);
    }
}
