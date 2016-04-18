package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.ui.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.ui.commanding.cmdhist.CommandHistoryFilters.Filter;

import com.google.protobuf.MessageLite;

public class CommandHistoryView extends ViewPart {

    private static final Logger log = Logger.getLogger(CommandHistoryView.class.getName());

    public static final String COL_COMMAND = "Command";
    public static final String COL_SRC_ID = "Src.ID";
    public static final String COL_SRC = "Src";
    public static final String COL_SEQ_ID = "Seq.ID";
    public static final String COL_PTV = "PTV";
    public static final String COL_T = "T";

    public static final int MAX_WIDTH = 500;

    // Prefix used in command attribute names
    private static final String ACK_PREFIX = "Acknowledge_";

    // Ignored for dynamic columns, most of these are actually considered fixed columns.
    private static final List<String> IGNORED_ATTRIBUTES = Arrays.asList("cmdName", "binary",
            CommandHistoryRecordContentProvider.ATTR_USERNAME,
            CommandHistoryRecordContentProvider.ATTR_SOURCE,
            CommandHistoryRecordContentProvider.ATTR_FINAL_SEQUENCE_COUNT,
            CommandHistoryRecordContentProvider.ATTR_TRANSMISSION_CONSTRAINTS,
            CommandHistoryRecordContentProvider.ATTR_COMMAND_FAILED);

    private LocalResourceManager resourceManager;
    private Image greenBubble;
    private Image redBubble;
    private Image grayBubble;
    private Image waitingImage;

    private Composite parent;
    private TableViewer tableViewer;
    private CommandHistoryViewerComparator tableViewerComparator;

    // Store layouts for when a new tcl is set. Because TCLs trigger only once, and we need dynamic columns
    private Map<TableColumn, ColumnLayoutData> layoutDataByColumn = new HashMap<>();

    private CommandHistoryRecordContentProvider tableContentProvider;
    private Set<String> dynamicColumns = new HashSet<>();

    private static CommandHistoryView instance;
    private List<CommandHistoryRecord> copyedCommandHistoryRecords = new ArrayList<>();

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        instance = this;
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager.createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/ok.png"));
        redBubble = resourceManager.createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/nok.png"));
        grayBubble = resourceManager.createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/undef.png"));
        waitingImage = resourceManager.createImage(RCPUtils.getImageDescriptor(CommandHistoryView.class, "icons/obj16/waiting.png"));

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.MULTI);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        addFixedColumns();
        applyColumnLayoutData(tcl);
        addPopupMenu();

        tableContentProvider = new CommandHistoryRecordContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new CommandHistoryViewerComparator();
        tableViewer.setComparator(tableViewerComparator);

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
        nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CommandHistoryRecord) element).getSource();
            }
        });
        layoutDataByColumn.put(nameColumn.getColumn(), new ColumnPixelData(500));

        TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        originColumn.getColumn().setText(COL_SRC);
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

    private void addPopupMenu() {
        // add popup menu
        Table table = tableViewer.getTable();
        Shell shell = table.getShell();
        Menu contextMenu = new Menu(tableViewer.getTable());
        table.setMenu(contextMenu);
        MenuItem mItem1 = new MenuItem(contextMenu, SWT.None);
        mItem1.setText("Add a Comment...");
        mItem1.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                CommandHistoryRecord chr = (CommandHistoryRecord) (tableViewer.getTable().getSelection()[0].getData());
                if (chr == null)
                    return;
                TableItem[] selection = tableViewer.getTable().getSelection();

                String cmdComment = "";
                if (selection.length == 1)
                    cmdComment = chr.getTextForColumn("Comment");
                InputDialog commentDialog = new InputDialog(tableViewer.getTable().getShell(), "Add Comment", "Add a comment for the command",
                        cmdComment,
                        new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        return null;
                    }
                }) {
                    @Override
                    protected int getInputTextStyle() {
                        return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
                    }

                    @Override
                    protected Control createDialogArea(Composite parent) {
                        Control res = super.createDialogArea(parent);
                        ((GridData) this.getText().getLayoutData()).heightHint = 4 * this.getText().getLineHeight();
                        return res;
                    }
                };
                int commentResult = commentDialog.open();
                if (commentResult == Window.OK) {
                    String newComment = commentDialog.getValue();
                    CommandingCatalogue catalogue = CommandingCatalogue.getInstance();

                    for (TableItem ti : selection) {

                        CommandHistoryRecord chri = (CommandHistoryRecord) ti.getData();
                        if (chri == null)
                            continue;

                        catalogue.updateCommandComment("realtime", chri.getCommandId(), newComment, new ResponseHandler() {

                            @Override
                            public void onMessage(MessageLite responseMsg) {
                            }

                            @Override
                            public void onException(Exception e) {
                                table.getDisplay().asyncExec(() -> {
                                    MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                                    dialog.setText("Comment Update");
                                    dialog.setMessage("Comment has not been updated. Details: " + e.getMessage());
                                    // open dialog and await user selection
                                    dialog.open();
                                });
                            }
                        });
                    }

                }
            }
        });

        MenuItem mItemCopySrc = new MenuItem(contextMenu, SWT.None);
        mItemCopySrc.setText("Copy");
        mItemCopySrc.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                // check something is selected
                TableItem[] selection = tableViewer.getTable().getSelection();
                if (selection == null || selection.length == 0)
                    return;

                // clear previous data
                copyedCommandHistoryRecords.clear();
                String source = "";

                // copy each selected items
                for (TableItem ti : selection) {
                    CommandHistoryRecord chr = (CommandHistoryRecord) (ti.getData());
                    if (chr == null)
                        continue;

                    copyedCommandHistoryRecords.add(chr);
                    source += chr.getSource() + "\n";
                }

                final Clipboard cb = new Clipboard(tableViewer.getTable().getDisplay());
                TextTransfer textTransfer = TextTransfer.getInstance();
                cb.setContents(new Object[] { source }, new Transfer[] { textTransfer });

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }

        });

        tableViewer.getTable().addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                TableItem[] selection = tableViewer.getTable().getSelection();
                if (selection.length != 0 && (event.button == 3)) {
                    contextMenu.setVisible(true);
                } else {
                    contextMenu.setVisible(false);
                }

            }

        });

    }

    public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (IGNORED_ATTRIBUTES.contains(attr.getName()))
                continue;

            String shortName = attr.getName()
                    .replace(ACK_PREFIX, "")
                    .replace(CommandHistoryRecord.STATUS_SUFFIX, "")
                    .replace(CommandHistoryRecord.TIME_SUFFIX, "");
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

    public List<CommandHistoryRecord> getCopyedCommandHistoryRecords() {
        return copyedCommandHistoryRecords;
    }
}
