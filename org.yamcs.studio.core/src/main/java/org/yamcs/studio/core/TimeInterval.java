/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Time interval where both ends can be open
 */
public class TimeInterval implements Serializable {
    private static final long serialVersionUID = 1L;

    private Instant start;
    private Instant stop;

    public TimeInterval(Instant start, Instant stop) {
        setStart(start);
        setStop(stop);
    }

    /**
     * Creates a TimeInterval with no ends
     */
    public TimeInterval() {
    }

    public static TimeInterval starting(Instant start) {
        var range = new TimeInterval();
        range.setStart(start);
        return range;
    }

    public boolean hasStart() {
        return start != null;
    }

    public boolean hasStop() {
        return stop != null;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getStart() {
        return start;
    }

    public void setStop(Instant stop) {
        this.stop = stop;
    }

    public Instant getStop() {
        return stop;
    }

    public Instant calculateStart() {
        if (start != null) {
            return start;
        } else {
            return YamcsPlugin.getMissionTime(true).truncatedTo(ChronoUnit.DAYS);
        }
    }

    public Instant calculateStop() {
        if (stop != null) {
            return stop;
        } else {
            return YamcsPlugin.getMissionTime(true).plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimeInterval)) {
            return false;
        }
        var other = (TimeInterval) obj;
        return Objects.equals(start, other.start) && Objects.equals(stop, other.stop);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("(");
        if (start != null) {
            sb.append(start);
        }
        sb.append(",");
        if (stop != null) {
            sb.append(stop);
        }
        sb.append(")");
        return sb.toString();
    }
}
