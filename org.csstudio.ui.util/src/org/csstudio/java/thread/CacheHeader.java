/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.java.thread;

import java.util.Date;

/**
 * Helper for computing cache expiration info.
 */
public class CacheHeader {

    /** Time when entry was last updated */
    final private Date timestamp;

    /** Time when entry expires */
    final private Date expiration;

    /**
     * Initialize
     * 
     * @param seconds
     *            Seconds after which clients should no longer cache the HTML
     */
    public CacheHeader(long seconds) {
        timestamp = new Date();
        expiration = new Date(timestamp.getTime() + seconds * 1000L);
    }

    /** Time when entry was last updated */
    public Date getTimestamp() {
        return timestamp;
    }

    /** Time when entry expires */
    public Date getExpirationDate() {
        return expiration;
    }

}
