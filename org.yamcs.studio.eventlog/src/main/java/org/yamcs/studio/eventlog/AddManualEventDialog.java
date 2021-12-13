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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.CreateEventRequest;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.protobuf.Timestamp;

public class AddManualEventDialog extends TitleAreaDialog {

    private Calendar generationTimeValue = null;

    private Text messageText;
    private CDateTime generationDatePicker;
    private Combo severityCombo;

    protected AddManualEventDialog(Shell shell) {
        super(shell);
    }

    protected AddManualEventDialog(Shell shell, Instant generationTime) {
        super(shell);
        var zdt = ZonedDateTime.ofInstant(generationTime, YamcsPlugin.getZoneId());
        generationTimeValue = GregorianCalendar.from(zdt);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add a Manual Event");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var area = (Composite) super.createDialogArea(parent);
        var container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        var layout = new GridLayout(2, false);
        layout.marginHeight = 20;
        layout.marginWidth = 20;

        layout.verticalSpacing = 5;
        container.setLayout(layout);

        var lbl = new Label(container, SWT.NONE);
        lbl.setText("Message");
        var gd = new GridData(GridData.FILL_VERTICAL);
        lbl.setLayoutData(gd);
        gd.verticalAlignment = SWT.TOP;
        messageText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.TOP;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        messageText.setLayoutData(gd);
        var gc = new GC(messageText);
        try {
            gc.setFont(messageText.getFont());
            var fm = gc.getFontMetrics();
            gd.heightHint = 5 * fm.getHeight();
        } finally {
            gc.dispose();
        }
        messageText.setText("");

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Event Time");
        generationDatePicker = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        if (generationTimeValue != null) {
            generationDatePicker.setSelection(generationTimeValue.getTime());
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Severity");
        severityCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        severityCombo.add(EventSeverity.INFO.name());
        severityCombo.add(EventSeverity.WATCH.name());
        severityCombo.add(EventSeverity.WARNING.name());
        severityCombo.add(EventSeverity.DISTRESS.name());
        severityCombo.add(EventSeverity.CRITICAL.name());
        severityCombo.add(EventSeverity.SEVERE.name());
        severityCombo.select(0);

        return container;
    }

    @Override
    protected void okPressed() {
        var message = messageText.getText();
        Instant time = null;
        if (generationDatePicker.hasSelection()) {
            time = generationDatePicker.getSelection().toInstant();
        }
        var severityString = severityCombo.getItem(severityCombo.getSelectionIndex());
        var severity = EventSeverity.valueOf(severityString);

        var client = YamcsPlugin.getYamcsClient();

        var requestb = CreateEventRequest.newBuilder();
        requestb.setInstance(YamcsPlugin.getInstance());
        requestb.setMessage(message);
        requestb.setSeverity(severity.toString());

        if (time != null) {
            var t = time.atOffset(ZoneOffset.UTC);
            requestb.setTime(Timestamp.newBuilder().setSeconds(t.toEpochSecond()).setNanos(t.getNano()));
        }

        client.createEvent(requestb.build()).whenComplete((data, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(this::close);
            } else {
                Display.getDefault().asyncExec(() -> {
                    var m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL);
                    m.setText("Error");
                    m.setMessage(exc.getMessage());
                    m.open();
                });
            }
        });
    }
}
