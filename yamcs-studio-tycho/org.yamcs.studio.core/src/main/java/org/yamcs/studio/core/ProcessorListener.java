package org.yamcs.studio.core;

/**
 * Register with YamcsPlugin
 */
public interface ProcessorListener {

    /**
     * When the client is subscribed to a (new) processor
     */
    public void onProcessorSwitch(String processorName);
}
