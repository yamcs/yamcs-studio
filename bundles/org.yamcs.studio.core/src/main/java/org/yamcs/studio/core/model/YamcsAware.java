package org.yamcs.studio.core.model;

import java.time.Instant;

import org.yamcs.studio.core.MissionDatabase;

/**
 * Marks a component as being aware of the global UI state. This state includes the connected instance and/or processor.
 */
public interface YamcsAware {

    /**
     * The globally activated instance has changed. This is always called on the UI thread.
     */
    default void changeInstance(String instance) {
    }

    /**
     * The globally activated processor has changed. This is always called on the UI thread.
     * <p>
     * Note that this method is not called if only the instance has changed.
     */
    default void changeProcessor(String instance, String processor) {
    }

    /**
     * New client-side MDB definitions have been loaded.
     */
    default void changeMissionDatabase(MissionDatabase missionDatabase) {
    }

    /**
     * Actual mission time as reported by Yamcs Server. If no time is defined (for example when not connected),
     * listeners will receive null
     */
    default void updateTime(Instant time) {
    }
}
