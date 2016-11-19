package org.yamcs.studio.ui.commanding.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Commanding.QueueState;
import org.yamcs.protobuf.Rest.EditCommandQueueRequest;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.security.YamcsAuthorizations;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.queue.QueuesTableModel.RowCommandQueueInfo;

public class CommandQueuesTableViewer extends TableViewer {

    public static final String COL_QUEUE = "Queue";
    public static final String COL_STATE = "State";
    public static final String COL_COMMANDS = "Commands";
    public static final String COL_SENT = "Sent";
    public static final String COL_REJECTED = "Rejected";

    private CommandQueueView commandQueueView;

    Composite parent;

    public CommandQueuesTableViewer(CommandQueueView commandQueueView, Composite parent, TableColumnLayout tcl) {
        super(new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL));
        //   this.styleProvider = styleProvider;

        this.commandQueueView = commandQueueView;
        this.parent = parent;

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
        addFixedColumns(tcl);
        createContextMenu();

        setLabelProvider(new ModelLabelProvider());

    }

    private void addFixedColumns(TableColumnLayout tcl) {

        TableViewerColumn nameColumn = new TableViewerColumn(this, SWT.NONE);
        nameColumn.getColumn().setText(COL_QUEUE);
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(40));

        TableViewerColumn stateColumn = new TableViewerColumn(this, SWT.CENTER);
        stateColumn.getColumn().setText(COL_STATE);
        stateColumn.getColumn().setWidth(250);
        stateColumn.setEditingSupport(new StateEditingSupport(stateColumn.getViewer()));
        tcl.setColumnData(stateColumn.getColumn(), new ColumnWeightData(30));

        TableViewerColumn commandsColumn = new TableViewerColumn(this, SWT.CENTER);
        commandsColumn.getColumn().setText(COL_COMMANDS);
        tcl.setColumnData(commandsColumn.getColumn(), new ColumnWeightData(10));

        TableViewerColumn sentColumn = new TableViewerColumn(this, SWT.CENTER);
        sentColumn.getColumn().setText(COL_SENT);
        tcl.setColumnData(sentColumn.getColumn(), new ColumnWeightData(10));

        TableViewerColumn rejectedColumn = new TableViewerColumn(this, SWT.CENTER);
        rejectedColumn.getColumn().setText(COL_REJECTED);
        tcl.setColumnData(rejectedColumn.getColumn(), new ColumnWeightData(10));

        // Common properties to all columns
        List<TableViewerColumn> columns = new ArrayList<>();
        columns.add(nameColumn);
        columns.add(stateColumn);
        columns.add(commandsColumn);
        columns.add(sentColumn);
        columns.add(rejectedColumn);
        for (TableViewerColumn column : columns) {
            // prevent resize to 0
            column.getColumn().addControlListener(new ControlListener() {
                @Override
                public void controlMoved(ControlEvent e) {
                }

                @Override
                public void controlResized(ControlEvent e) {
                    if (column.getColumn().getWidth() < 5)
                        column.getColumn().setWidth(5);
                }
            });
        }

    }

    private void createContextMenu() {
        MenuManager popupManager = new MenuManager();
        popupManager.add(new Action("Block Queue") {
            @Override
            public void run() {
                IStructuredSelection sel = (IStructuredSelection) getSelection();
                if (!sel.isEmpty()) {
                    CommandQueue commandQueue = (CommandQueue) sel.getFirstElement();
                    updateCommandQueueState(commandQueue, QueueState.BLOCKED);
                }
            }
        });
        popupManager.add(new Action("Disable Queue") {
            @Override
            public void run() {
                IStructuredSelection sel = (IStructuredSelection) getSelection();
                if (!sel.isEmpty()) {
                    CommandQueue commandQueue = (CommandQueue) sel.getFirstElement();
                    updateCommandQueueState(commandQueue, QueueState.DISABLED);
                }
            }
        });
        popupManager.add(new Action("Enable Queue") {
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

    class StateEditingSupport extends EditingSupport {
        private ComboBoxViewerCellEditor cellEditor = null;

        private StateEditingSupport(ColumnViewer viewer) {
            super(viewer);
            cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
            cellEditor.setLabelProvider(new LabelProvider());
            cellEditor.setContentProvider(new ArrayContentProvider());
            cellEditor.setInput(Commanding.QueueState.values());
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return YamcsAuthorizations.getInstance().hasSystemPrivilege(YamcsAuthorizations.SystemPrivilege.MayControlCommandQueue);
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof CommandQueue) {
                CommandQueue data = (CommandQueue) element;
                return data.getState();
            }
            return null;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof CommandQueue && value instanceof Commanding.QueueState) {
                CommandQueue data = (CommandQueue) element;
                QueueState newValue = (QueueState) value;
                updateCommandQueueState(data, newValue);
            }
        }
    }

    private void updateCommandQueueState(CommandQueue commandQueue, QueueState newState) {
        /* only set new value if it differs from old one */
        if (!commandQueue.getState().equals(newState)) {
            commandQueue.setState(newState);

            CommandQueueInfo q = null;
            for (RowCommandQueueInfo rcqi : commandQueueView.currentQueuesModel.queues) {
                if (rcqi.cq == commandQueue)
                    q = rcqi.commandQueueInfo;
            }
            //CommandQueueInfo q = queues.get(row);
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
                if (missionTime - cqe.getGenerationTime() > CommandQueueView.oldCommandWarningTime * 1000L) {
                    oldcommandsfound = true;
                    break;
                }
            }
        }

        if (oldcommandsfound) {
            int result = CommandFateDialog.showDialog2(parent.getShell());
            switch (result) {
            case -1://cancel
                return;
            case 0: //send with updated times
                updateQueueState(q, QueueState.ENABLED, true);
                break;
            case 1://send with old times
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
        EditCommandQueueRequest req = EditCommandQueueRequest.newBuilder().setState(queueState.toString()).build();
        CommandingCatalogue catalogue = CommandingCatalogue.getInstance();
        catalogue.editQueue(queue, req, RestClient.NULL_RESPONSE_HANDLER);
    }

    class ModelLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            // no image to show
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            // each element comes from the ContentProvider.getElements(Object)
            if (!(element instanceof CommandQueue)) {
                return "";
            }
            CommandQueue model = (CommandQueue) element;
            switch (columnIndex) {
            case 0:
                return model.getQueue();
            case 1:
                String state = model.getState().name();
                if (model.getStateExpirationTimeS() > 0) {
                    state += " (" + toDayHourMinuteSecond(model.getStateExpirationTimeS()) + ")";
                }
                return state;
            case 2:
                if (model.getCommands() == null)
                    return 0 + "";
                return model.getCommands().size() + "";
            case 3:
                return model.getNbSentCommands() + "";
            case 4:
                return model.getNbRejectedCommands() + "";
            default:
                break;
            }
            return "";
        }
    }

    static public String toDayHourMinuteSecond(int totalSeconds) {
        String result = "";
        int days = (int) TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) - (days * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - (TimeUnit.SECONDS.toHours(totalSeconds) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(totalSeconds) - (TimeUnit.SECONDS.toMinutes(totalSeconds) * 60);

        result = hours + ":" + minutes + ":" + seconds;
        if (days > 0)
            result = days + "d " + result;

        return result;
    }

    public static void main(String arg[]) {
        int nbSeconds = 5; // 00:00:05
        System.out.println("nbSeconds = " + nbSeconds + "=" + CommandQueuesTableViewer.toDayHourMinuteSecond(nbSeconds));
        nbSeconds = 65; // 00:01:05
        System.out.println("nbSeconds = " + nbSeconds + "=" + CommandQueuesTableViewer.toDayHourMinuteSecond(nbSeconds));
        nbSeconds = 3665; // 01:01:05
        System.out.println("nbSeconds = " + nbSeconds + "=" + CommandQueuesTableViewer.toDayHourMinuteSecond(nbSeconds));
        nbSeconds = 90065; //1d 01:01:05
        System.out.println("nbSeconds = " + nbSeconds + "=" + CommandQueuesTableViewer.toDayHourMinuteSecond(nbSeconds));

    }
}
