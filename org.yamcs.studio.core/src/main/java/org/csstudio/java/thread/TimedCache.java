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

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe Cache for anything that times out after some time.
 */
public class TimedCache<KEYTYPE, VALUETYPE> {
    /**
     * Map KEY to { VALUE, Date when put into map } KEYTYPE can be any type, VALUETYPE can be any type (types determined
     * when class instance created)
     */
    final private Map<KEYTYPE, TimedCacheEntry<VALUETYPE>> map = new HashMap<>();

    /** How long items are considered 'valid' in seconds */
    final private long timeout_secs;

    /** Number of successful cache hists */
    private long hits = 0;

    /** Number of failed cache hists */
    private long misses = 0;

    /** Number of entries that expired */
    private long exirations = 0;

    /**
     * Initialize cache
     *
     * @param timeout_secs
     *            How long items are considered 'valid' in seconds
     */
    public TimedCache(long timeout_secs) {
        this.timeout_secs = timeout_secs;
    }

    /** @return Cache statistics */
    public synchronized CacheStats getCacheStats() {
        return new CacheStats(hits, misses, exirations);
    }

    /**
     * Get entry from cache
     *
     * @param key
     * @return Cached entry or <code>null</code> when not found or timed out
     */
    public synchronized TimedCacheEntry<VALUETYPE> getEntry(KEYTYPE key) {
        var entry = map.get(key);
        // Is there a matching entry?
        if (entry == null) {
            ++misses;
            return null;
        }
        // Is it still valid?
        if (entry.isStillValid()) {
            ++hits;
            return entry;
        }
        // Value is too old:
        ++exirations;
        map.remove(key);
        return null;
    }

    /**
     * Get value of entry from cache
     *
     * @param key
     * @return Cached value or <code>null</code> when not found or timed out
     */
    public VALUETYPE getValue(KEYTYPE key) {
        var entry = getEntry(key);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    /**
     * Add item to cache
     *
     * @param key
     * @param value
     * @return Cache entry
     */
    public synchronized TimedCacheEntry<VALUETYPE> remember(KEYTYPE key, VALUETYPE value) {
        var entry = new TimedCacheEntry<>(value, timeout_secs);
        map.put(key, entry);
        return entry;
    }

    /** Use if need to get rid of all expired cache entries */
    public synchronized void cleanup() {
        var keys = map.keySet().iterator();
        while (keys.hasNext()) {
            var key = keys.next();
            if (!map.get(key).isStillValid()) {
                map.remove(key);
            }
        }
    }
}
