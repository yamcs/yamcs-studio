package org.yamcs.studio.commanding.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Commanding.QueueState;
import org.yamcs.protobuf.EditQueueRequest;
import org.yamcs.studio.commanding.queue.QueuesTableModel.RowCommandQueueInfo;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.utils.TimeEncoding;

public class CommandQueuesTableViewer extends TableViewer {

    public static final String COL_ORDER = "#";
    public static final String COL_QUEUE = "Queue";
    public static final String COL_ACTION = "Action";
    public static final String COL_COMMANDS = "Pending";

    private CommandQueueView commandQueueView;

    Composite parent;

    public CommandQueuesTableViewer(CommandQueueView commandQueueView, Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        // this.styleProvider = styleProvider;

        this.commandQueueView = commandQueueView;
        this.parent = parent;

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);
        createContextMenu();
    }

    private void addFixedColumns(TableColumnLayout tcl) {
        TableViewerColumn orderColumn = new TableViewerColumn(this, SWT.NONE);
        orderColumn.getColumn().setText(COL_ORDER);
        orderColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandQueue model = (CommandQueue) element;
                return String.valueOf(model.getOrder());
            }
        });
        tcl.setColumnData(orderColumn.getColumn(), new ColumnPixelData(30));

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_QUEUE);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandQueue model = (CommandQueue) element;
                return model.getQueue();
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(40));

        TableViewerColumn actionColumn = new TableViewerColumn(this, SWT.CENTER);
        actionColumn.getColumn().setText(COL_ACTION);
        actionColumn.getColumn().setWidth(250);
        actionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandQueue model = (CommandQueue) element;
                String action;
                switch (model.getState()) {
                case BLOCKED:
                    action = "HOLD";
                    break;
                case DISABLED:
                    action = "REJECT";
                    break;
                case ENABLED:
                    action = "ACCEPT";
                    break;
                default:
                    throw new IllegalStateException("Unexpected state " + model.getState());
                }
                if (model.getStateExpirationTimeS() > 0) {
                    action += " (" + toDayHourMinuteSecond(model.getStateExpirationTimeS()) + ")";
                }
                return action;
            }
        });
        tcl.setColumnData(actionColumn.getColumn(), new ColumnWeightData(30));

        TableViewerColumn commandsColumn = new TableViewerColumn(this, SWT.CENTER);
        commandsColumn.getColumn().setText(COL_COMMANDS);
        commandsColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                CommandQueue model = (CommandQueue) element;
                if (model.getCommands() == null) {
                    return "0";
                } else {
                    return String.valueOf(model.getCommands().size());
                }
            }
        });
        tcl.setColumnData(commandsColumn.getColumn(), new ColumnWeightData(10));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(orderColumn);
        columns.add(nameColumn);
        columns.add(actionColumn);
        columns.add(commandsColumn);
        for (TableViewerColumn column : columns) {
            // prevent resize to 0
            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (column.getColumn().getWidth() < 5) {
                        column.getColumn().setWidth(5);
                    }
                }
            });
        }

    }

    private void createContextMenu() {
        MenuManager popupManager = new MenuManager();
        popupManager.add(new Action("Action: HOLD") {
            @Override
            public void run() {
                IStructuredSelection sel = (IStructuredSelection) getSelection();
                if (!sel.isEmpty()) {
                    CommandQueue commandQueue = (CommandQueue) sel.getFirstElement();
                    updateCommandQueueState(commandQueue, QueueState.BLOCKED);
                }
            }
        });
        popupManager.add(new Action("Action: REJECT") {
            @Override
            public void run() {
                IStructuredSelection sel = (IStructuredSelection) getSelection();
                if (!sel.isEmpty()) {
                    CommandQueue commandQueue = (CommandQueue) sel.getFirstElement();
                    updateCommandQueueState(commandQueue, QueueState.DISABLED);
                }
            }
        });
        popupManager.add(new Action("Action: ACCEPT") {
            @Override
            public void run() {
                IStructuredSelection sel = (IStructuredSelection) getSelection();
                if (!sel.isEmpty()) {
                    CommandQueue commandQueue = (CommandQueue) sel.getFirstElement();
                    updateCommandQueueState(commandQueue, QueueState.ENABLED);
                }
            }
        });

        Menu popup = popupManager.createContextMenu(getTable());
        getTable().setMenu(popup);
    }

    private void updateCommandQueueState(CommandQueue commandQueue, QueueState newState) {
        if (!commandQueue.getState().equals(newState)) {

            CommandQueueInfo q = null;
            for (RowCommandQueueInfo rcqi : commandQueueView.currentQueuesModel.queues) {
                if (rcqi.cq == commandQueue) {
                    q = rcqi.commandQueueInfo;
                }
            }
            if (newState == QueueState.BLOCKED) {
                blockQueue(q);
            } else if (newState == QueueState.DISABLED) {
                disableQueue(q);
            } else if (newState == QueueState.ENABLED) {
                enableQueue(q);
            }
        }
    }

    private void blockQueue(CommandQueueInfo q) {
        updateQueueState(q, QueueState.BLOCKED, false);
    }

    private void disableQueue(CommandQueueInfo q) {
        updateQueueState(q, QueueState.DISABLED, false);
    }

    private void enableQueue(CommandQueueInfo q) {
        boolean oldcommandsfound = false;
        ArrayList<CommandQueueEntry> cmds = commandQueueView.currentQueuesModel.commands.get(q.getName());
        if (cmds != null) {
            for (CommandQueueEntry cqe : cmds) {
                long missionTime = TimeCatalogue.getInstance().getMissionTime();
                long generationTime = TimeEncoding.fromProtobufTimestamp(cqe.getGenerationTime());
                if (missionTime - generationTime > CommandQueueView.oldCommandWarningTime * 1000L) {
                    oldcommandsfound = true;
                    break;
                }
            }
        }

        if (oldcommandsfound) {
            int result = CommandFateDialog.showDialog2(parent.getShell());
            switch (result) {
            case -1:// cancel
                return;
            case 0: // send with updated times
                updateQueueState(q, QueueState.ENABLED, true);
                break;
            case 1:// send with old times
                updateQueueState(q, QueueState.ENABLED, false);
                break;
            }
        } else {
            updateQueueState(q, QueueState.ENABLED, false);
        }
    }

    // rebuild doesn't seem to do anything and therefore is not currently included in rest api
    // keeping it around for future reference mostly
    private void updateQueueState(CommandQueueInfo queue, QueueState queueState, boolean rebuild) {
        EditQueueRequest req = EditQueueRequest.newBuilder().setState(queueState.toString()).build();
        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.editQueue(queue, req);
    }

    private static String toDayHourMinuteSecond(int totalSeconds) {
        String result = "";
        int days = (int) TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - (TimeUnit.SECONDS.toHours(totalSeconds) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(totalSeconds) - (TimeUnit.SECONDS.toMinutes(totalSeconds) * 60);

        result = hours + ":" + minutes + ":" + seconds;
        if (days > 0) {
            result = days + "d " + result;
        }

        return result;
    }
}
