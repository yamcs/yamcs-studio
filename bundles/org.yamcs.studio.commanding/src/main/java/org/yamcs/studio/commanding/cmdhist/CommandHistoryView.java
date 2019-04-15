package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Rest.ListCommandsResponse;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.ColumnData;
import org.yamcs.studio.core.ui.utils.ColumnDef;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.ui.utils.ViewerColumnsDialog;

import com.google.protobuf.InvalidProtocolBufferException;

public class CommandHistoryView extends ViewPart implements YamcsConnectionListener, InstanceListener {

    private static final Logger log = Logger.getLogger(CommandHistoryView.class.getName());

    public static final String COL_COMPLETION = "";
    public static final String COL_COMMAND = "Command";
    public static final String COL_ORIGIN_ID = "Ori.ID";
    public static final String COL_USER = "User";
    public static final String COL_ORIGIN = "Origin";
    public static final String COL_SEQ_ID = "Seq.ID";
    public static final String COL_PTV = "PTV";
    public static final String COL_T = "T";

    private static final int DYNAMIC_COLUMN_WIDTH = 90;

    // Ignored for dynamic columns, most of these are actually considered fixed
    // columns.
    private static final List<String> IGNORED_ATTRIBUTES = Arrays.asList("cmdName",
            CommandHistoryRecordContentProvider.ATTR_BINARY,
            CommandHistoryRecordContentProvider.ATTR_USERNAME,
            CommandHistoryRecordContentProvider.ATTR_SOURCE,
            CommandHistoryRecordContentProvider.ATTR_FINAL_SEQUENCE_COUNT,
            CommandHistoryRecordContentProvider.ATTR_TRANSMISSION_CONSTRAINTS,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_COMPLETE,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_FAILED,
            CommandHistoryRecordContentProvider.ATTR_COMMENT,
            CommandHistoryRecordContentProvider.ATTR_COMMENT_NEW);

    private LocalResourceManager resourceManager;

    Image greenBubble;
    Image redBubble;
    Image grayBubble;
    Image waitingImage;
    Image headerCompleteImage;
    Image checkmarkImage;
    Image errorImage;
    Image prevImage;
    Image nextImage;

