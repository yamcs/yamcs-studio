package org.yamcs.studio.core.model;

import org.yamcs.protobuf.YamcsManagement.LinkInfo;

/**
 * Server-wide updates on yamcs data links. Register for updates with YamcsPlugin
 */
public interface LinkListener {

    public void linkRegistered(LinkInfo linkInfo);

    public void linkUnregistered(LinkInfo linkInfo);

    public void linkUpdated(LinkInfo linkInfo);
}
