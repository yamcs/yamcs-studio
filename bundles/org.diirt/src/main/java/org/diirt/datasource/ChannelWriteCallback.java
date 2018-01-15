/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

/**
 * Called by the ChannelHandler once a write is completed.
 *
 * @author carcassi
 */
public interface ChannelWriteCallback {

    /**
     * Called when a write is completed. If completed without error,
     * the argument is null.
     *
     * @param ex the exception if the write failed, null otherwise
     */
    public void channelWritten(Exception ex);
}
