package org.yamcs.studio.core;

/**
 * Informs different components of connect/disconnect events on the global yamcs connection.
 */
public interface StudioConnectionListener {

    default void onStudioConnectionFailure(Throwable t) {
    }

    /**
     * Called when we the global connection to yamcs was succesfully established
     */
    void onStudioConnect();

    /**
     * Called when the yamcs is connection went lost (might be resumed later with {@link onStudioConnect()})
     */
    void onStudioDisconnect();

}
