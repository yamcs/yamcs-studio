package org.yamcs.studio.core;

/**
 * A singleton service managed by {@link YamcsPlugin}
 */
public interface PluginService {

    /**
     * Performs and necessary cleanup.
     */
    void dispose();
}
