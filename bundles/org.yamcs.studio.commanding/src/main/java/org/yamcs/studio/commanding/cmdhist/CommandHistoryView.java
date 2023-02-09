package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryFilters.Filter;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryRecord.CommandState;
import org.yamcs.studio.core.model.CommandHistoryListener;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class CommandHistoryView extends ViewPart {

    private static final Logger log = Logger.getLogger(CommandHistoryView.class.getName());

    public static final String COL_COMMAND = "Command";
    public static final String COL_SRC_ID = "Src.ID";
    public static final String COL_SRC = "Src";
    public static final String COL_SEQ_ID = "Seq.ID";
    public static final String COL_PTV = "PTV";
    public static final String COL_T = "T";

    public static final int MAX_WIDTH = 500;

    // Ignored for dynamic columns, most of these are actually considered fixed
    // columns.
    private static final List<String> IGNORED_ATTRIBUTES = Arrays.asList("cmdName",
            CommandHistoryRecordContentProvider.ATTR_BINARY, CommandHistoryRecordContentProvider.ATTR_USERNAME,
            CommandHistoryRecordContentProvider.ATTR_SOURCE,
            CommandHistoryRecordContentProvider.ATTR_FINAL_SEQUENCE_COUNT,
            CommandHistoryRecordContentProvider.ATTR_TRANSMISSION_CONSTRAINTS,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_COMPLETE,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_FAILED);

    private LocalResourceManager resourceManager;
    Image greenBubble;
    Image redBubble;
    Image grayBubble;
    private Image waitingImage;
    private Image headerCompleteImage;
    Image checkmarkImage;
    Image errorImage;
    Image prevImage;
    Image nextImage;

    private TableViewer tableViewer;
    private TableLayout tableLayout;
    private CommandHistoryViewerComparator tableViewerComparator;

    private CommandHistoryListener commandHistoryListener;
    private CommandHistoryRecordContentProvider tableContentProvider;
    private Set<String> dynamicColumns = new HashSet<>();

    private boolean showRelativeTime = true;

    private Filter allColumnsFilter;
    private Filter keyColumnsFilter;
    private Filter activeColumnsFilter;

    @Override
    public void createPartControl(Composite parent) {

        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/ok.png"));
        redBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/nok.png"));
        grayBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/undef.png"));
        waitingImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/waiting.png"));
        headerCompleteImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/header_complete.png"));
        checkmarkImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/checkmark.gif"));
        errorImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/error_tsk.png"));
        prevImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/event_prev.png"));
        nextImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/event_next.png"));

        createActions();

        tableLayout = new TableLayout();

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        tableViewer.getTable().setLayout(tableLayout);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        createColumns();

        tableContentProvider = new CommandHistoryRecordContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new CommandHistoryViewerComparator();
        tableViewer.setComparator(tableViewerComparator);

        // Register context menu. Commands are added in plugin.xml
        MenuManager menuManager = new MenuManager();
        Menu menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewer);

        // Default action is to open Command properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(CommandHistory.CMD_COMMAND_PROPERTIES);
        });

        updateSummaryLine();

        getViewSite().setSelectionProvider(tableViewer);

        commandHistoryListener = cmdhistEntry -> {
            Display.getDefault().asyncExec(() -> processCommandHistoryEntry(cmdhistEntry));
        };
        CommandingCatalogue.getInstance().addCommandHistoryListener(commandHistoryListener);
    }

    private void updateSummaryLine() {
        String yamcsInstance = ManagementCatalogue.getCurrentYamcsInstance();
        String summaryLine = "";
        if (yamcsInstance != null) {
            summaryLine = "Showing commands for Yamcs instance " + yamcsInstance + ". ";
        }
        setContentDescription(summaryLine + String.format("%d completed, %d failed, %d others (no filter)",
                tableContentProvider.getCommandCount(CommandState.COMPLETED),
                tableContentProvider.getCommandCount(CommandState.FAILED),
                tableContentProvider.getCommandCount(CommandState.UNKNOWN)));
    }

    public void clear() {
        tableContentProvider.clearAll();
        updateSummaryLine();
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

    private void createActions() {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();

        allColumnsFilter = new Filter("Full");
        allColumnsFilter.filterFields.add(Pattern.compile(".*"));
        Action allColumnsAction = new Action("Show all columns", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    activeColumnsFilter = allColumnsFilter;
                    createColumns();
                }
            }
        };
        allColumnsAction.setChecked(true);
        mgr.add(allColumnsAction);

        keyColumnsFilter = new Filter("Brief");
        keyColumnsFilter.filterFields.add(Pattern.compile("^Command$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^PTV$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^Seq.ID$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^FRC$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^DASS$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^MCS$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^[A-Z]$"));
        keyColumnsFilter.filterFields.add(Pattern.compile("^Comment$"));
        mgr.add(new Action("Show key columns", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    activeColumnsFilter = keyColumnsFilter;
                    createColumns();
                }
            }
        });

        activeColumnsFilter = allColumnsFilter;

        mgr.add(new Separator());

        Action showRelativeTimeAction = new Action("Show relative time", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                showRelativeTime = isChecked();
                tableViewer.refresh();
            }
        };
        showRelativeTimeAction.setChecked(showRelativeTime);
        mgr.add(showRelativeTimeAction);
    }

    public void processCommandHistoryEntries(List<CommandHistoryEntry> cmdhistEntries) {
        if (tableViewer.getTable().isDisposed()) {
            return;
        }

        for (CommandHistoryEntry cmdhistEntry : cmdhistEntries) {
            maybeUpdateColumns(cmdhistEntry);
        }
        tableContentProvider.processCommandHistoryEntries(cmdhistEntries);
        updateSummaryLine();
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        maybeUpdateColumns(cmdhistEntry);

        // Now add content
        tableContentProvider.processCommandHistoryEntry(cmdhistEntry);
        updateSummaryLine();
    }

    private void maybeUpdateColumns(CommandHistoryEntry cmdhistEntry) {
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (IGNORED_ATTRIBUTES.contains(attr.getName())) {
                continue;
            }

            String shortName = CommandHistoryRecordContentProvider.toHumanReadableName(attr);
            if (!dynamicColumns.contains(shortName)) {
                createDynamicColumn(shortName);
            }
        }
    }

    private void checkMinWidth(TableColumn column) {
        if (column.getData("hidden") != null && !((boolean) column.getData("hidden"))) {
            if (column.getWidth() < 5) {
                column.setWidth(5);
            }
        }
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

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }

    private void createColumns() {
        tableViewer.getTable().setRedraw(false);
        for (TableColumn column : tableViewer.getTable().getColumns()) {
            column.dispose();
        }

        createFixedColumns();
        for (String columnName : dynamicColumns) {
            createDynamicColumn(columnName);
        }
        tableViewer.getTable().setRedraw(true);
        tableViewer.refresh();
    }

    private void createFixedColumns() {
        List<TableViewerColumn> columns = new ArrayList<>();

        if (activeColumnsFilter != keyColumnsFilter) {
            TableViewerColumn stateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            stateColumn.getColumn().setImage(headerCompleteImage);
            stateColumn.getColumn().addSelectionListener(getSelectionAdapter(stateColumn.getColumn()));
            stateColumn.getColumn().setToolTipText("Completion");
            stateColumn.setLabelProvider(new ColumnLabelProvider() {
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
            tableLayout.addColumnData(new ColumnPixelData(90));
            stateColumn.getColumn().setWidth(90);
        }

        TableViewerColumn gentimeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        gentimeColumn.getColumn().setText(COL_T);
        gentimeColumn.getColumn().addSelectionListener(getSelectionAdapter(gentimeColumn.getColumn()));
        gentimeColumn.getColumn().setToolTipText("Generation Time");
        gentimeColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CommandHistoryRecord) element).getGenerationTime();
            }
        });
        tableLayout.addColumnData(new ColumnPixelData(150));
        gentimeColumn.getColumn().setWidth(150);
        columns.add(gentimeColumn);

        if (activeColumnsFilter.matchFilter(COL_COMMAND)) {
            TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            nameColumn.getColumn().setText(COL_COMMAND);
            nameColumn.getColumn().setToolTipText("Command String");
            nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
            nameColumn.setLabelProvider(new ColumnLabelProvider() {

                @Override
                public String getText(Object element) {
                    CommandHistoryRecord rec = (CommandHistoryRecord) element;
                    return rec.getCommandString();
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(500));
            nameColumn.getColumn().setWidth(500);
            columns.add(nameColumn);
        }

        if (activeColumnsFilter.matchFilter(COL_SRC)) {
            TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            originColumn.getColumn().setText(COL_SRC);
            originColumn.getColumn().setToolTipText("Source (user@origin)");
            originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
            originColumn.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    CommandHistoryRecord rec = (CommandHistoryRecord) element;
                    if (rec.getOrigin() != null) {
                        return rec.getUsername() + "@" + rec.getOrigin();
                    } else {
                        return rec.getUsername();
                    }
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(200));
            originColumn.getColumn().setWidth(200);
            columns.add(originColumn);
        }

        if (activeColumnsFilter.matchFilter(COL_SRC_ID)) {
            TableViewerColumn seqIdColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
            seqIdColumn.getColumn().setText(COL_SRC_ID);
            seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
            seqIdColumn.getColumn().setToolTipText("Client ID");
            seqIdColumn.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return String.valueOf(((CommandHistoryRecord) element).getSequenceNumber());
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(50));
            seqIdColumn.getColumn().setWidth(50);
            columns.add(seqIdColumn);
        }

        if (activeColumnsFilter.matchFilter(COL_PTV)) {
            TableViewerColumn ptvColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
            ptvColumn.getColumn().setText(COL_PTV);
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
            tableLayout.addColumnData(new ColumnPixelData(50));
            ptvColumn.getColumn().setWidth(50);
            columns.add(ptvColumn);
        }

        if (activeColumnsFilter.matchFilter(COL_SEQ_ID)) {
            TableViewerColumn finalSeqColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
            finalSeqColumn.getColumn().setText(COL_SEQ_ID);
            finalSeqColumn.getColumn().addSelectionListener(getSelectionAdapter(finalSeqColumn.getColumn()));
            finalSeqColumn.getColumn().setToolTipText("Final Sequence Count");
            finalSeqColumn.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    CommandHistoryRecord rec = (CommandHistoryRecord) element;
                    return (rec.getFinalSequenceCount() != null) ? String.valueOf(rec.getFinalSequenceCount()) : "-";
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(50));
            finalSeqColumn.getColumn().setWidth(50);
            columns.add(finalSeqColumn);
        }

        for (TableViewerColumn column : columns) {
            // prevent resize to 0
            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    checkMinWidth(column.getColumn());
                }
            });
        }

        // TODO use IMemento or something
        tableViewer.getTable().setSortColumn(gentimeColumn.getColumn());
        tableViewer.getTable().setSortDirection(SWT.DOWN);
        getViewSite().setSelectionProvider(tableViewer);
    }

    private void createDynamicColumn(String columnName) {
        if (activeColumnsFilter.matchFilter(columnName)) {
            TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
            column.getColumn().setText(columnName);
            column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));
            column.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((CommandHistoryRecord) element).getTextForColumn(columnName, showRelativeTime);
                }

                @Override
                public String getToolTipText(Object element) {
                    return ((CommandHistoryRecord) element).getTooltipForColumn(columnName);
                }

                @Override
                public Image getImage(Object element) {
                    String imgLoc = ((CommandHistoryRecord) element).getImageForColumn(columnName);
                    if (CommandHistoryRecordContentProvider.GREEN.equals(imgLoc)) {
                        return greenBubble;
                    } else if (CommandHistoryRecordContentProvider.RED.equals(imgLoc)) {
                        return redBubble;
                    } else {
                        return null;
                    }
                }
            });
            tableLayout.addColumnData(new ColumnPixelData(90));
            column.getColumn().setWidth(90);

            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    checkMinWidth(column.getColumn());
                }
            });
        }
        dynamicColumns.add(columnName);
    }

    public TableViewer getTableViewer() {
        return this.tableViewer;
    }

    @Override
    public void dispose() {
        super.dispose();
        CommandingCatalogue.getInstance().removeCommandHistoryListener(commandHistoryListener);
    }
}