    private TableViewer tableViewer;
    private CommandHistorySorter tableViewerComparator;
    private ControlAdapter columnResizeListener = new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e) {
            syncCurrentWidthsToModel();
            saveColumnState();
        }
    };

    private CommandHistoryRecordContentProvider tableContentProvider;

    private ColumnData columnData;
    private List<String> dynamicColumns = new ArrayList<>();

    private boolean showRelativeTime = true;

    @Override
    public void createPartControl(Composite parent) {
        CommandingPlugin plugin = CommandingPlugin.getDefault();
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/ok.png"));
        redBubble = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/nok.png"));
        grayBubble = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/undef.png"));
        waitingImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/waiting.png"));
        headerCompleteImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/header_complete.png"));
        checkmarkImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/checkmark.gif"));
        errorImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/error_tsk.png"));
        prevImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/event_prev.png"));
        nextImage = resourceManager.createImage(plugin.getImageDescriptor("icons/obj16/event_next.png"));

        createActions(parent.getShell());

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);

        columnData = createDefaultColumnData();
        restoreColumnState();
        createColumns();

        tableContentProvider = new CommandHistoryRecordContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new CommandHistorySorter();
        tableViewer.setComparator(tableViewerComparator);

        getViewSite().setSelectionProvider(tableViewer);

        // Register context menu. Commands are added in plugin.xml
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewer);

        // Default action is to open Command properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(CommandHistory.CMD_COMMAND_PROPERTIES);
        });

        getViewSite().setSelectionProvider(tableViewer);

        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
        CommandingCatalogue.getInstance().addCommandHistoryListener(cmdhistEntry -> {
            Display.getDefault().asyncExec(() -> processCommandHistoryEntry(cmdhistEntry));
        });
    }

    @Override
    public void onYamcsConnected() {
        Display.getDefault().asyncExec(() -> {
            clear();
            fetchLatestEntries();
        });
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        Display.getDefault().asyncExec(() -> {
            clear();
            fetchLatestEntries();
        });
    }

    @Override
    public void onYamcsDisconnected() {
        Display.getDefault().asyncExec(() -> clear());
    }

    public ColumnData createDefaultColumnData() {
        ColumnData data = new ColumnData();
        data.addColumn(COL_COMPLETION, 90);
        data.addColumn(COL_T, 160);
        data.addColumn(COL_COMMAND, 500);
        data.addColumn(COL_USER, 100);
        data.addColumn(COL_ORIGIN, 200, false, true, true);
        data.addColumn(COL_ORIGIN_ID, 50, false, true, true);
        data.addColumn(COL_PTV, 50);
        data.addColumn(COL_SEQ_ID, 50, false, true, true);

        for (String dynamicColumn : dynamicColumns) {
            ColumnDef def = columnData.getColumn(dynamicColumn);
            data.addColumn(def.name, DYNAMIC_COLUMN_WIDTH);
        }
        return data;
    }

    public void clear() {
        tableContentProvider.clearAll();
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    public void setShowRelativeTime(boolean enabled) {
        this.showRelativeTime = enabled;
    }

    public CommandHistoryRecord getPreviousRecord(CommandHistoryRecord rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            int[] indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] > 0) {
                int prevIndex = indices[0] - 1;
                return (CommandHistoryRecord) tableViewer.getElementAt(prevIndex);
            }
        }
        return null;
    }

    public CommandHistoryRecord getNextRecord(CommandHistoryRecord rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            int[] indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] < tableViewer.getTable().getItemCount() - 1) {
                int nextIndex = indices[0] + 1;
                return (CommandHistoryRecord) tableViewer.getElementAt(nextIndex);
            }
        }
        return null;
    }

    private void createActions(Shell shell) {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();

        Action showRelativeTimeAction = new Action("Show Relative Time", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                showRelativeTime = isChecked();
                tableViewer.refresh();
            }
        };
        showRelativeTimeAction.setChecked(showRelativeTime);
        mgr.add(showRelativeTimeAction);

        mgr.add(new Separator());

        Action configureColumnsAction = new Action("Configure Columns...", IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                syncCurrentWidthsToModel();
                ViewerColumnsDialog dialog = new CommandHistoryViewerColumnsDialog(shell, CommandHistoryView.this,
                        columnData);
                if (dialog.open() == Dialog.OK) {
                    columnData.clear();
                    dialog.getVisible().forEach(c -> {
                        columnData.addColumn(c.name, c.width, c.visible, c.resizable, c.moveable);
                    });
                    dialog.getNonVisible().forEach(c -> {
                        columnData.addColumn(c.name, c.width, c.visible, c.resizable, c.moveable);
                    });
                    createColumns();
                }
            }
        };
        mgr.add(configureColumnsAction);
    }

    @Override
    public void dispose() {
        YamcsPlugin.getDefault().removeYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().removeInstanceListener(this);
        super.dispose();
    }

    private void syncCurrentWidthsToModel() {
        for (TableColumn column : tableViewer.getTable().getColumns()) {
            ColumnDef def = columnData.getColumn(column.getText());
            if (def != null) {
                def.width = column.getWidth();
            }
        }
    }

    private void createColumns() {
        Table table = tableViewer.getTable();
        TableLayout layout = new TableLayout();

        TableColumn[] currentColumns = tableViewer.getTable().getColumns();
        for (TableColumn currentColumn : currentColumns) {
            currentColumn.dispose();
        }

        for (ColumnDef def : columnData.getVisibleColumns()) {
            if (def.name.equals(COL_COMPLETION)) {
                TableViewerColumn completionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                completionColumn.getColumn().addControlListener(columnResizeListener);
                completionColumn.getColumn().addSelectionListener(getSelectionAdapter(completionColumn.getColumn()));
                completionColumn.getColumn().setImage(headerCompleteImage);
                completionColumn.getColumn().setToolTipText("Completion");
                completionColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        switch (((CommandHistoryRecord) element).getCommandState()) {
                        case COMPLETED:
                            return checkmarkImage;
                        case FAILED:
                            return errorImage;
                        default:
                            return null;
                        }
                    }

                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        switch (((CommandHistoryRecord) element).getCommandState()) {
                        case COMPLETED:
                            return "Completed";
                        case FAILED:
                            return rec.getPTVInfo().getFailureMessage();
                        default:
                            return null;
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_T)) {
                TableViewerColumn gentimeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                gentimeColumn.getColumn().addControlListener(columnResizeListener);
                gentimeColumn.getColumn().addSelectionListener(getSelectionAdapter(gentimeColumn.getColumn()));
                gentimeColumn.getColumn().setText(COL_T);
                gentimeColumn.getColumn().setToolTipText("Generation Time");
                gentimeColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CommandHistoryRecord) element).getGenerationTime();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));

                // Add chevron
                table.setSortColumn(gentimeColumn.getColumn());
                table.setSortDirection(SWT.UP);
            } else if (def.name.equals(COL_COMMAND)) {
                TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
                nameColumn.getColumn().setText(COL_COMMAND);
                nameColumn.getColumn().setToolTipText("Command String");
                nameColumn.getColumn().addControlListener(columnResizeListener);
                nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
                nameColumn.setLabelProvider(new ColumnLabelProvider() {

                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        return rec.getCommandString();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_USER)) {
                TableViewerColumn userColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                userColumn.getColumn().addSelectionListener(getSelectionAdapter(userColumn.getColumn()));
                userColumn.getColumn().setText(COL_USER);
                userColumn.getColumn().setToolTipText("User that issued the command");
                userColumn.getColumn().addControlListener(columnResizeListener);
                userColumn.getColumn().addSelectionListener(getSelectionAdapter(userColumn.getColumn()));
                userColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        return rec.getUsername();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_ORIGIN)) {
                TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.getColumn().setText(COL_ORIGIN);
                originColumn.getColumn().setToolTipText("Origin");
                originColumn.getColumn().addControlListener(columnResizeListener);
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        return rec.getOrigin();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_ORIGIN_ID)) {
                TableViewerColumn seqIdColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setText(COL_ORIGIN_ID);
                seqIdColumn.getColumn().addControlListener(columnResizeListener);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setToolTipText("Client ID at origin");
                seqIdColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return String.valueOf(((CommandHistoryRecord) element).getSequenceNumber());
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_PTV)) {
                TableViewerColumn ptvColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                ptvColumn.getColumn().setText(COL_PTV);
                ptvColumn.getColumn().addControlListener(columnResizeListener);
                ptvColumn.getColumn().addSelectionListener(getSelectionAdapter(ptvColumn.getColumn()));
                ptvColumn.getColumn().setToolTipText("Pre-Transmission Verification");
                ptvColumn.setLabelProvider(new CenteredImageLabelProvider() {

                    @Override
                    public Image getImage(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        switch (rec.getPTVInfo().getState()) {
                        case UNDEF:
                            return grayBubble;
                        case NA:
                        case OK:
                            return greenBubble;
                        case PENDING:
                            return waitingImage;
                        case NOK:
                            return redBubble;
                        default:
                            log.warning("Unexpected PTV state " + rec.getPTVInfo().getState());
                            return grayBubble;
                        }
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        if (rec.getPTVInfo().getFailureMessage() != null) {
                            return rec.getPTVInfo().getFailureMessage();
                        } else {
                            return super.getToolTipText(element);
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_SEQ_ID)) {
                TableViewerColumn finalSeqColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                finalSeqColumn.getColumn().setText(COL_SEQ_ID);
                finalSeqColumn.getColumn().addControlListener(columnResizeListener);
                finalSeqColumn.getColumn().addSelectionListener(getSelectionAdapter(finalSeqColumn.getColumn()));
                finalSeqColumn.getColumn().setToolTipText("Final Sequence Count");
                finalSeqColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        return (rec.getFinalSequenceCount() != null) ? String.valueOf(rec.getFinalSequenceCount())
                                : "-";
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (dynamicColumns.contains(def.name)) {
                TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
                column.getColumn().setText(def.name);
                column.getColumn().addControlListener(columnResizeListener);
                column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));
                column.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CommandHistoryRecord) element).getTextForColumn(def.name, showRelativeTime);
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        return ((CommandHistoryRecord) element).getTooltipForColumn(def.name);
                    }

                    @Override
                    public Image getImage(Object element) {
                        String imgLoc = ((CommandHistoryRecord) element).getImageForColumn(def.name);
                        if (CommandHistoryRecordContentProvider.GREEN.equals(imgLoc)) {
                            return greenBubble;
                        } else if (CommandHistoryRecordContentProvider.RED.equals(imgLoc)) {
                            return redBubble;
                        } else {
                            return null;
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            }
        }

        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.layout(true); // !! Ensures column widths are applied when recreating columns
        tableViewer.refresh(); // !! Ensures table renders correctly for old data when adding a new column

        saveColumnState();
    }

    private void saveColumnState() {
        IDialogSettings settings = CommandingPlugin.getDefault().getCommandHistoryTableSettings();

        List<ColumnDef> visibleColumns = columnData.getVisibleColumns();
        String[] visibleNames = visibleColumns.stream().map(c -> c.name).toArray(String[]::new);
        Integer[] visibleWidths = visibleColumns.stream().map(c -> c.width).toArray(Integer[]::new);

        settings.put("visible-cols", visibleNames);
        for (int i = 0; i < visibleNames.length; i++) {
            settings.put("visible-width-" + i, visibleWidths[i]);
        }

        List<ColumnDef> hiddenColumns = columnData.getHiddenColumns();
        String[] hiddenNames = hiddenColumns.stream().map(c -> c.name).toArray(String[]::new);
        settings.put("hidden-cols", hiddenNames);

        String[] dynamicColumns = this.dynamicColumns.toArray(new String[0]);
        settings.put("dynamic-cols", dynamicColumns);
    }

    private void restoreColumnState() {
        IDialogSettings settings = CommandingPlugin.getDefault().getCommandHistoryTableSettings();

        String[] oldVisibleNames = settings.getArray("visible-cols");
        if (oldVisibleNames != null) {
            int[] oldVisibleWidths = new int[oldVisibleNames.length];
            for (int i = 0; i < oldVisibleNames.length; i++) {
                oldVisibleWidths[i] = settings.getInt("visible-width-" + i);
            }
            String[] oldHiddenNames = settings.getArray("hidden-cols");
            List<String> oldDynamicColumns = Arrays.asList(settings.getArray("dynamic-cols"));

            ColumnData restoredData = new ColumnData();

            // Add visible columns we still remember from a previous session
            for (int i = 0; i < oldVisibleNames.length; i++) {
                ColumnDef def = columnData.getColumn(oldVisibleNames[i]);
                if (def != null) {
                    restoredData.addColumn(def.name, oldVisibleWidths[i], true, def.resizable, def.moveable);
                } else if (oldDynamicColumns.contains(oldVisibleNames[i])) {
                    restoredData.addColumn(oldVisibleNames[i], oldVisibleWidths[i], true, true, true);
                    dynamicColumns.add(oldVisibleNames[i]);
                } else {
                    // Ignore. Maybe a static column was renamed.
                }
            }

            // Add hidden columns we still remember from a previous session
            for (int i = 0; i < oldHiddenNames.length; i++) {
                ColumnDef def = columnData.getColumn(oldHiddenNames[i]);
                if (def != null) {
                    restoredData.addColumn(def.name, def.width, false, def.resizable, def.moveable);
                } else if (oldDynamicColumns.contains(oldHiddenNames[i])) {
                    restoredData.addColumn(oldHiddenNames[i], DYNAMIC_COLUMN_WIDTH, false, true, true);
                    dynamicColumns.add(oldHiddenNames[i]);
                } else {
                    // Ignore. Maybe a static column was renamed.
                }
            }

            // Ensure that any newly introduced columns remain known (to the right for now)
            for (ColumnDef def : columnData.getColumns()) {
                if (restoredData.getColumn(def.name) == null) {
                    restoredData.addColumn(def.name, def.width, def.visible, def.resizable, def.moveable);
                }
            }

            columnData = restoredData;
        }
    }

    private void fetchLatestEntries() {
        String instance = ManagementCatalogue.getCurrentYamcsInstance();
        CommandingCatalogue.getInstance().fetchLatestEntries(instance).whenComplete((data, exc) -> {
            try {
                ListCommandsResponse response = ListCommandsResponse.parseFrom(data);

                List<CommandHistoryEntry> entryList = new ArrayList<>(response.getEntryList());

                Display.getDefault().asyncExec(() -> {
                    addEntries(entryList);
                });
            } catch (InvalidProtocolBufferException e) {
                log.log(Level.SEVERE, "Failed to decode server message", e);
            }
        });
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (!IGNORED_ATTRIBUTES.contains(attr.getName())) {
                String shortName = CommandHistoryRecordContentProvider.toHumanReadableName(attr);

                if (!dynamicColumns.contains(shortName)) {
                    dynamicColumns.add(shortName);
                    columnData.addColumn(shortName, 90);
                    syncCurrentWidthsToModel();
                    createColumns();
                }
            }
        }

        // Now add content
        tableContentProvider.processCommandHistoryEntry(cmdhistEntry);
    }

    public void addEntries(List<CommandHistoryEntry> entries) {
        if (tableViewer.getTable().isDisposed()) {
            return;
        }
        tableContentProvider.addEntries(entries);
    }

    private SelectionAdapter getSelectionAdapter(TableColumn column) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableViewerComparator.setColumn(column);
                int dir = tableViewerComparator.getDirection();
                tableViewer.getTable().setSortDirection(dir);
                tableViewer.getTable().setSortColumn(column);
                tableViewer.refresh();
            }
        };
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }
}
