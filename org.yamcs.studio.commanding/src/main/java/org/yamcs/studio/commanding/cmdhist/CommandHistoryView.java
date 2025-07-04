/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.client.Acknowledgment;
import org.yamcs.client.Command;
import org.yamcs.protobuf.SubscribeCommandsRequest;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.utils.CenteredImageLabelProvider;
import org.yamcs.studio.core.utils.ColumnData;
import org.yamcs.studio.core.utils.RCPUtils;
import org.yamcs.studio.core.utils.ViewerColumnsDialog;
import org.yamcs.studio.data.yamcs.StringConverter;

public class CommandHistoryView extends ViewPart implements YamcsAware {

    private static final Logger log = Logger.getLogger(CommandHistoryView.class.getName());
    public static final String ID = "org.yamcs.studio.commanding.cmdhist.CommandHistoryView";
    public static final String COL_COMPLETION = "";
    public static final String COL_COMMAND = "Command";
    public static final String COL_ORIGIN_ID = "Ori.ID";
    public static final String COL_USER = "User";
    public static final String COL_ORIGIN = "Origin";
    public static final String COL_PTV = "PTV";
    public static final String COL_T = "T";
    public static final String COL_QUEUED = "Q";
    public static final String COL_RELEASED = "R";
    public static final String COL_SENT = "S";

    private static final int DYNAMIC_COLUMN_WIDTH = 90;

    private PatchedCommandSubscription subscription;

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
        var plugin = CommandingPlugin.getDefault();
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        greenBubble = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/ok.png"));
        redBubble = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/nok.png"));
        grayBubble = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/undef.png"));
        waitingImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/waiting.png"));
        headerCompleteImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/header_complete.png"));
        checkmarkImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/checkmark.gif"));
        errorImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/error_tsk.png"));
        prevImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/event_prev.png"));
        nextImage = resourceManager.create(plugin.getImageDescriptor("/icons/obj16/event_next.png"));

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
        var menuManager = new MenuManager();
        var menu = menuManager.createContextMenu(tableViewer.getTable());
        tableViewer.getTable().setMenu(menu);
        getSite().registerContextMenu(menuManager, tableViewer);

        // Default action is to open Command properties
        tableViewer.getTable().addListener(SWT.MouseDoubleClick, evt -> {
            RCPUtils.runCommand(CommandHistory.CMD_COMMAND_PROPERTIES);
        });

        getViewSite().setSelectionProvider(tableViewer);

        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        if (subscription != null) {
            subscription.cancel(true);
        }
        Display.getDefault().syncExec(this::clear);

