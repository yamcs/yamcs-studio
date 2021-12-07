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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;

public class EventLogSeverityFilter extends ViewerFilter {

    private EventSeverity minimumSeverity = EventSeverity.INFO;

    public void setMinimumSeverity(EventSeverity minimumSeverity) {
        this.minimumSeverity = minimumSeverity;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof EventLogItem) {
            var event = ((EventLogItem) element).event;
            switch (minimumSeverity) {
            case INFO:
                return true;
            case WATCH:
                if (event.getSeverity() == EventSeverity.WATCH) {
                    return true;
                }
                // fall
            case WARNING:
                if (event.getSeverity() == EventSeverity.WARNING) {
                    return true;
                }
                // fall
            case DISTRESS:
                if (event.getSeverity() == EventSeverity.DISTRESS) {
                    return true;
                }
                // fall
            case CRITICAL:
                if (event.getSeverity() == EventSeverity.CRITICAL) {
                    return true;
                }
                // fall
            case SEVERE:
            case ERROR:
                if (event.getSeverity() == EventSeverity.SEVERE || event.getSeverity() == EventSeverity.ERROR) {
                    return true;
                }
            }
        }
        return false;
    }
}
