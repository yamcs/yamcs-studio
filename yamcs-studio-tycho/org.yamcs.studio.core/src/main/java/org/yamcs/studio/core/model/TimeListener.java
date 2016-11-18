package org.yamcs.studio.core.model;

/**
 * Reports on time as indicated by the studio-wide processor
 */
public interface TimeListener {

    /**
     * Actual mission time as reported by Yamcs Server. If no time is defined (for example when not
     * connected), listeners will receive {@code TimeEncoding.INVALID_INSTANT}
     */
    public void processTime(long missionTime);

}