        if (instance != null) {
            Display.getDefault().asyncExec(this::fetchLatestCommands);
        }
        if (processor != null) {
            var client = YamcsPlugin.getYamcsClient();
            subscription = new PatchedCommandSubscription(client.getMethodHandler());
            subscription.addListener(command -> Display.getDefault().asyncExec(() -> {
                processCommand(command, true);
            }));

            subscription.sendMessage(SubscribeCommandsRequest.newBuilder()
                    .setInstance(instance)
                    .setProcessor(processor)
                    .build());
        }
    }

    public ColumnData createDefaultColumnData() {
        var data = new ColumnData();
        data.addColumn(COL_COMPLETION, 90);
        data.addColumn(COL_T, 160);
        data.addColumn(COL_COMMAND, 500);
        data.addColumn(COL_USER, 100);
        data.addColumn(COL_ORIGIN, 200, false, true, true);
        data.addColumn(COL_ORIGIN_ID, 50, false, true, true);
        data.addColumn(COL_QUEUED, 50);
        data.addColumn(COL_RELEASED, 50);
        data.addColumn(COL_SENT, 50);

        for (var dynamicColumn : dynamicColumns) {
            // Temporary. This column was erroneously added in past versions of studio.
            if (dynamicColumn.equals("Acknowledge_Released")) {
                continue;
            }
            var def = columnData.getColumn(dynamicColumn);
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
        showRelativeTime = enabled;
    }

    public CommandHistoryRecord getPreviousRecord(CommandHistoryRecord rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            var indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] > 0) {
                var prevIndex = indices[0] - 1;
                return (CommandHistoryRecord) tableViewer.getElementAt(prevIndex);
            }
        }
        return null;
    }

    public CommandHistoryRecord getNextRecord(CommandHistoryRecord rec) {
        if (tableViewer.getTable().getSelectionCount() > 0) {
            var indices = tableViewer.getTable().getSelectionIndices();
            if (indices[0] < tableViewer.getTable().getItemCount() - 1) {
                var nextIndex = indices[0] + 1;
                return (CommandHistoryRecord) tableViewer.getElementAt(nextIndex);
            }
        }
        return null;
    }

    private void createActions(Shell shell) {
        var bars = getViewSite().getActionBars();
        var mgr = bars.getMenuManager();

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
        if (subscription != null) {
            subscription.cancel(true);
        }
        super.dispose();
    }

    private void syncCurrentWidthsToModel() {
        for (var column : tableViewer.getTable().getColumns()) {
            var def = columnData.getColumn(column.getText());
            if (def != null) {
                def.width = column.getWidth();
            }
        }
    }

    private void createColumns() {
        var table = tableViewer.getTable();
        var layout = new TableLayout();

        var currentColumns = tableViewer.getTable().getColumns();
        for (var currentColumn : currentColumns) {
            currentColumn.dispose();
        }

        for (var def : columnData.getVisibleColumns()) {
            if (def.name.equals(COL_COMPLETION)) {
                var completionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                completionColumn.getColumn().addControlListener(columnResizeListener);
                completionColumn.getColumn().addSelectionListener(getSelectionAdapter(completionColumn.getColumn()));
                completionColumn.getColumn().setImage(headerCompleteImage);
                completionColumn.getColumn().setToolTipText("Completion");
                completionColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        var command = ((CommandHistoryRecord) element).getCommand();
                        if (command.isSuccess()) {
                            return checkmarkImage;
                        } else if (command.isFailure()) {
                            return errorImage;
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public String getText(Object element) {
                        var command = ((CommandHistoryRecord) element).getCommand();
                        if (command.isSuccess()) {
                            return "Completed";
                        } else if (command.isFailure()) {
                            return command.getError();
                        } else {
                            return null;
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_T)) {
                var gentimeColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                gentimeColumn.getColumn().addControlListener(columnResizeListener);
                gentimeColumn.getColumn().addSelectionListener(getSelectionAdapter(gentimeColumn.getColumn()));
                gentimeColumn.getColumn().setText(COL_T);
                gentimeColumn.getColumn().setToolTipText("Generation Time");
                gentimeColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        var generationTime = ((CommandHistoryRecord) element).getCommand().getGenerationTime();
                        return YamcsPlugin.getDefault().formatInstant(generationTime);
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));

                // Add chevron
                table.setSortColumn(gentimeColumn.getColumn());
                table.setSortDirection(SWT.UP);
            } else if (def.name.equals(COL_COMMAND)) {
                var nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
                nameColumn.getColumn().setText(COL_COMMAND);
                nameColumn.getColumn().setToolTipText("Command String");
                nameColumn.getColumn().addControlListener(columnResizeListener);
                nameColumn.getColumn().addSelectionListener(getSelectionAdapter(nameColumn.getColumn()));
                nameColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        return rec.getSource();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_USER)) {
                var userColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                userColumn.getColumn().addSelectionListener(getSelectionAdapter(userColumn.getColumn()));
                userColumn.getColumn().setText(COL_USER);
                userColumn.getColumn().setToolTipText("User that issued the command");
                userColumn.getColumn().addControlListener(columnResizeListener);
                userColumn.getColumn().addSelectionListener(getSelectionAdapter(userColumn.getColumn()));
                userColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        return rec.getCommand().getUsername();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_ORIGIN)) {
                var originColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.getColumn().setText(COL_ORIGIN);
                originColumn.getColumn().setToolTipText("Origin");
                originColumn.getColumn().addControlListener(columnResizeListener);
                originColumn.getColumn().addSelectionListener(getSelectionAdapter(originColumn.getColumn()));
                originColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        return rec.getCommand().getOrigin();
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_ORIGIN_ID)) {
                var seqIdColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setText(COL_ORIGIN_ID);
                seqIdColumn.getColumn().addControlListener(columnResizeListener);
                seqIdColumn.getColumn().addSelectionListener(getSelectionAdapter(seqIdColumn.getColumn()));
                seqIdColumn.getColumn().setToolTipText("Client ID at origin");
                seqIdColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        return String.valueOf(rec.getCommand().getSequenceNumber());
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_PTV)) {
                var ptvColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                ptvColumn.getColumn().setText(COL_PTV);
                ptvColumn.getColumn().addControlListener(columnResizeListener);
                ptvColumn.getColumn().addSelectionListener(getSelectionAdapter(ptvColumn.getColumn()));
                ptvColumn.getColumn().setToolTipText("Pre-Transmission Verification");
                ptvColumn.setLabelProvider(new CenteredImageLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var command = rec.getCommand();
                        if (command.isSuccess()) {
                            return greenBubble;
                        } else if (command.isFailure()) {
                            return redBubble;
                        } else {
                            return grayBubble;
                        }
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var command = rec.getCommand();
                        if (command.getError() != null) {
                            return command.getError();
                        } else {
                            return super.getToolTipText(element);
                        }
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_QUEUED)) {
                var queuedColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                queuedColumn.getColumn().setText(COL_QUEUED);
                queuedColumn.getColumn().addControlListener(columnResizeListener);
                queuedColumn.getColumn().addSelectionListener(getSelectionAdapter(queuedColumn.getColumn()));
                queuedColumn.getColumn().setToolTipText("Queued acknowledgment");
                queuedColumn.setLabelProvider(new CenteredImageLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getQueuedAcknowledgment();
                        return getAckImage(ack);
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getQueuedAcknowledgment();
                        return (ack != null) ? ack.getMessage() : null;
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_RELEASED)) {
                var releasedColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                releasedColumn.getColumn().setText(COL_RELEASED);
                releasedColumn.getColumn().addControlListener(columnResizeListener);
                releasedColumn.getColumn().addSelectionListener(getSelectionAdapter(releasedColumn.getColumn()));
                releasedColumn.getColumn().setToolTipText("Released acknowledgment");
                releasedColumn.setLabelProvider(new CenteredImageLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getReleasedAcknowledgment();
                        return getAckImage(ack);
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getReleasedAcknowledgment();
                        return (ack != null) ? ack.getMessage() : null;
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (def.name.equals(COL_SENT)) {
                var sentColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
                sentColumn.getColumn().setText(COL_SENT);
                sentColumn.getColumn().addControlListener(columnResizeListener);
                sentColumn.getColumn().addSelectionListener(getSelectionAdapter(sentColumn.getColumn()));
                sentColumn.getColumn().setToolTipText("Sent acknowledgment");
                sentColumn.setLabelProvider(new CenteredImageLabelProvider() {
                    @Override
                    public Image getImage(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getSentAcknowledgment();
                        return getAckImage(ack);
                    }

                    @Override
                    public String getToolTipText(Object element) {
                        var rec = (CommandHistoryRecord) element;
                        var ack = rec.getCommand().getSentAcknowledgment();
                        return (ack != null) ? ack.getMessage() : null;
                    }
                });
                layout.addColumnData(new ColumnPixelData(def.width));
            } else if (dynamicColumns.contains(def.name)) {
                var column = new TableViewerColumn(tableViewer, SWT.LEFT);
                column.getColumn().setText(def.name);
                column.getColumn().addControlListener(columnResizeListener);
                column.getColumn().addSelectionListener(getSelectionAdapter(column.getColumn()));

                if (def.name.startsWith("Verifier_") || def.name.startsWith("Acknowledge_")) {
                    column.setLabelProvider(new CenteredImageLabelProvider() {
                        @Override
                        public Image getImage(Object element) {
                            var rec = (CommandHistoryRecord) element;
                            var ack = rec.getCommand().getAcknowledgment(def.name);
                            return getAckImage(ack);
                        }

                        @Override
                        public String getToolTipText(Object element) {
                            var rec = (CommandHistoryRecord) element;
                            var ack = rec.getCommand().getAcknowledgment(def.name);
                            return (ack != null) ? ack.getMessage() : null;
                        }
                    });
                    layout.addColumnData(new ColumnPixelData(def.width));
                } else {
                    column.setLabelProvider(new ColumnLabelProvider() {
                        @Override
                        public String getText(Object element) {
                            var rec = (CommandHistoryRecord) element;

                            var value = rec.getCommand().getAttribute(def.name);
                            if (value == null) {
                                return null;
                            } else if (value instanceof byte[]) {
                                return StringConverter.arrayToHexString((byte[]) value);
                            } else {
                                return String.valueOf(value);
                            }
                        }
                    });
                }
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

    private Image getAckImage(Acknowledgment ack) {
        if (ack == null) {
            return grayBubble;
        } else {
            switch (ack.getStatus()) {
            case "NEW":
                return grayBubble;
            case "OK":
                return greenBubble;
            case "PENDING":
                return waitingImage;
            case "SCHEDULED":
            case "DISABLED":
            case "CANCELLED":
                return grayBubble;
            case "TIMEOUT":
            case "NOK":
                return redBubble;
            default:
                log.fine("Unexpected ack state " + ack.getStatus());
                return redBubble;
            }
        }
    }

    private void saveColumnState() {
        var settings = CommandingPlugin.getDefault().getCommandHistoryTableSettings();

        var visibleColumns = columnData.getVisibleColumns();
        var visibleNames = visibleColumns.stream().map(c -> c.name).toArray(String[]::new);
        var visibleWidths = visibleColumns.stream().map(c -> c.width).toArray(Integer[]::new);

        settings.put("visible-cols", visibleNames);
        for (var i = 0; i < visibleNames.length; i++) {
            settings.put("visible-width-" + i, visibleWidths[i]);
        }

        var hiddenColumns = columnData.getHiddenColumns();
        var hiddenNames = hiddenColumns.stream().map(c -> c.name).toArray(String[]::new);
        settings.put("hidden-cols", hiddenNames);

        var dynamicColumns = this.dynamicColumns.toArray(new String[0]);
        settings.put("dynamic-cols", dynamicColumns);
    }

    private void restoreColumnState() {
        var settings = CommandingPlugin.getDefault().getCommandHistoryTableSettings();

        var oldVisibleNames = settings.getArray("visible-cols");
        if (oldVisibleNames != null) {
            var oldVisibleWidths = new int[oldVisibleNames.length];
            for (var i = 0; i < oldVisibleNames.length; i++) {
                oldVisibleWidths[i] = settings.getInt("visible-width-" + i);
            }
            var oldHiddenNames = settings.getArray("hidden-cols");
            List<String> oldDynamicColumns = Arrays.asList(settings.getArray("dynamic-cols"));

            var restoredData = new ColumnData();

            // Add visible columns we still remember from a previous session
            for (var i = 0; i < oldVisibleNames.length; i++) {
                if (oldVisibleNames[i].endsWith("_Status")) { // TEMP to work around a client bug
                    continue;
                }
                var def = columnData.getColumn(oldVisibleNames[i]);
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
            for (var i = 0; i < oldHiddenNames.length; i++) {
                if (oldHiddenNames[i].endsWith("_Status")) { // TEMP to work around a bug
                    continue;
                }
                var def = columnData.getColumn(oldHiddenNames[i]);
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
            for (var def : columnData.getColumns()) {
                if (restoredData.getColumn(def.name) == null) {
                    restoredData.addColumn(def.name, def.width, def.visible, def.resizable, def.moveable);
                }
            }

            columnData = restoredData;
        }
    }

    private void fetchLatestCommands() {
        var archiveClient = YamcsPlugin.getArchiveClient();
        if (archiveClient != null) {
            archiveClient.listCommands().whenComplete((page, exc) -> {
                var commands = new ArrayList<Command>();
                page.forEach(commands::add);

                Display.getDefault().asyncExec(() -> {
                    addCommands(commands);
                });
            });
        }
    }

    public void processCommand(Command command, boolean update) {
        // Maybe we need to update structure
        for (var acknowledgmentName : command.getAcknowledgments().keySet()) {
            if (acknowledgmentName.endsWith("_Status")) { // TODO remove once fixed on client
                acknowledgmentName = acknowledgmentName.substring(0, acknowledgmentName.length() - 7);
            }
            switch (acknowledgmentName) {
            case "Acknowledge_Queued":
            case "Acknowledge_Released":
            case "Acknowledge_Sent":
                continue;
            default:
                if (!dynamicColumns.contains(acknowledgmentName)) {
                    dynamicColumns.add(acknowledgmentName);
                    columnData.addColumn(acknowledgmentName, 90);
                    syncCurrentWidthsToModel();
                    createColumns();
                }
            }
        }
        for (var attributeName : command.getExtraAttributes().keySet()) {
            if (!dynamicColumns.contains(attributeName)) {
                dynamicColumns.add(attributeName);
                columnData.addColumn(attributeName, 90);
                syncCurrentWidthsToModel();
                createColumns();
            }
        }

        // Now add content
        if (update) {
            tableContentProvider.processCommand(command);
        }
    }

    public void addCommands(List<Command> commands) {
        if (tableViewer.getTable().isDisposed()) {
            return;
        }
        for (var command : commands) {
            processCommand(command, false);
        }
        tableContentProvider.addCommands(commands);
    }

    private SelectionAdapter getSelectionAdapter(TableColumn column) {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tableViewerComparator.setColumn(column);
                var dir = tableViewerComparator.getDirection();
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
