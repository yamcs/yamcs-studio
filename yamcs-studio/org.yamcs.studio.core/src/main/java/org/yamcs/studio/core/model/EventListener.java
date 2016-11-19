package org.yamcs.studio.core.model;

import org.yamcs.protobuf.Yamcs.Event;

/**
 * Reports on events in the studio-wide instance
 */
public interface EventListener {

    void processEvent(Event event);
}
