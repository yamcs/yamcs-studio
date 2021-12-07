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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

public class EventLogView extends ViewPart {

    private EventLog eventlog;

    @Override
    public void createPartControl(Composite parent) {
        eventlog = new EventLog(parent, SWT.NONE);
        eventlog.setLayoutData(new GridData(GridData.FILL_BOTH));

        eventlog.attachToSite(getViewSite());
        createActions(getSite().getShell());
    }

    private void createActions(Shell shell) {
        var bars = getViewSite().getActionBars();
        var mgr = bars.getMenuManager();

        Action configureColumnsAction = new Action("Configure Columns...", IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                eventlog.openConfigureColumnsDialog(shell);
            }
        };
        mgr.add(configureColumnsAction);
    }

    @Override
    public void setFocus() {
        eventlog.setFocus();
    }

    public EventLog getEventLog() {
        return eventlog;
    }

    @Override
    public void dispose() {
        eventlog.dispose();
        super.dispose();
    }
}
