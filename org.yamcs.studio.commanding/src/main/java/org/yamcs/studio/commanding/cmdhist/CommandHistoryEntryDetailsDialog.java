/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.yamcs.client.Acknowledgment;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.yamcs.StringConverter;

public class CommandHistoryEntryDetailsDialog extends TrayDialog {

    // Pattern that we use to find the server name inside a cascading prefix
    private static final Pattern CASCADING_PREFIX = Pattern.compile("yamcs<([^>]+)>_");

    private Label recordLabel;
    private Combo recordCombo;
    private String[] recordComboPrefixes; // Array with same size as combo items

    private Text originLabel;
    private Text dateLabel;
    private Text commandLabel;
    private Text userLabel;
    private Label completedImageLabel;
    private Label completedLabel;
    private Text binaryLabel;
    private Text commentLabel;
    private Text argumentsText;

    private Button prevButton;
    private Button nextButton;

    private CommandHistoryView commandHistoryView;

    private CommandHistoryRecord rec;
    private CommandHistoryRecord previousRec;
    private CommandHistoryRecord nextRec;

    private AckTableViewer localAckTableViewer;
    private AckTableViewer extraAckTableViewer;

    // If not null, the information shown is as coming from an upstream
    // (cascaded) server.
    private String cascadingPrefix;

    public CommandHistoryEntryDetailsDialog(Shell shell, CommandHistoryView commandHistoryView,
            CommandHistoryRecord rec) {
        super(shell);
        setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
        this.commandHistoryView = commandHistoryView;
        updateRecord(rec);
    }

    private void updateRecord(CommandHistoryRecord rec) {
        this.rec = rec;
        previousRec = commandHistoryView.getPreviousRecord(rec);
        nextRec = commandHistoryView.getNextRecord(rec);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Command Details");
    }

    @Override
    public void create() {
        super.create();
        getShell().setSize(700, 550);

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
        commandHistoryView.getTableViewer().setSelection(sel, true);
        updateRecord(previousRec);
        updateProperties();
        updateButtonState();
    }

    private void nextPressed() {
        IStructuredSelection sel = new StructuredSelection(nextRec);
        commandHistoryView.getTableViewer().setSelection(sel, true);
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
        var gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);
        container.setLayout(new FillLayout());

        var tabFolder = new TabFolder(container, SWT.NONE);

        var generalTab = new TabItem(tabFolder, SWT.NONE);
        generalTab.setText("General");
        var generalControl = createDetailsSection(tabFolder);
        generalTab.setControl(generalControl);

        var binaryTab = new TabItem(tabFolder, SWT.NONE);
        binaryTab.setText("Binary");
        var binaryControl = createBinarySection(tabFolder);
        binaryTab.setControl(binaryControl);

        var ackTab = new TabItem(tabFolder, SWT.NONE);
        ackTab.setText("Acknowledgments");
        var ackControl = createAckSection(tabFolder);
        ackTab.setControl(ackControl);

        updateProperties();
        updateButtonState();

