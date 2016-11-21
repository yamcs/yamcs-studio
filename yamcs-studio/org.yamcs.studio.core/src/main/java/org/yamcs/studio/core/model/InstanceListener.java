package org.yamcs.studio.core.model;

/**
 * Informs when the connection has changed from one Yamcs instance to another. (this does not imply
 * a disconnect/connect of the web socket)
 */
public interface InstanceListener {

    /**
     * Called when the Studio changes from oldInstance to newInstance. Only change events are sent.
     * You will not get an update with the actual instance when you register.
     */
    void instanceChanged(String oldInstance, String newInstance);
}
