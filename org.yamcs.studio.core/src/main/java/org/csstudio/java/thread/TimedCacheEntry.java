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
 * Entry in the {@link TimedCache}
 */
public class TimedCacheEntry<VALUETYPE> {
    final private VALUETYPE value;
    final private CacheHeader header;

    /**
     * Initialize
     *
     * @param value
     *            Value of this entry
     * @param valid_seconds
     *            Duration in seconds, how long this entry is valid
     */
    public TimedCacheEntry(VALUETYPE value, long valid_seconds) {
        this.value = value;
        header = new CacheHeader(valid_seconds);
    }

    /** @return Value of enty */
    public VALUETYPE getValue() {
        return value;
    }

    /** @return Chache header info: Time stamp, expiration time */
    public CacheHeader getCacheHeader() {
        return header;
    }

    /** @return <code>true</code> if entry is still valid */
    public boolean isStillValid() {
        var now = new Date();
        return now.before(header.getExpirationDate());
    }
}
