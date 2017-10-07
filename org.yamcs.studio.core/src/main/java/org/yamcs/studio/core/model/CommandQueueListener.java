package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;

public interface CommandQueueListener {

    /**
     * called once after the connection to yamcs has been (re)established and then each time when a
     * queue changes state
     */
    void updateQueue(CommandQueueInfo queue);

    void commandAdded(CommandQueueEntry entry);

    void commandRejected(CommandQueueEntry entry);

    void commandSent(CommandQueueEntry entry);

    /**
     * Called when the model state of the backing commanding catalogue has changed, such that all
     * listeners should follow suit. This can happen for two reasons:
     * <ul>
     * <li>The studio connection was lost
     * <li>The Yamcs instance for the current client was updated
     * </ul>
     */
    void clearCommandQueueData();
}
