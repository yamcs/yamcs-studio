package org.yamcs.studio.core.model;

/**
 * Reports on time as indicated by the studio-wide processor
 */
public interface TimeListener {

    public void processTime(long missionTime);

}
