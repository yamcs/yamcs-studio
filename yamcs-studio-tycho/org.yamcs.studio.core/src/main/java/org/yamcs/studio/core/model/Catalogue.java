package org.yamcs.studio.core.model;

import org.yamcs.studio.core.StudioConnectionListener;

public interface Catalogue extends StudioConnectionListener {

    default void shutdown() {
        // NOP
    }
}