        Dialog.applyDialogFont(container);
        return container;
    }

    private Control createDetailsSection(Composite parent) {
        var container = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);
        var data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 200;
        container.setLayoutData(data);

        createTextSection(container);
        createToolbarButtonBar(container);
        return container;
    }

    private Control createBinarySection(Composite parent) {
        binaryLabel = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        binaryLabel.setEditable(false);
        return binaryLabel;
    }

    private Control createAckSection(Composite parent) {
        var ackContainer = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        ackContainer.setLayout(layout);
        ackContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        var ackLabel = new Label(ackContainer, SWT.NONE);
        ackLabel.setText("Yamcs acknowledgments:");
        createLocalAckTable(ackContainer);

        ackLabel = new Label(ackContainer, SWT.NONE);
        ackLabel.setText("Extra acknowledgments:");
        createExtraAckTable(ackContainer);
        return ackContainer;
    }

    private void createLocalAckTable(Composite parent) {
        var tableContainer = new Composite(parent, SWT.BORDER);
        tableContainer.setLayout(new FillLayout());
        tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        localAckTableViewer = new AckTableViewer(tableContainer, commandHistoryView);
    }

    private void createExtraAckTable(Composite parent) {
        var tableContainer = new Composite(parent, SWT.BORDER);
        tableContainer.setLayout(new FillLayout());
        tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        extraAckTableViewer = new AckTableViewer(tableContainer, commandHistoryView);
    }

    private void createTextSection(Composite parent) {
        var textContainer = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = layout.marginWidth = 0;
        textContainer.setLayout(layout);
        textContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        recordLabel = new Label(textContainer, SWT.NONE);
        recordLabel.setText("Record");
        recordLabel.setLayoutData(new GridData() /* keep, we use it to exclude */);
        recordCombo = new Combo(textContainer, SWT.READ_ONLY);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        recordCombo.setLayoutData(gd);
        recordCombo.addListener(SWT.Selection, e -> {
            cascadingPrefix = recordComboPrefixes[recordCombo.getSelectionIndex()];
            updateProperties();
        });

        var label = new Label(textContainer, SWT.NONE);
        label.setText("Time");
        dateLabel = new Text(textContainer, SWT.BORDER);
        dateLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dateLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Command");
        commandLabel = new Text(textContainer, SWT.BORDER);
        commandLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        commandLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Completion");
        completedImageLabel = new Label(textContainer, SWT.NONE);
        completedLabel = new Label(textContainer, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        completedLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("User");
        userLabel = new Text(textContainer, SWT.BORDER);
        userLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        userLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Origin");
        originLabel = new Text(textContainer, SWT.BORDER);
        originLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        originLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Comment");
        commentLabel = new Text(textContainer, SWT.BORDER);
        commentLabel.setEditable(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        commentLabel.setLayoutData(gd);

        label = new Label(textContainer, SWT.NONE);
        label.setText("Arguments");
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
        label.setLayoutData(gd);
        argumentsText = new Text(textContainer, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
        argumentsText.setEditable(false);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.grabExcessVerticalSpace = true;
        argumentsText.setLayoutData(gd);
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

        prevButton = createButton(container, IDialogConstants.BACK_ID, "", false);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        prevButton.setLayoutData(gd);
        prevButton.setToolTipText("Previous Entry");
        prevButton.setImage(commandHistoryView.prevImage);

        nextButton = createButton(container, IDialogConstants.NEXT_ID, "", false);
        gd = new GridData();
        nextButton.setLayoutData(gd);
        nextButton.setToolTipText("Next Entry");
        nextButton.setImage(commandHistoryView.nextImage);

        layout.numColumns = 1;
    }

    private void updateProperties() {
        var command = rec.getCommand();

        var cascadingPrefixMatched = false;
        if (cascadingPrefix != null) {
            for (var entry : command.getCascadedRecords().entrySet()) {
                if (entry.getKey().equals(cascadingPrefix)) {
                    command = entry.getValue();
                    cascadingPrefixMatched = true;
                    break;
                }
            }
        }

        // Not every record necessarily has the same prefixes.
        // Revert back to the local record.
        if (cascadingPrefix != null && !cascadingPrefixMatched) {
            cascadingPrefix = null;
            updateProperties();
            return;
        }

        var prefixes = new ArrayList<String>();
        prefixes.add(null);

        var items = new ArrayList<String>();
        items.add("Local");

        for (var entry : rec.getCommand().getCascadedRecords().entrySet()) {
            var prefix = entry.getKey();
            var serverName = getLabelForCascadingPrefix(prefix);
            prefixes.add(prefix);
            items.add(serverName);
        }
        recordCombo.setItems(items.toArray(new String[0]));
        recordComboPrefixes = prefixes.toArray(new String[0]);

        var shouldSelectIdx = prefixes.indexOf(cascadingPrefix);
        recordCombo.select(shouldSelectIdx);

        var showRecordSelector = items.size() > 1;

        // Most setups will not have cascaded command records, so prefer
        // to hide it when possible.
        ((GridData) recordLabel.getLayoutData()).exclude = !showRecordSelector;
        ((GridData) recordCombo.getLayoutData()).exclude = !showRecordSelector;
        recordLabel.setVisible(showRecordSelector);
        recordCombo.setVisible(showRecordSelector);
        recordCombo.getParent().requestLayout();

        dateLabel.setText(YamcsPlugin.getDefault().formatInstant(command.getGenerationTime()));
        commandLabel.setText(rec.getDisplayedName());
        argumentsText.setText(rec.printArguments());

        userLabel.setText(command.getUsername());

        if (command.getOrigin() != null && !"".equals(command.getOrigin())) {
            originLabel.setText(command.getOrigin());
        } else {
            originLabel.setText("");
        }

        if (command.getComment() != null) {
            commentLabel.setText(command.getComment());
        } else {
            commentLabel.setText("");
        }

        if (command.isSuccess()) {
            completedImageLabel.setImage(commandHistoryView.checkmarkImage);
            completedLabel.setText("Completed");
        } else if (command.isFailure()) {
            completedImageLabel.setImage(commandHistoryView.errorImage);
            completedLabel.setText(command.getError());
        } else {
            completedImageLabel.setImage(null);
            completedLabel.setText("");
        }

        if (command.getBinary() != null) {
            var hexString = StringConverter.arrayToHexString(command.getBinary(), true);
            binaryLabel.setText(hexString);
        } else {
            binaryLabel.setText("");
        }

        var localAcks = command.getAcknowledgments().values().stream()
                .filter(Acknowledgment::isLocal)
                .map(ack -> new AckTableRecord(ack, rec))
                .toList();
        localAckTableViewer.setInput(localAcks.toArray());

        var extraAcks = command.getAcknowledgments().values().stream()
                .filter(ack -> !ack.isLocal())
                .map(ack -> new AckTableRecord(ack, rec))
                .toList();
        extraAckTableViewer.setInput(extraAcks.toArray());
    }

    private void updateButtonState() {
        prevButton.setEnabled(previousRec != null);
        nextButton.setEnabled(nextRec != null);
    }

    private String getLabelForCascadingPrefix(String cascadingPrefix) {
        var matcher = CASCADING_PREFIX.matcher(cascadingPrefix);
        var servers = new ArrayList<String>(2);
        while (matcher.find()) {
            servers.add(matcher.group(1));
        }
        String label;
        var lastServer = servers.get(servers.size() - 1);
        if (servers.size() == 1) {
            label = lastServer;
        } else {
            Collections.reverse(servers); // From furthest to closest
            servers.remove(0); // Furthest server, where the info really comes from
            var otherServers = servers.stream().collect(Collectors.joining(" and "));
            label = lastServer + " over " + otherServers;
        }

        return label;
    }
}
