package org.yamcs.studio.core.model;

import java.time.Instant;

/**
 * Reports on time as indicated by the studio-wide processor
 */
public interface TimeListener {

    /**
     * Actual mission time as reported by Yamcs Server. If no time is defined (for example when not connected),
     * listeners will receive null
     */
    public void processTime(Instant missionTime);
}
