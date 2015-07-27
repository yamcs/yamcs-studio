package org.yamcs.studio.ui.commanding.queue;

import java.util.ArrayList;

import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;

public class CommandQueue {

    private String queue;
    private Commanding.QueueState state;
    //  private int commands;

    ArrayList<CommandQueueEntry> commands;

    public CommandQueue(String queue, Commanding.QueueState state, ArrayList<CommandQueueEntry> commands)
    {
        this.queue = queue;
        this.state = state;
        this.commands = commands;
    }

    public String getQueue()
    {
        return queue;
    }

    public void setQueue(String queue)
    {
        this.queue = queue;
    }

    public Commanding.QueueState getState()
    {
        return state;
    }

    public void setState(Commanding.QueueState state)
    {
        this.state = state;
    }

    public ArrayList<CommandQueueEntry> getCommands()
    {
        return commands;
    }

    public void setCommands(ArrayList<CommandQueueEntry> commands)
    {
        this.commands = commands;
    }

}
