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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.protobuf.Yamcs.Event.EventSeverity;

import com.google.protobuf.Timestamp;

public class ColoringRuleDialog extends TitleAreaDialog {

    private Text expressionText;

    private ColorSelector bgSelector;
    private ColorSelector fgSelector;

    private String expression;
    private RGB bg;
    private RGB fg;

    public ColoringRuleDialog(Shell parentShell, ColoringRule rule) {
        super(parentShell);
        if (rule != null) {
            expression = rule.expression;
            bg = rule.bg;
            fg = rule.fg;
        } else {
            bg = JFaceColors.getInformationViewerBackgroundColor(parentShell.getDisplay()).getRGB();
            fg = JFaceColors.getInformationViewerForegroundColor(parentShell.getDisplay()).getRGB();
        }
        setHelpAvailable(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var fieldArea = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);

        var noExpression = expression == null;
        var title = noExpression ? "Add Coloring Rule" : "Edit Coloring Rule";

        getShell().setText(title);
        setTitle(title);

        setMessage(noExpression ? "Enter color rule details." : "Edit color rule details.");

        fieldArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        var gl = new GridLayout(2, false);
        fieldArea.setLayout(gl);

        var label = new Label(fieldArea, SWT.NONE);
        label.setText("Expression");
        label.setLayoutData(new GridData());
        expressionText = new Text(fieldArea, SWT.BORDER);
        if (expression != null) {
            expressionText.setText(expression);
        }
        expressionText.setLayoutData(new GridData());
        expressionText.addModifyListener(e -> updatePageComplete());
        expressionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(fieldArea, SWT.NONE);
        var example = new Composite(fieldArea, SWT.NONE);
        example.setLayoutData(new GridData(GridData.FILL_BOTH));
        gl = new GridLayout(2, false);
        example.setLayout(gl);
        label = new Label(example, SWT.NONE);
        label.setText("Example:");
        label = new Label(example, SWT.NONE);
        label.setText("severity == WARNING &&&& type == LVPDU");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(example, SWT.NONE);
        label = new Label(example, SWT.NONE);
        label.setText("Operator precedence: ==  !=  &&&&  ||");
        new Label(example, SWT.NONE);
        label = new Label(example, SWT.NONE);
        label.setText("Properties: severity  type  source");

        label = new Label(fieldArea, SWT.NONE);
        label.setText("Background Color");
        label.setLayoutData(new GridData());
        bgSelector = new ColorSelector(fieldArea);
        bgSelector.setColorValue(bg);
        var backgroundColorButton = bgSelector.getButton();
        backgroundColorButton.setLayoutData(new GridData());

        label = new Label(fieldArea, SWT.NONE);
        label.setText("Foreground Color");
        label.setLayoutData(new GridData());
        fgSelector = new ColorSelector(fieldArea);
        fgSelector.setColorValue(fg);
        var foregroundColorButton = fgSelector.getButton();
        foregroundColorButton.setLayoutData(new GridData());

        Dialog.applyDialogFont(fieldArea);

        return fieldArea;
    }

    private void updatePageComplete() {
        setErrorMessage(null);
        var hasError = false;

        try {
            var anyEvent = Event.newBuilder().setMessage("a message").setSource("a source")
                    .setGenerationTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                    .setReceptionTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()))
                    .setSeqNumber(123).setSeverity(EventSeverity.WARNING).setType("a type").build();
            var rule = new ColoringRule(expressionText.getText(), bg, fg);
            rule.matches(anyEvent);
        } catch (Exception e) {
            hasError = true;
            setErrorMessage("Invalid expression");
        } finally {
            getButton(IDialogConstants.OK_ID).setEnabled(!hasError);
        }
    }

    @Override
    protected void okPressed() {
        expression = expressionText.getText();
        bg = bgSelector.getColorValue();
        fg = fgSelector.getColorValue();
        super.okPressed();
    }

    public ColoringRule getRule() {
        return new ColoringRule(expression, bg, fg);
    }
}
