package org.yamcs.studio.ui.commanding.queue;

import java.util.ArrayList;

import org.yamcs.protobuf.Commanding;
import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;

public class CommandQueue {

    private String queue;
    private Commanding.QueueState state;
    //  private int commands;

    ArrayList<CommandQueueEntry> commands;
    private int nbSentCommands;
    private int nbRejectedCommands;

    public CommandQueue(CommandQueueInfo cqi, ArrayList<CommandQueueEntry> commands)
    {
        this.queue = cqi.getName();
        this.state = cqi.getState();
        this.setNbRejectedCommands(cqi.getNbRejectedCommands());
        this.setNbSentCommands(cqi.getNbSentCommands());
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

    public int getNbSentCommands() {
        return nbSentCommands;
    }

    public void setNbSentCommands(int nbSentCommands) {
        this.nbSentCommands = nbSentCommands;
    }

    public int getNbRejectedCommands() {
        return nbRejectedCommands;
    }

    public void setNbRejectedCommands(int nbRejectedCommands) {
        this.nbRejectedCommands = nbRejectedCommands;
    }

}
