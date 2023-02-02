/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.preferences;

import java.util.List;

import org.csstudio.opibuilder.util.AlarmRepresentationScheme;
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

public class NamedColorDialog extends TitleAreaDialog {

    private Text nameText;
    private boolean reservedMode;
    private boolean editMode;

    private ColorSelector rgbSelector;

    private String name;
    private RGB rgb;

    private List<String> existingNames;

    public NamedColorDialog(Shell parentShell, NamedColor color, List<String> existingNames) {
        super(parentShell);
        if (color != null) {
            name = color.name;
            rgb = color.rgb;
            reservedMode = AlarmRepresentationScheme.isReservedColor(color.name);
        } else {
            name = "";
            rgb = JFaceColors.getInformationViewerBackgroundColor(parentShell.getDisplay()).getRGB();
            reservedMode = false;
        }
        editMode = name != null && !name.isEmpty();
        this.existingNames = existingNames;
        setHelpAvailable(false);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var fieldArea = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);

        var title = editMode ? "Edit Color" : "Add Color";

        getShell().setText(title);
        setTitle(title);

        setMessage(editMode ? "Edit color details." : "Enter color details.");

        fieldArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        var gl = new GridLayout(2, false);
        fieldArea.setLayout(gl);

        var label = new Label(fieldArea, SWT.NONE);
        label.setText("Name:");
        label.setLayoutData(new GridData());

        var textOptions = SWT.BORDER;
        if (editMode && reservedMode) {
            textOptions |= SWT.READ_ONLY;
        }

        nameText = new Text(fieldArea, textOptions);
        if (name != null) {
            nameText.setText(name);
        }
        if (editMode && reservedMode) {
            nameText.setEditable(false);
            nameText.setEnabled(false);
        }
        nameText.setLayoutData(new GridData());
        nameText.addModifyListener(e -> updatePageComplete());
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(fieldArea, SWT.NONE);
        label.setText("Color:");
        label.setLayoutData(new GridData());
        rgbSelector = new ColorSelector(fieldArea);
        rgbSelector.setColorValue(rgb);
        var backgroundColorButton = rgbSelector.getButton();
        backgroundColorButton.setLayoutData(new GridData());
        rgbSelector.addListener(evt -> updatePageComplete());

        Dialog.applyDialogFont(fieldArea);

        return fieldArea;
    }

    private void updatePageComplete() {
        var text = nameText.getText() == null ? "" : nameText.getText().trim();
        if (text.equals("")) {
            setErrorMessage("Name is required");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else if (!reservedMode && existingNames.contains(text)) {
            setErrorMessage("Name already in use");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else if (text.contains(";")) {
            setErrorMessage("Name contains invalid character ;");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else if (text.contains("@")) {
            setErrorMessage("Name contains invalid character @");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else {
            setErrorMessage(null);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    @Override
    protected void okPressed() {
        name = nameText.getText();
        rgb = rgbSelector.getColorValue();
        super.okPressed();
    }

    public NamedColor getColor() {
        return new NamedColor(name, rgb);
    }
}
