package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Commanding.CommandQueueEntry;
import org.yamcs.protobuf.Commanding.CommandQueueInfo;
import org.yamcs.studio.core.StudioConnectionListener;

public interface CommandQueueListener extends StudioConnectionListener {

    /*
     * called once after the connection to yamcs has been (re)established and then each time when a
     * queue changes state
     */
    void updateQueue(CommandQueueInfo queue);

    void commandAdded(CommandQueueEntry entry);

    void commandRejected(CommandQueueEntry entry);

    void commandSent(CommandQueueEntry entry);
}
