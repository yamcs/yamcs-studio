package org.yamcs.studio.core.commanding.cmdstack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandHistoryEntry;
import org.yamcs.protobuf.Rest.RestDumpArchiveRequest;
import org.yamcs.protobuf.Rest.RestDumpArchiveResponse;
import org.yamcs.protobuf.Yamcs.CommandHistoryReplayRequest;
import org.yamcs.studio.core.CommandHistoryListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.commanding.cmdhist.CommandHistoryRecord;
import org.yamcs.studio.core.commanding.cmdhist.CommandHistoryRecordContentProvider;
import org.yamcs.studio.core.commanding.cmdhist.CommandHistoryViewerComparator;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

/**
 * TODO show a friendly message when the thing is still loading
 */
public class CommandStackView extends ViewPart {

    private static final Logger log = Logger.getLogger(CommandStackView.class.getName());

    public static final String COL_COMMAND = "Command";
    public static final String COL_SRC_ID = "Src.ID";
    public static final String COL_SRC_HOST = "Src.Host";
    public static final String COL_USER = "User";
    public static final String COL_SEQ_ID = "Seq.ID";
    public static final String COL_T = "T";

    private WebSocketRegistrar webSocketClient = YamcsPlugin.getDefault().getWebSocketClient();
    private RestClient restClient = YamcsPlugin.getDefault().getRestClient();

    // Prefix used in command attribute names
    private static final String ACK_PREFIX = "Acknowledge_";

    // Ignored for dynamic columns, most of these are actually considered fixed columns.
    private static final List<String> IGNORED_ATTRIBUTES = Arrays.asList("cmdName", "binary", "username", "source", "Final_Sequence_Count");

    private LocalResourceManager resourceManager;
    private Image greenBubble;
    private Image redBubble;

    private Composite parent;
    private TableViewer tableViewer;
    private CommandHistoryViewerComparator tableViewerComparator;

    // Store layouts for when a new tcl is set. Because TCLs trigger only once, and we need dynamic columns
    private Map<TableColumn, ColumnLayoutData> layoutDataByColumn = new HashMap<>();

    private CommandHistoryRecordContentProvider tableContentProvider;
    private Set<String> dynamicColumns = new HashSet<>();

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        // Load images
        Bundle bundle = FrameworkUtil.getBundle(CommandStackView.class);
        ImageDescriptor desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/ok.png"), null));
        greenBubble = resourceManager.createImage(desc);
        desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/nok.png"), null));
        redBubble = resourceManager.createImage(desc);

        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);

        tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);

        addFixedColumns();
        applyColumnLayoutData(tcl);

        tableContentProvider = new CommandHistoryRecordContentProvider(tableViewer);
        tableViewer.setContentProvider(tableContentProvider);
        tableViewer.setInput(tableContentProvider); // ! otherwise refresh() deletes everything...

        tableViewerComparator = new CommandHistoryViewerComparator();
        tableViewer.setComparator(tableViewerComparator);

        subscribeToUpdates();
        fetchArchivedCommands();
    }

    private void addFixedColumns() {
        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText(COL_COMMAND);
        nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CommandHistoryRecord) element).getSource();
            }
        });
        layoutDataByColumn.put(nameColumn.getColumn(), new ColumnWeightData(200));

        TableViewerColumn seqIdColumn = new TableViewerColumn(tableViewer, SWT.NONE);
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

        TableViewerColumn originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        originColumn.getColumn().setText(COL_SRC_HOST);
        originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
        originColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CommandHistoryRecord) element).getOrigin();
            }
        });
        layoutDataByColumn.put(originColumn.getColumn(), new ColumnWeightData(70));

        TableViewerColumn userColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        userColumn.getColumn().setText(COL_USER);
        userColumn.getColumn().addSelectionListener(getSelectionAdapter(userColumn.getColumn()));
        userColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CommandHistoryRecord) element).getUsername();
            }
        });
        layoutDataByColumn.put(userColumn.getColumn(), new ColumnWeightData(70));

        TableViewerColumn finalSeqColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        finalSeqColumn.getColumn().setText(COL_SEQ_ID);
        finalSeqColumn.getColumn().addSelectionListener(getSelectionAdapter(finalSeqColumn.getColumn()));
        finalSeqColumn.getColumn().setToolTipText("Final Sequence Count");
        finalSeqColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return String.valueOf(((CommandHistoryRecord) element).getFinalSequenceCount());
            }
        });
        layoutDataByColumn.put(finalSeqColumn.getColumn(), new ColumnPixelData(50));

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

        // TODO use IMemento or something
        tableViewer.getTable().setSortColumn(gentimeColumn.getColumn());
        tableViewer.getTable().setSortDirection(SWT.DOWN);
    }

    private void applyColumnLayoutData(TableColumnLayout tcl) {
        layoutDataByColumn.forEach((k, v) -> tcl.setColumnData(k, v));
    }

    private void subscribeToUpdates() {
        webSocketClient.addCommandHistoryListener(new CommandHistoryListener() {
            @Override
            public void signalYamcsDisconnected() {
            }

            @Override
            public void signalYamcsConnected() {
            }

            @Override
            public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
                Display.getDefault().asyncExec(() -> CommandStackView.this.processCommandHistoryEntry(cmdhistEntry));
            }
        });
    }

    private void fetchArchivedCommands() {
        // TODO limit to 'some' time in the past
        RestDumpArchiveRequest request = RestDumpArchiveRequest.newBuilder().setCommandHistoryRequest(CommandHistoryReplayRequest.newBuilder())
                .build();
        restClient.dumpArchive(request, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                RestDumpArchiveResponse response = (RestDumpArchiveResponse) responseMsg;
                Display.getDefault().asyncExec(() -> {
                    for (CommandHistoryEntry cmdhistEntry : response.getCommandList())
                        CommandStackView.this.processCommandHistoryEntry(cmdhistEntry);
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Error while fetching archived telecommands", e);
            }
        });
    }

    private void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
        // Maybe we need to update structure
        for (CommandHistoryAttribute attr : cmdhistEntry.getAttrList()) {
            if (IGNORED_ATTRIBUTES.contains(attr.getName()))
                continue;

            String shortName = attr.getName()
                    .replace(ACK_PREFIX, "")
                    .replace(CommandHistoryRecord.STATUS_SUFFIX, "")
                    .replace(CommandHistoryRecord.TIME_SUFFIX, "");
            if (!dynamicColumns.contains(shortName)) {
                TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
                column.getColumn().setText(shortName);
                column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));
                column.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        return ((CommandHistoryRecord) element).getTextForColumn(shortName);
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
            }
        }

        // Now add content
        tableContentProvider.processCommandHistoryEntry(cmdhistEntry);
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
    }

    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }
}
