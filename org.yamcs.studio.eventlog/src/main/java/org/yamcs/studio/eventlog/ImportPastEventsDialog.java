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

import java.time.Instant;
import java.util.Calendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ImportPastEventsDialog extends TitleAreaDialog {

    private CDateTime startDate;
    private Calendar startTimeValue;

    private CDateTime stopDate;
    private Calendar stopTimeValue;

    private Instant start;
    private Instant stop;

    public ImportPastEventsDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Import Past Events");
    }

    private void validate() {
        String errorMessage = null;
        var start = startDate.getSelection();
        var stop = stopDate.getSelection();
        if (start != null && stop != null && start.after(stop)) {
            errorMessage = "Stop has to be greater than start";
        }

        setErrorMessage(errorMessage);
        getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var area = (Composite) super.createDialogArea(parent);
        var container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        var layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;
        layout.verticalSpacing = 2;
        container.setLayout(layout);

        var lbl = new Label(container, SWT.NONE);
        lbl.setText("Start:");
        startDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        startDate.addListener(SWT.Selection, e -> validate());
        startDate.addListener(SWT.FocusOut, e -> validate());
        var gd = new GridData();
        gd.widthHint = 200;
        startDate.setLayoutData(gd);
        if (startTimeValue != null) {
            startDate.setSelection(startTimeValue.getTime());
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Stop:");
        stopDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        stopDate.addListener(SWT.Selection, e -> validate());
        stopDate.addListener(SWT.FocusOut, e -> validate());
        gd = new GridData();
        gd.widthHint = 200;
        stopDate.setLayoutData(gd);
        if (stopTimeValue != null) {
            stopDate.setSelection(stopTimeValue.getTime());
        }

        return container;
    }

    @Override
    protected void okPressed() {
        if (startDate.hasSelection()) {
            start = startDate.getSelection().toInstant();
        }
        if (stopDate.hasSelection()) {
            stop = stopDate.getSelection().toInstant();
        }
        super.okPressed();
    }

    public Instant getStart() {
        return start;
    }

    public Instant getStop() {
        return stop;
    }

    @Override
    public boolean close() {
        return super.close();
    }
}
