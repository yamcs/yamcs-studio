/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.next;

import java.time.Instant;

/**
 * Immutable Time implementation.
 *
 * @author carcassi
 */
class ITime extends Time {

    private final Instant timestamp;
    private final Integer userTag;
    private final boolean valid;

    ITime(Instant timestamp, Integer userTag, boolean valid) {
        this.timestamp = timestamp;
        this.userTag = userTag;
        this.valid = valid;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Integer getUserTag() {
        return userTag;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

}
