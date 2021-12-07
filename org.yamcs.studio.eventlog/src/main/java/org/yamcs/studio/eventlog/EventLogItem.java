/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.eventlog;

import java.util.List;
import java.util.Objects;

import org.eclipse.swt.graphics.RGB;
import org.yamcs.protobuf.Yamcs.Event;

public class EventLogItem {

    public final Event event;
    public RGB bg;
    public RGB fg;

    public EventLogItem(Event event) {
        this.event = event;
    }

    public void colorize(List<ColoringRule> rules) {
        for (ColoringRule rule : rules) {
            if (rule.matches(event)) {
                bg = rule.bg;
                fg = rule.fg;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventLogItem) || obj == null) {
            return false;
        }
        var other = ((EventLogItem) obj).event;
        return event.getSeqNumber() == other.getSeqNumber() && event.getGenerationTime() == other.getGenerationTime()
                && event.getSource().equals(other.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(event.getGenerationTime(), event.getSource(), event.getSeqNumber());
    }
}
