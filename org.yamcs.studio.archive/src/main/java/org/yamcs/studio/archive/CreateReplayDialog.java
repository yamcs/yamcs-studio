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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.CreateProcessorRequest;
import org.yamcs.studio.core.TimeInterval;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CreateReplayDialog extends TitleAreaDialog {

    private static final Logger log = Logger.getLogger(CreateReplayDialog.class.getName());

    // TODO look instead at current list of processors, and find something new
    private static AtomicInteger replayCounter = new AtomicInteger();

    private Text name;
    private String nameValue = "replay" + replayCounter.incrementAndGet();

    private CDateTime startDate;
    private Instant startTimeValue;

    private CDateTime stopDate;
    private Instant stopTimeValue;

    private Button stepByStepButton;

    private TableViewer ppTable;
    private List<String> ppValue;

    private CreateProcessorRequest request;

    public CreateReplayDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Start a new replay");
        // setMessage("Replays can be joined by other users", IMessageProvider.INFORMATION);
    }

    private void validate() {
        String errorMessage = null;
        var start = startDate.getSelection().toInstant();
        var stop = stopDate.getSelection().toInstant();
        if (start.isAfter(stop)) {
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
        if (startTimeValue != null) {
            startDate.setSelection(Date.from(startTimeValue));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Stop:");
        stopDate = new CDateTime(container,
                CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM | CDT.CLOCK_24_HOUR);
        stopDate.addListener(SWT.Selection, e -> validate());
        if (stopTimeValue != null) {
            stopDate.setSelection(Date.from(stopTimeValue));
        }

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Name:");
        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name.setText(nameValue);

        lbl = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.verticalAlignment = SWT.CENTER;
        gd.heightHint = 20;
        lbl.setLayoutData(gd);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Packets:");
        var includePackets = new Button(container, SWT.CHECK);
        includePackets.setText("All");
        includePackets.setLayoutData(new GridData());
        includePackets.setSelection(true);
        includePackets.setEnabled(false);

        lbl = new Label(container, SWT.NONE);
        lbl.setText("Parameter Groups:");
        gd = new GridData();
        gd.verticalAlignment = SWT.TOP;
        lbl.setLayoutData(gd);
        var tableWrapper = new Composite(container, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableWrapper.setLayout(gl);

        ppTable = CheckboxTableViewer.newCheckList(tableWrapper, SWT.V_SCROLL | SWT.BORDER);
        ppTable.setContentProvider(ArrayContentProvider.getInstance());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 5 * ppTable.getTable().getItemHeight();
        ppTable.getTable().setLayoutData(gd);
        ppTable.setInput(ppValue);
        for (TableItem item : ppTable.getTable().getItems()) {
            item.setChecked(false);
        }

        lbl = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalAlignment = SWT.CENTER;
        gd.heightHint = 20;
        gd.horizontalSpan = 2;
        lbl.setLayoutData(gd);

        stepByStepButton = new Button(container, SWT.CHECK);
        gd = new GridData();
        gd.horizontalSpan = 2;
        stepByStepButton.setLayoutData(gd);
        stepByStepButton.setText("Enable step-by-step mode (pauses after each data frame)");
        stepByStepButton.setSelection(false);

        return container;
    }

    @Override
    protected void okPressed() {
        getButton(IDialogConstants.OK_ID).setEnabled(false);

        request = toCreateProcessorRequest();
        var client = YamcsPlugin.getYamcsClient();
        client.createProcessor(request).whenComplete((processorClient, exc) -> {
            if (exc == null) {
                Display.getDefault().asyncExec(() -> {
                    CreateReplayDialog.super.okPressed();
                });
            } else {
                log.log(Level.SEVERE, "Could not start replay", exc);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Could not start replay",
                            exc.getMessage());
                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                });
            }
        });
    }

    public CreateProcessorRequest getRequest() {
        return request;
    }

    public void initialize(TimeInterval interval, List<String> pps) {
        startTimeValue = interval.calculateStart();
        stopTimeValue = interval.calculateStop();
        ppValue = pps;
    }

    private CreateProcessorRequest toCreateProcessorRequest() {
        var spec = new JsonObject();
        spec.addProperty("start", startDate.getSelection().toInstant().toString());
        spec.addProperty("stop", stopDate.getSelection().toInstant().toString());

        spec.add("packetRequest", new JsonObject());

        var ppFilters = new JsonArray();
        for (TableItem item : ppTable.getTable().getItems()) {
            if (item.getChecked()) {
                ppFilters.add(item.getText());
            }
        }

        if (ppFilters.size() > 0) {
            var ppObj = new JsonObject();
            ppObj.add("groupNameFilter", ppFilters);
            spec.add("ppRequest", ppObj);
        }

        if (stepByStepButton.getSelection()) {
            var speed = new JsonObject();
            speed.addProperty("type", "STEP_BY_STEP");
            spec.add("speed", speed);
        }

        var specJson = new Gson().toJson(spec);
        var resultb = CreateProcessorRequest.newBuilder().setInstance(YamcsPlugin.getInstance()).setName(name.getText())
                .setType("Archive").setPersistent(true) // TODO temp
                .setConfig(specJson);

        return resultb.build();
    }
}
