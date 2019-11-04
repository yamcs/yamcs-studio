package org.yamcs.studio.commanding.queue;

import java.util.List;

import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.protobuf.Commanding.QueueState;

public class CommandQueue {

    private int order;
    private String queue;
    private QueueState state;
    private int stateExpirationTimeS;
    private List<CommandQueueEntry> commands;

    public CommandQueue(CommandQueueInfo proto, List<CommandQueueEntry> commands) {
        order = proto.getOrder();
        queue = proto.getName();
        state = proto.getState();
        stateExpirationTimeS = proto.getStateExpirationTimeS();
        this.commands = commands;
    }

    public int getOrder() {
        return order;
    }

    public String getQueue() {
        return queue;
    }

    public QueueState getState() {
        return state;
    }

    public int getStateExpirationTimeS() {
        return stateExpirationTimeS;
    }

    public List<CommandQueueEntry> getCommands() {
        return commands;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setState(QueueState state) {
        this.state = state;
    }

    public void setStateExpirationTimeS(int stateExpirationTimeS) {
        this.stateExpirationTimeS = stateExpirationTimeS;
    }

    public void setCommands(List<CommandQueueEntry> commands) {
        this.commands = commands;
    }
}
