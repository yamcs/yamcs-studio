package org.yamcs.studio.core;

import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;

/**
 * Informs different components of new or changed connection settings.
 * <p>
 * This was originally created as a way to have a central go-green signal because we are setting up
 * a bunch of different connection types to the same server. One of them would act as the leader,
 * and if it works, than go over all the other ones.
 * <p>
 * Eventually we could extend the usage to dynamic changing of connection info.
 */
public interface StudioConnectionListener {

    /**
     * Called when we get green light from YamcsPlugin
     */
    void processConnectionInfo(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps);

    /**
     * Called when YamcsPlugin wants this connection to stop (might be resumed latter with
     * processConnectionInfo)
     */
    void disconnect();

}
