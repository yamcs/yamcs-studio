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

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class EventLogSearchBoxFilter extends ViewerFilter {

    private Pattern pattern = Pattern.compile(".*");

    public void setSearchTerm(String searchTerm) {
        pattern = Pattern.compile("(?i:.*" + searchTerm + ".*)");
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof EventLogItem) {
            var event = ((EventLogItem) element).event;
            if (pattern.matcher(event.getMessage()).matches()
                    || (event.hasType() && pattern.matcher(event.getType()).matches())
                    || (event.hasSource() && pattern.matcher(event.getSource()).matches())) {
                return true;
            }
        }
        return false;
    }
}
