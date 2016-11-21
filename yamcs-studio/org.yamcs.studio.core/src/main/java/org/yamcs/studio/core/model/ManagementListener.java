package org.yamcs.studio.core.model;

import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;

/**
 * Server-wide updates on yamcs processors and their connected clients. Register for updates with
 * YamcsPlugin
 */
public interface ManagementListener {

    /**
     * Called when *any* processor is updated. Includes 'replay state', 'replay configuration'.
     */
    public void processorUpdated(ProcessorInfo processorInfo);

    /**
     * Called when *any* processor is closed
     */
    public void processorClosed(ProcessorInfo processorInfo);

    /**
     * Called when *any* processor's statistics are updated. Includes current time of the replay.
     */
    public void statisticsUpdated(Statistics stats);

    /**
     * Called when *any* client's ClientInfo was updated. Includes things like the subscribed
     * processor.
     */
    public void clientUpdated(ClientInfo clientInfo);

    /**
     * Called when *any* client was disconnected.
     */
    public void clientDisconnected(ClientInfo clientInfo);

    /**
     * Called when all listeners should update their state to remove any client or processor data.
     * This serves to warrant consistency between the catalogue and any listeners.
     * <p>
     * Currently the only event where this can happen, is when the Studio lost connection.
     */
    public void clearAllManagementData();
}
