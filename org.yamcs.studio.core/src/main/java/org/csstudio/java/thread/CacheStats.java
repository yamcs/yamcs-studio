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

/**
 * Cache statistics
 */
public class CacheStats {
    final long hits, misses, expirations, total;

    /**
     * Initialize
     *
     * @param hits
     * @param misses
     * @param expirations
     */
    CacheStats(long hits, long misses, long expirations) {
        this.hits = hits;
        this.misses = misses;
        this.expirations = expirations;
        total = hits + misses + expirations;
    }

    /** @return hits */
    public long getHits() {
        return hits;
    }

    /** @return misses */
    public long getMisses() {
        return misses;
    }

    /** @return expirations */
    public long getExpirations() {
        return expirations;
    }

    /** @return total number of cache accesses */
    public long getTotal() {
        return total;
    }

    /** @return Info text suitable for display on web page */
    @Override
    public String toString() {
        if (total <= 0) {
            return "Never used";
        }
        var buf = new StringBuilder();
        buf.append("Cache hits=").append(hits).append(" (").append(hits * 100 / total).append("%), ");
        buf.append("misses=").append(misses).append(" (").append(misses * 100 / total).append("%), ");
        buf.append("expirations=").append(expirations).append(" (").append(expirations * 100 / total).append("%)");
        return buf.toString();
    }
}
