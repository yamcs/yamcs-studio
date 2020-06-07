package org.yamcs.studio.core;

import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.UserInfo;

/**
 * Groups entities that have been populated from the remote server.
 * <p>
 * This is intended as a convenience class for passing information from {@link YamcsConnector} to {@link YamcsPlugin}.
 */
public class RemoteEntityHolder {

    // Required
    YamcsClient yamcsClient;
    UserInfo userInfo;

    // Optional
    String instance;
    ProcessorInfo processor;
    MissionDatabase missionDatabase;
}
