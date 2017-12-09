package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.ui.commanding.cmdhist.CommandHistoryFilters.Filter;
import org.yamcs.studio.ui.commanding.cmdhist.CommandHistoryRecord.CommandState;

public class CommandHistoryView extends ViewPart {

    private static final Logger log = Logger.getLogger(CommandHistoryView.class.getName());

    public static final String COL_COMMAND = "Command";
    public static final String COL_SRC_ID = "Src.ID";
    public static final String COL_SRC = "Src";
    public static final String COL_SEQ_ID = "Seq.ID";
    public static final String COL_PTV = "PTV";
    public static final String COL_T = "T";

    public static final int MAX_WIDTH = 500;

    // Ignored for dynamic columns, most of these are actually considered fixed columns.
    private static final List<String> IGNORED_ATTRIBUTES = Arrays.asList("cmdName", "binary",
            CommandHistoryRecordContentProvider.ATTR_USERNAME,
            CommandHistoryRecordContentProvider.ATTR_SOURCE,
            CommandHistoryRecordContentProvider.ATTR_FINAL_SEQUENCE_COUNT,
            CommandHistoryRecordContentProvider.ATTR_TRANSMISSION_CONSTRAINTS,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_COMPLETE,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_FAILED);

    private LocalResourceManager resourceManager;
    private Image greenBubble;
    private Image redBubble;
    private Image grayBubble;
    private Image waitingImage;

    private Color palidGreen;
    private Color palidRed;

    private Composite parent;
    private TableViewer tableViewer;
    private CommandHistoryViewerComparator tableViewerComparator;

    // Store layouts for when a new tcl is set. Because TCLs trigger only once, and we need dynamic columns
    private Map<TableColumn, ColumnLayoutData> layoutDataByColumn = new HashMap<>();

    private CommandHistoryRecordContentProvider tableContentProvider;
    private Set<String> dynamicColumns = new HashSet<>();

    private static CommandHistoryView instance;

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        instance = this;
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/ok.png"));
        redBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/nok.png"));
        grayBubble = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/undef.png"));
        waitingImage = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/waiting.png"));

        palidGreen = resourceManager.createColor(new RGB(230, 255, 237));
        palidRed = resourceManager.createColor(new RGB(255, 238, 240));

