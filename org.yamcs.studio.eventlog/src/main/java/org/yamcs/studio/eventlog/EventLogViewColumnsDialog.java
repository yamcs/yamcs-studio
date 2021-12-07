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

import org.eclipse.swt.widgets.Shell;
import org.yamcs.studio.core.utils.ColumnData;
import org.yamcs.studio.core.utils.ViewerColumnsDialog;

public class EventLogViewColumnsDialog extends ViewerColumnsDialog {

    private EventLogTableViewer viewer;

    public EventLogViewColumnsDialog(Shell parentShell, EventLogTableViewer viewer, ColumnData columnData) {
        super(parentShell, columnData);
        this.viewer = viewer;
    }

    @Override
    protected void performDefaults() {
        var defaultData = viewer.createDefaultColumnData();

        getVisible().clear();
        getVisible().addAll(defaultData.getVisibleColumns());

        getNonVisible().clear();
        getNonVisible().addAll(defaultData.getHiddenColumns());

        super.performDefaults();
    }
}
