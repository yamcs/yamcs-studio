package org.csstudio.opibuilder.preferences;

import java.util.List;

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

    private ColorSelector rgbSelector;

    private String name;
    private RGB rgb;

    private List<String> existingNames;

    public NamedColorDialog(Shell parentShell, NamedColor color, List<String> existingNames) {
        super(parentShell);
        if (color != null) {
            name = color.name;
            rgb = color.rgb;
        } else {
            name = "";
            rgb = JFaceColors.getInformationViewerBackgroundColor(parentShell.getDisplay()).getRGB();
        }
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

        var noName = name == null;
        var title = noName ? "Add Color" : "Edit Color";

        getShell().setText(title);
        setTitle(title);

        setMessage(noName ? "Enter color details." : "Edit color details.");

        fieldArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        var gl = new GridLayout(2, false);
        fieldArea.setLayout(gl);

        var label = new Label(fieldArea, SWT.NONE);
        label.setText("Name:");
        label.setLayoutData(new GridData());
        nameText = new Text(fieldArea, SWT.BORDER);
        if (name != null) {
            nameText.setText(name);
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

        Dialog.applyDialogFont(fieldArea);

        return fieldArea;
    }

    private void updatePageComplete() {
        var text = nameText.getText() == null ? "" : nameText.getText().trim();
        if (text.equals("")) {
            setErrorMessage("Name is required");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else if (existingNames.contains(text)) {
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