        createActions();

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.MULTI);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        /**
         * Colorize row background based on completion status
         */
        tableViewer.getTable().addListener(SWT.EraseItem, event -> {
            if ((event.detail & SWT.SELECTED) == 1) {
                return; // Prefer selection color
            }

            Table table = (Table) event.widget;

            CommandHistoryRecord rec = (CommandHistoryRecord) event.item.getData();

            int clientWidth = table.getClientArea().width;

            GC gc = event.gc;
            Color oldBackground = gc.getBackground();

            if (rec.getCommandState() == CommandState.COMPLETED) {
                gc.setBackground(palidGreen);
            } else if (rec.getCommandState() == CommandState.FAILED) {
                gc.setBackground(palidRed);
            }
            gc.fillRectangle(0, event.y, clientWidth, event.height);
            gc.setBackground(oldBackground);
        });

        addFixedColumns();
        applyColumnLayoutData(tcl);

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
            RCPUtils.runCommand(CommandHistory.CMD_EVENT_PROPERTIES);
        });

        getViewSite().setSelectionProvider(tableViewer);

        CommandingCatalogue.getInstance().addCommandHistoryListener(cmdhistEntry -> {
            Display.getDefault().asyncExec(() -> processCommandHistoryEntry(cmdhistEntry));
        });
    }

    public static CommandHistoryView getInstance() {
        return instance;
    }

    public void clear() {
        tableContentProvider.clearAll();
    }

    public void enableScrollLock(boolean enabled) {
        tableContentProvider.enableScrollLock(enabled);
    }

    private void createActions() {
        IActionBars bars = getViewSite().getActionBars();
        IMenuManager mgr = bars.getMenuManager();

        Filter allColumnsFilter = new Filter("Full");
        allColumnsFilter.filterFields.add(Pattern.compile(".*"));
        Action allColumnsAction = new Action("Show all columns", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    applyFilter(allColumnsFilter);
                }
            }
        };
        allColumnsAction.setChecked(true);
        mgr.add(allColumnsAction);

        Filter keyColumnsFilter = new Filter("Brief");
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
                    applyFilter(keyColumnsFilter);
                }
            }
        });
    }

    private void addFixedColumns() {

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
        layoutDataByColumn.put(gentimeColumn.getColumn(), new ColumnPixelData(150));

        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.getColumn().setToolTipText("Command String");
        nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
        nameColumn.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public Image getImage(Object element) {
                switch (((CommandHistoryRecord) element).getCommandState()) {
                case COMPLETED:
                    return greenBubble;
                case FAILED:
                    return redBubble;
                default:
                    return grayBubble;
                }
            }

            @Override
            public String getText(Object element) {
                CommandHistoryRecord rec = (CommandHistoryRecord) element;
                return rec.getCommandString();
            }

            @Override
            public String getToolTipText(Object element) {
                CommandHistoryRecord rec = (CommandHistoryRecord) element;
                if (rec.getPTVInfo().getFailureMessage() != null)
                    return rec.getPTVInfo().getFailureMessage();
                else
                    return super.getToolTipText(element);
            }
        });
        layoutDataByColumn.put(nameColumn.getColumn(), new ColumnPixelData(500));

        TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        originColumn.getColumn().setText(COL_SRC);
        originColumn.getColumn().setToolTipText("Source (user@origin)");
        originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
        originColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandHistoryRecord rec = (CommandHistoryRecord) element;
                return rec.getUsername() + "@" + rec.getOrigin();
            }
        });
        layoutDataByColumn.put(originColumn.getColumn(), new ColumnPixelData(200));

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
        layoutDataByColumn.put(seqIdColumn.getColumn(), new ColumnPixelData(50));

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
                if (rec.getPTVInfo().getFailureMessage() != null)
                    return rec.getPTVInfo().getFailureMessage();
                else
                    return super.getToolTipText(element);
            }
        });
        layoutDataByColumn.put(ptvColumn.getColumn(), new ColumnPixelData(50));

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
        layoutDataByColumn.put(finalSeqColumn.getColumn(), new ColumnPixelData(50));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(gentimeColumn);
        columns.add(nameColumn);
        columns.add(originColumn);
        columns.add(seqIdColumn);
        columns.add(ptvColumn);
        columns.add(finalSeqColumn);
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

    private void applyColumnLayoutData(TableColumnLayout tcl) {
        layoutDataByColumn.forEach((k, v) -> tcl.setColumnData(k, v));
    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (IGNORED_ATTRIBUTES.contains(attr.getName()))
                continue;

            String shortName = CommandHistoryRecordContentProvider.toHumanReadableName(attr);
            if (!dynamicColumns.contains(shortName)) {
                TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
                column.getColumn().setText(shortName);
                column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));
                column.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        String text = ((CommandHistoryRecord) element).getTextForColumn(shortName);
                        return (text != null) ? text : null;
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        return ((CommandHistoryRecord) element).getTooltipForColumn(shortName);
                    }

                    @Override
                    public Image getImage(Object element) {
                        String imgLoc = ((CommandHistoryRecord) element).getImageForColumn(shortName);
                        if (CommandHistoryRecordContentProvider.GREEN.equals(imgLoc))
                            return greenBubble;
                        else if (CommandHistoryRecordContentProvider.RED.equals(imgLoc))
                            return redBubble;
                        else
                            return null;
                    }
                });
                dynamicColumns.add(shortName);
                layoutDataByColumn.put(column.getColumn(), new ColumnPixelData(90));
                TableColumnLayout tcl = new TableColumnLayout();
                parent.setLayout(tcl);
                applyColumnLayoutData(tcl);
                column.getColumn().setWidth(90);
                tableViewer.getTable().layout();

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
        }

        // Now add content
        tableContentProvider.processCommandHistoryEntry(cmdhistEntry);
    }

    private void checkMinWidth(TableColumn column) {
        if (column.getData("hidden") != null && !((boolean) column.getData("hidden"))) {
            if (column.getWidth() < 5)
                column.setWidth(5);
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

    public void applyFilter(Filter selectedFilter) {
        for (TableColumn column : tableViewer.getTable().getColumns()) {
            if (selectedFilter.matchFilter(column.getText())) {
                column.setData("hidden", false);
                column.pack();
                if (column.getWidth() > MAX_WIDTH)
                    column.setWidth(MAX_WIDTH);
            } else {
                column.setData("hidden", true);
                column.setWidth(0);
            }
        }

    }

    public TableViewer getTableViewer() {
        return this.tableViewer;
    }
}
