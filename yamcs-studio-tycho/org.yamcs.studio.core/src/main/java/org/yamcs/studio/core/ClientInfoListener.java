package org.yamcs.studio.core;

import org.yamcs.protobuf.YamcsManagement.ClientInfo;

/**
 * Reports on assigned client-info after an established web socket connection. Useful for retrieving
 * the client-id needed in some operations (i.e. replays)
 *
 * TODO this might as well use E4 events. It should be just a one-time thing even.
 */
public interface ClientInfoListener {

    public void processClientInfo(ClientInfo clientInfo);
}
