package org.yamcs.studio.core.model;

import org.yamcs.studio.core.YamcsConnectionListener;

public interface Catalogue extends YamcsConnectionListener, InstanceListener {

    default void shutdown() {
        // NOP
    }
}
