package org.yamcs.studio.ui.commanding.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;

public class QueuesTableModel implements IStructuredContentProvider {

    private CommandQueueView commandQueueView;
    private TableViewer queuesTableViewer;
    private TableViewer commandsTableViewer;
    List<RowCommandQueueInfo> queues = new ArrayList<RowCommandQueueInfo>(3);
    Map<String, ArrayList<CommandQueueEntry>> commands = new HashMap<String, ArrayList<CommandQueueEntry>>();
    String instance, channel;

    public QueuesTableModel(CommandQueueView commandQueueView, TableViewer queuesTableViewer,
            TableViewer commandsTableViewer,
            String instance, String channel) {
        this.commandQueueView = commandQueueView;
        this.queuesTableViewer = queuesTableViewer;
        this.commandsTableViewer = commandsTableViewer;
        this.instance = instance;
        this.channel = channel;
    }

    void updateQueue(CommandQueueInfo cqi) {

        ArrayList<CommandQueueEntry> cmds = commands.get(cqi.getName());
        if (cmds == null) {
            cmds = new ArrayList<CommandQueueEntry>();
            commands.put(cqi.getName(), cmds);
        }
        CommandQueue newCq = new CommandQueue(cqi.getName(), cqi.getState(), cmds);

        boolean found = updateQueueProperties(cqi.getName(), newCq);

        if (!found)
        {

            queues.add(new RowCommandQueueInfo(newCq, cqi));
            queuesTableViewer.add(newCq);
        }
    }

    void commandAdded(final CommandQueueEntry cqe) {
        ArrayList<CommandQueueEntry> cmds = commands.get(cqe.getQueueName());
        if (cmds == null) {
            cmds = new ArrayList<CommandQueueEntry>();
            commands.put(cqe.getQueueName(), cmds);
        }
        cmds.add(cqe);
        reloadCommandsTable(currentSelection);
        // update command count number
        updateQueueProperties(cqe.getQueueName(), null);
    }

    boolean updateQueueProperties(String queueName, CommandQueue newCq)
    {
        for (int i = 0; i < queues.size(); i++) {
            RowCommandQueueInfo rq = queues.get(i);
            if (rq.commandQueueInfo.getName().equals(queueName)) {
                if (newCq != null)
                {
                    rq.cq.setQueue(newCq.getQueue());
                    rq.cq.setState(newCq.getState());
                    rq.cq.setCommands(newCq.getCommands());
                }
                queuesTableViewer.update(rq.cq, null);
                return true;
            }
        }
        return false;
    }

    void removeCommandFromQueue(CommandQueueEntry cqe) {
        ArrayList<CommandQueueEntry> cmds = commands.get(cqe.getQueueName());
        if (cmds == null) {
            return;
        }
        for (int i = 0; i < cmds.size(); i++) {
            if (cmds.get(i).getCmdId().equals(cqe.getCmdId())) {
                cmds.remove(i);
                reloadCommandsTable(currentSelection);
                break;
            }
        }
        updateQueueProperties(cqe.getQueueName(), null);
    }

    /**
     * Called when some rows are selected in the queue table. Shows all the commands in the selected
     * queues
     */
    void setQueue(CommandQueueInfo q) {
        commandsTableViewer.getTable().removeAll();
        ArrayList<CommandQueueEntry> cmds = commands.get(q.getName());
        if (cmds == null) {
            return;
        }
        for (CommandQueueEntry cqe : cmds) {
            commandsTableViewer.add(cqe);
        }
    }

    IStructuredSelection currentSelection;

    public void reloadCommandsTable(IStructuredSelection selection) {
        currentSelection = selection;
        if (selection == null)
            return;
        if (this == commandQueueView.currentQueuesModel) {

            CommandQueueInfo q = null;
            try {
                for (RowCommandQueueInfo rcqi : queues)
                {
                    if (rcqi.cq == selection.getFirstElement())
                    {
                        q = rcqi.commandQueueInfo;
                    }
                }
            } catch (Exception e) {
            }

            if (q != null) {
                setQueue(q);
            }
        }
    }

    CommandQueueEntry getCommand(String queueName, int index) {
        ArrayList<CommandQueueEntry> cmds = commands.get(queueName);
        if (cmds == null)
            return null;
        return cmds.get(index);
    }

    public boolean isCellEditable(int row, int col) {
        return col == 1;
    }

    public void setValueAt(Object value, int row, int col) {
        //        super.setValueAt(value, row, col);
        //        System.out.println(String.format("setvalue %d/%d to %s", row, col, value));
        //        try {
        //            CommandQueueInfo q = queues.get(row);
        //            if (value.equals(queueStateItems[0])) {
        //                commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(QueueState.BLOCKED).build(), false);
        //            } else if (value.equals(queueStateItems[1])) {
        //                commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(QueueState.DISABLED).build(), false);
        //            } else if (value.equals(queueStateItems[2])) {
        //                boolean oldcommandsfound = false;
        //                ArrayList<CommandQueueEntry> cmds = commands.get(q.getName());
        //                if (cmds != null) {
        //                    for (CommandQueueEntry cqe : cmds) {
        //                        if (TimeEncoding.currentInstant() - cqe.getGenerationTime() > oldCommandWarningTime * 1000L) {
        //                            oldcommandsfound = true;
        //                            break;
        //                        }
        //                    }
        //                }
        //
        //                if (oldcommandsfound) {
        //                    int result = CommandFateDialog.showDialog2(frame);
        //                    switch (result) {
        //                    case -1://cancel
        //                        return;
        //                    case 0: //send with updated times
        //                        commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(QueueState.ENABLED).build(), true);
        //                        break;
        //                    case 1://send with old times
        //                        commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(QueueState.ENABLED).build(), false);
        //                        break;
        //                    }
        //                } else {
        //                    commandQueueControl.setQueueState(CommandQueueInfo.newBuilder(q).setState(QueueState.ENABLED).build(), false);
        //                }
        //            }
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            YamcsMonitor.theApp.showMessage(e.toString());
        //        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return queues.toArray();
    }

    // Associate a table row to a CommandQueueInfo
    // Convenient to retrieve the selected queue
    class RowCommandQueueInfo {
        public RowCommandQueueInfo(CommandQueue cq, CommandQueueInfo commandQueueInfo)
        {
            this.cq = cq;
            this.commandQueueInfo = commandQueueInfo;
        }

        CommandQueue cq;
        CommandQueueInfo commandQueueInfo;
    }

}
