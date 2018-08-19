package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryRecord.CommandState;
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
            CommandHistoryRecordContentProvider.ATTR_COMMENT);

    private List<String> visibleColumns = new ArrayList<>();
    private List<String> hiddenColumns = new ArrayList<>();

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
    private CommandHistoryViewerComparator tableViewerComparator;

    private CommandHistoryRecordContentProvider tableContentProvider;
    private Set<String> dynamicColumns = new HashSet<>();

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

        createActions();

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);

        visibleColumns.add(COL_T);
        visibleColumns.add(COL_COMMAND);
        visibleColumns.add(COL_SRC);
        visibleColumns.add(COL_SRC_ID);
        visibleColumns.add(COL_PTV);
        visibleColumns.add(COL_SEQ_ID);
        createColumns(new TableColumn[0], new int[0]);

        tableContentProvider = new CommandHistoryRecordContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new CommandHistoryViewerComparator();
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

        updateSummaryLine();

        getViewSite().setSelectionProvider(tableViewer);

        CommandingCatalogue.getInstance().addCommandHistoryListener(cmdhistEntry -> {
            Display.getDefault().asyncExec(() -> processCommandHistoryEntry(cmdhistEntry));
        });
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
            }
        };
        mgr.add(configureColumnsAction);
    }

    private void createColumns(TableColumn[] currentColumns, int[] widths) {
        Table table = tableViewer.getTable();
        TableLayout layout = new TableLayout();

        if (currentColumns.length == 0) {
            TableViewerColumn stateColumn = new TableViewerColumn(tableViewer, SWT.NONE);
            stateColumn.getColumn().addSelectionListener(getSelectionAdapter(stateColumn.getColumn()));
            stateColumn.getColumn().setImage(headerCompleteImage);
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
        }
        layout.addColumnData(new ColumnPixelData(90));

        for (int i = 1; i < currentColumns.length; i++) {
            currentColumns[i].dispose();
        }

        for (String name : visibleColumns) {
            if (name.equals(COL_T)) {
                TableViewerColumn gentimeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                gentimeColumn.getColumn().addSelectionListener(getSelectionAdapter(gentimeColumn.getColumn()));
                gentimeColumn.getColumn().setText(COL_T);
                gentimeColumn.getColumn().setToolTipText("Generation Time");
                gentimeColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CommandHistoryRecord) element).getGenerationTime();
                    }
                });
                layout.addColumnData(new ColumnPixelData(150));

                // Add chevron
                table.setSortColumn(gentimeColumn.getColumn());
                table.setSortDirection(SWT.DOWN);
            } else if (name.equals(COL_COMMAND)) {
                TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
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
                layout.addColumnData(new ColumnPixelData(500));
            } else if (name.equals(COL_SRC)) {
                TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.getColumn().setText(COL_SRC);
                originColumn.getColumn().setToolTipText("Source (user@origin)");
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        CommandHistoryRecord rec = (CommandHistoryRecord) element;
                        if (rec.getOrigin() != null && !"".equals(rec.getOrigin())) {
                            return rec.getUsername() + "@" + rec.getOrigin();
                        } else {
                            return rec.getUsername();
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(200));
            } else if (name.equals(COL_SRC_ID)) {
                TableViewerColumn seqIdColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setText(COL_SRC_ID);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setToolTipText("Client ID");
                seqIdColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return String.valueOf(((CommandHistoryRecord) element).getSequenceNumber());
                    }
                });
                layout.addColumnData(new ColumnPixelData(50));
            } else if (name.equals(COL_PTV)) {
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
                layout.addColumnData(new ColumnPixelData(50));
            } else if (name.equals(COL_SEQ_ID)) {
                TableViewerColumn finalSeqColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                finalSeqColumn.getColumn().setText(COL_SEQ_ID);
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
                layout.addColumnData(new ColumnPixelData(50));
            } else if (dynamicColumns.contains(name)) {
                TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
                column.getColumn().setText(name);
                column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));
                column.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CommandHistoryRecord) element).getTextForColumn(name, showRelativeTime);
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        return ((CommandHistoryRecord) element).getTooltipForColumn(name);
                    }

                    @Override
                    public Image getImage(Object element) {
                        String imgLoc = ((CommandHistoryRecord) element).getImageForColumn(name);
                        if (CommandHistoryRecordContentProvider.GREEN.equals(imgLoc)) {
                            return greenBubble;
                        } else if (CommandHistoryRecordContentProvider.RED.equals(imgLoc)) {
                            return redBubble;
                        } else {
                            return null;
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(90));
            }
        }

        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.layout(true); // !! Ensures column widths are applied when recreating columns
        tableViewer.refresh(); // !! Ensures table renders correctly for old data when adding a new column
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (!IGNORED_ATTRIBUTES.contains(attr.getName())) {
                String shortName = CommandHistoryRecordContentProvider.toHumanReadableName(attr);

                if (!dynamicColumns.contains(shortName)) {
                    dynamicColumns.add(shortName);
                    visibleColumns.add(shortName);
                    createColumns(tableViewer.getTable().getColumns(), new int[0]);
                }
            }
        }

        // Now add content
        tableContentProvider.processCommandHistoryEntry(cmdhistEntry);
        updateSummaryLine();
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

    public TableViewer getTableViewer() {
        return tableViewer;
    }
}
