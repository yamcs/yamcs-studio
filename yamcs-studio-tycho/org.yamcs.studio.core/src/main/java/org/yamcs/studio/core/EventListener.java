package org.yamcs.studio.core;

import org.yamcs.protobuf.Yamcs.Event;

/**
 * Reports on events on the studio-wide processor
 */
public interface EventListener {

    public void processEvent(Event event);

}
