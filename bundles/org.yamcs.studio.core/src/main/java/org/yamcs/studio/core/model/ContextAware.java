package org.yamcs.studio.core.model;

/**
 * Marks a component as being aware of the global UI state. This state includes the connected instance and/or processor.
 */
public interface ContextAware {

    /**
     * The globally activated instance has changed. This is always called on the UI thread.
     */
    default void changeInstance(String instance) {
    }

    /**
     * The globally activated processor has changed. This is always called on the UI thread.
     */
    default void changeProcessor(String instance, String processor) {
    }
}
