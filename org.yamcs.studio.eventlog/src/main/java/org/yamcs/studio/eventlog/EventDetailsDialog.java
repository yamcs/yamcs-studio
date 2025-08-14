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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.protobuf.Event.EventSeverity;
import org.yamcs.studio.core.YamcsPlugin;

public class EventDetailsDialog extends TrayDialog {

    private StyledText messageText;

    private Label severityLabel;
    private Label severityImageLabel;
    private Label typeLabel;
    private Label sourceLabel;
    private Label generationTimeLabel;
    private Label receptionTimeLabel;
    private Label sequenceNumberLabel;

    private Button prevButton;
    private Button nextButton;

    private EventLog eventLog;

    private EventLogItem rec;
    private EventLogItem previousRec;
    private EventLogItem nextRec;

    private Image infoIcon;
    private Image watchIcon;
    private Image warningIcon;
    private Image distressIcon;
    private Image criticalIcon;
    private Image severeIcon;

    private ResourceManager resourceManager;

    public EventDetailsDialog(Shell parentShell, EventLog eventLog, EventLogItem rec) {
        super(parentShell);
        this.eventLog = eventLog;
        updateRecord(rec);
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        var plugin = EventLogPlugin.getDefault();
        infoIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level0s.png"));
        watchIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level1s.png"));
        warningIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level2s.png"));
        distressIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level3s.png"));
        criticalIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level4s.png"));
        severeIcon = resourceManager.create(plugin.getImageDescriptor("icons/eview16/level5s.png"));
    }

    private void updateRecord(EventLogItem rec) {
        this.rec = rec;
        previousRec = eventLog.getPreviousRecord(rec);
        nextRec = eventLog.getNextRecord(rec);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Event Details");
    }

    @Override
    public void create() {
        super.create();
        // getShell().setSize(600, 550);

        applyDialogFont(buttonBar);
        getButton(IDialogConstants.OK_ID).setFocus();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (IDialogConstants.OK_ID == buttonId) {
            okPressed();
        } else if (IDialogConstants.CANCEL_ID == buttonId) {
            cancelPressed();
        } else if (IDialogConstants.BACK_ID == buttonId) {
            backPressed();
        } else if (IDialogConstants.NEXT_ID == buttonId) {
            nextPressed();
        }
    }

    private void backPressed() {
        IStructuredSelection sel = new StructuredSelection(previousRec);
        eventLog.getTableViewer().setSelection(sel, true);
        updateRecord(previousRec);
        updateProperties();
        updateButtonState();
    }

    private void nextPressed() {
        IStructuredSelection sel = new StructuredSelection(nextRec);
        eventLog.getTableViewer().setSelection(sel, true);
        updateRecord(nextRec);
        updateProperties();
        updateButtonState();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var container = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        var gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        createDetailsSection(container);

        updateProperties();
        updateButtonState();

        Dialog.applyDialogFont(container);
        return container;
    }

    private void createDetailsSection(Composite parent) {
        var container = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        layout.numColumns = 2;
        container.setLayout(layout);
        var data = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(data);

        createTextSection(container);
        createToolbarButtonBar(container);
    }

    private void createTextSection(Composite parent) {
        var textContainer = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = layout.marginWidth = 0;
        textContainer.setLayout(layout);
        textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        var label = new Label(textContainer, SWT.NONE);
        label.setText("Severity:");
        severityImageLabel = new Label(textContainer, SWT.NONE);
        severityLabel = new Label(textContainer, SWT.NONE);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        severityLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Generation Time:");
        generationTimeLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        generationTimeLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Reception Time:");
        receptionTimeLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        receptionTimeLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Source:");
        sourceLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        sourceLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Type:");
        typeLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        typeLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Sequence Number:");
        sequenceNumberLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        sequenceNumberLabel.setLayoutData(gd);

        var terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Message:");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);
        messageText = new StyledText(textContainer, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        messageText.setAlwaysShowScrollBars(true);
        messageText.setEditable(false);
        messageText.setFont(terminalFont);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
        gd.widthHint = 400;
        gd.heightHint = 80;
        gd.horizontalSpan = 2;
        gd.grabExcessVerticalSpace = true;
        messageText.setLayoutData(gd);
    }

    private void createToolbarButtonBar(Composite parent) {
        var comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 0;
        // layout.numColumns = 1;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        ((GridData) comp.getLayoutData()).verticalAlignment = SWT.BOTTOM;

        var container = new Composite(comp, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        var plugin = EventLogPlugin.getDefault();

        prevButton = createButton(container, IDialogConstants.BACK_ID, "", false);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        prevButton.setLayoutData(gd);
        prevButton.setToolTipText("Previous Entry");
        prevButton.setImage(resourceManager.create(plugin.getImageDescriptor("icons/obj16/event_prev.png")));

        nextButton = createButton(container, IDialogConstants.NEXT_ID, "", false);
        gd = new GridData();
        nextButton.setLayoutData(gd);
        nextButton.setToolTipText("Next Entry");
        nextButton.setImage(resourceManager.create(plugin.getImageDescriptor("icons/obj16/event_next.png")));

        layout.numColumns = 1;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public void closeTray() throws IllegalStateException {
        super.closeTray();
        resourceManager.dispose();
    }

    private void updateProperties() {
        messageText.setText(rec.event.getMessage());
        sequenceNumberLabel.setText(String.valueOf(rec.event.getSeqNumber()));

        if (rec.event.hasType()) {
            typeLabel.setText(rec.event.getType());
        } else {
            typeLabel.setText("-");
        }

        if (rec.event.hasSource()) {
            sourceLabel.setText(rec.event.getSource());
        } else {
            sourceLabel.setText("-");
        }

        severityLabel.setText("-");
        severityImageLabel.setImage(null);
        if (rec.event.hasSeverity()) {
            var severity = rec.event.getSeverity();
            if (severity == EventSeverity.WARNING_NEW) {
                severity = EventSeverity.WARNING;
            }
            severityLabel.setText("" + severity);
            switch (severity) {
            case INFO:
                severityImageLabel.setImage(infoIcon);
                break;
            case WATCH:
                severityImageLabel.setImage(watchIcon);
                break;
            case WARNING:
                severityImageLabel.setImage(warningIcon);
                break;
            case DISTRESS:
                severityImageLabel.setImage(distressIcon);
                break;
            case CRITICAL:
                severityImageLabel.setImage(criticalIcon);
                break;
            case SEVERE:
            case ERROR:
                severityImageLabel.setImage(severeIcon);
                break;
            default:
                severityImageLabel.setImage(null);
            }
        }

        var uiPlugin = YamcsPlugin.getDefault();
        var generationTime = Instant.ofEpochSecond(rec.event.getGenerationTime().getSeconds(),
                rec.event.getGenerationTime().getNanos());
        generationTimeLabel.setText(uiPlugin.formatInstant(generationTime));
        var receptionTime = Instant.ofEpochSecond(rec.event.getReceptionTime().getSeconds(),
                rec.event.getReceptionTime().getNanos());
        receptionTimeLabel.setText(uiPlugin.formatInstant(receptionTime));
    }

    private void updateButtonState() {
        prevButton.setEnabled(previousRec != null);
        nextButton.setEnabled(nextRec != null);
    }
}
