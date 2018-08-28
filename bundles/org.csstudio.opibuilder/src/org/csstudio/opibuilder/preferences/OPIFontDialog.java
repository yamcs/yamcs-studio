package org.csstudio.opibuilder.preferences;

import java.util.List;

import org.csstudio.opibuilder.util.OPIFont;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OPIFontDialog extends TitleAreaDialog {

    private Text nameText;
    private Text fontText;

    private String name;
    private FontData fontData;

    private List<String> existingNames;

    public OPIFontDialog(Shell parentShell, OPIFont font, List<String> existingNames) {
        super(parentShell);
        if (font != null) {
            name = font.getFontMacroName();
            fontData = font.getFontData();
        } else {
            name = "";
        }
        this.existingNames = existingNames;
        setHelpAvailable(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite fieldArea = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);

        boolean noName = name == null;
        String title = noName ? "Add Font" : "Edit Font";

        getShell().setText(title);
        setTitle(title);

        setMessage(noName ? "Enter font details." : "Edit font details.");

        fieldArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout gl = new GridLayout(2, false);
        fieldArea.setLayout(gl);

        Label label = new Label(fieldArea, SWT.NONE);
        label.setText("Name:");
        label.setLayoutData(new GridData());
        nameText = new Text(fieldArea, SWT.BORDER);
        if (name != null) {
            nameText.setText(name);
        }
        nameText.addModifyListener(e -> updatePageComplete());
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        label = new Label(fieldArea, SWT.NONE);
        label.setText("Font:");
        label.setLayoutData(new GridData());

        Composite fontSelector = new Composite(fieldArea, SWT.NONE);
        fontSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        fontSelector.setLayout(gl);

        fontText = new Text(fontSelector, SWT.BORDER | SWT.READ_ONLY);
        if (fontData != null) {
            fontText.setText(StringConverter.asString(fontData));
        }
        fontText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button browseButton = new Button(fontSelector, SWT.PUSH);
        browseButton.setText("Browse...");

        Listener browseListener = evt -> {
            FontDialog dialog = new FontDialog(getParentShell());
            dialog.setFontList(new FontData[] { fontData });
            FontData fontData = dialog.open();
            if (fontData != null) {
                this.fontData = fontData;
                fontText.setText(StringConverter.asString(fontData));
            }
            updatePageComplete();
        };

        fontText.addListener(SWT.MouseDoubleClick, browseListener);
        browseButton.addListener(SWT.Selection, browseListener);

        Dialog.applyDialogFont(fieldArea);

        return fieldArea;
    }

    private void updatePageComplete() {
        String text = nameText.getText() == null ? "" : nameText.getText().trim();
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
        } else if (fontData == null) {
            setErrorMessage("Font is required");
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        } else {
            setErrorMessage(null);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        name = nameText.getText();
        super.okPressed();
    }

    public OPIFont getFont() {
        return new OPIFont(name, fontData);
    }
}
