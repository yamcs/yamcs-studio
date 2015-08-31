package org.yamcs.studio.ui.commanding.queue;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.studio.core.YamcsAuthorizations;
import org.yamcs.studio.ui.commanding.queue.QueuesTableModel.RowCommandQueueInfo;
import org.yamcs.utils.TimeEncoding;

public class CommandQueuesTableViewer extends TableViewer {

    private static final Logger log = Logger.getLogger(CommandQueuesTableViewer.class.getName());

    public static final String COL_QUEUE = "Queue";
    public static final String COL_STATE = "State";
    public static final String COL_COMMANDS = "Commands";
    public static final String COL_SENT = "Sent";
    public static final String COL_REJECTED = "Rejected";

    private CommandQueuesTableContentProvider contentProvider;
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

    }

    class StateEditingSupport extends EditingSupport
    {
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
                Commanding.QueueState newValue = (Commanding.QueueState) value;
                /* only set new value if it differs from old one */
                if (!data.getState().equals(newValue)) {
                    data.setState(newValue);

                    try {
                        CommandQueueInfo q = null;
                        for (RowCommandQueueInfo rcqi : commandQueueView.currentQueuesModel.queues)
                        {
                            if (rcqi.cq == element)
                                q = rcqi.commandQueueInfo;
                        }
                        //CommandQueueInfo q = queues.get(row);
                        if (value.equals(Commanding.QueueState.BLOCKED)) {
                            commandQueueView.commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(Commanding.QueueState.BLOCKED)
                                    .build(), false);
                        } else if (value.equals(Commanding.QueueState.DISABLED)) {
                            commandQueueView.commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q)
                                    .setState(Commanding.QueueState.DISABLED).build(), false);
                        } else if (value.equals(Commanding.QueueState.ENABLED)) {
                            boolean oldcommandsfound = false;
                            ArrayList<CommandQueueEntry> cmds = commandQueueView.currentQueuesModel.commands.get(q.getName());
                            if (cmds != null) {
                                for (CommandQueueEntry cqe : cmds) {
                                    if (TimeEncoding.currentInstant() - cqe.getGenerationTime() > CommandQueueView.oldCommandWarningTime * 1000L) {
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
                                    commandQueueView.commandQueueControl.setQueueState(
                                            CommandQueueInfo.newBuilder(q).setState(Commanding.QueueState.ENABLED).build(), true);
                                    break;
                                case 1://send with old times
                                    commandQueueView.commandQueueControl.setQueueState(
                                            CommandQueueInfo.newBuilder(q).setState(Commanding.QueueState.ENABLED).build(), false);
                                    break;
                                }
                            } else {
                                commandQueueView.commandQueueControl.setQueueState(
                                        CommandQueueInfo.newBuilder(q).setState(Commanding.QueueState.ENABLED).build(),
                                        false);
                            }
                        }
                    } catch (Exception e) {
                        log.severe(e.getMessage());
                    }

                }
            }

        }

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
                if (model.getStateExpirationTimeS() > 0)
                {
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

    static public String toDayHourMinuteSecond(int totalSeconds)
    {
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

    public static void main(String arg[])
    {
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
