/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import java.time.Instant;
import java.util.Date;

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
import org.yamcs.studio.core.YamcsPlugin;

public class JumpToDialog extends TitleAreaDialog {

    private CDateTime date;

    private Instant selectedTime;

    public JumpToDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Jump to a specific time");
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
        lbl.setText("Time:");
        date = new CDateTime(container,
                SWT.BORDER | CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);

        var missionTime = YamcsPlugin.getMissionTime();
        if (missionTime != null) {
            date.setSelection(Date.from(missionTime));
        }

        return container;
    }

    @Override
    protected void okPressed() {
        selectedTime = date.getSelection().toInstant();
        super.okPressed();
    }

    public Instant getTime() {
        return selectedTime;
    }
}
