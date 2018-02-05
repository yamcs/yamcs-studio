package org.yamcs.studio.core.pvmanager;

/**
 * Supports writable Software parameters.
 * 
 * @deprecated use of sw:// datasource is discouraged. yamcs:// datasource was converted to support writing to
 *             parameters, while delegating the verification to the server.
 * 
 */
public class SoftwareParameterChannelHandler extends ParameterChannelHandler {

    public SoftwareParameterChannelHandler(String channelName) {
        super(channelName);
    }
}
