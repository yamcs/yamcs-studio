package org.yamcs.studio.core.model;

import java.util.List;

import org.yamcs.protobuf.Yamcs.Event;

/**
 * Reports on a batches of events.
 * Useful for limiting GUI updates.
 */
@FunctionalInterface
public interface BulkEventListener {

    void processEvents(List<Event> events);
}
