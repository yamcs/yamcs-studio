/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * The dialog for editing OPI Font.
 */
public class OPIFontDialog extends TrayDialog {

    /**
     * Private class to encapsulate logic for the pixel or points radio button group.
     */
    private class PixelsOrPointsBox {

        private final Button pixelsButton;
        private final Button pointsButton;
        private final Group radioGroup;

        public PixelsOrPointsBox(Composite parent, int style) {
            radioGroup = new Group(parent, style);
            radioGroup.setText("Font size");
            pointsButton = new Button(radioGroup, SWT.RADIO);
            pointsButton.setText("points");
            pixelsButton = new Button(radioGroup, SWT.RADIO);
            pixelsButton.setText("pixels");
            var layout = new GridLayout();
            layout.numColumns = 2;
            radioGroup.setLayout(layout);
            radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }

        public void setSizeInPixels(boolean isSizeInPixels) {
            pointsButton.setSelection(!isSizeInPixels);
            pixelsButton.setSelection(isSizeInPixels);
        }

        public void setEnabled(boolean enabled) {
            radioGroup.setEnabled(enabled);
            pixelsButton.setEnabled(enabled);
            pointsButton.setEnabled(enabled);
        }

        public boolean isSizeInPixels() {
            return pixelsButton.getSelection();
        }

        public void addSelectionListener(SelectionListener listener) {
            pixelsButton.addSelectionListener(listener);
            pointsButton.addSelectionListener(listener);
        }
    }

    private OPIFont opiFont;
    private TableViewer preDefinedFontsViewer;
    private PixelsOrPointsBox pixelsOrPointsBox;
    private Label outputTextLabel;
    private String title;

    protected OPIFontDialog(Shell parentShell, OPIFont font, String dialogTitle) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        title = dialogTitle;
        opiFont = new OPIFont(font);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parent_Composite = (Composite) super.createDialogArea(parent);

        var mainComposite = new Composite(parent_Composite, SWT.None);
        mainComposite.setLayout(new GridLayout(2, false));
        var gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 300;
        mainComposite.setLayoutData(gridData);
        var leftComposite = new Composite(mainComposite, SWT.None);
        leftComposite.setLayout(new GridLayout(1, false));
        var gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 250;
        leftComposite.setLayoutData(gd);
        createLabel(leftComposite, "Choose from Predefined Fonts:");

        preDefinedFontsViewer = createPredefinedFontsTableViewer(leftComposite);
        preDefinedFontsViewer.setInput(MediaService.getInstance().getAllPredefinedFonts());

        var rightComposite = new Composite(mainComposite, SWT.None);
        rightComposite.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 250;
        rightComposite.setLayoutData(gd);

        @SuppressWarnings("unused") // This label doesn't need to do anything but exist.
        var spacer = new Label(rightComposite, SWT.NONE);
        var fontDialogButton = new Button(rightComposite, SWT.PUSH);
        // Push radioButtons to bottom of rightComposite.
        var spacer2 = new Label(rightComposite, SWT.NONE);
        var gd2 = new GridData();
        gd2.grabExcessVerticalSpace = true;
        spacer2.setLayoutData(gd2);
        pixelsOrPointsBox = new PixelsOrPointsBox(rightComposite, SWT.NONE);
        pixelsOrPointsBox.setSizeInPixels(opiFont.isSizeInPixels());

        fontDialogButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        fontDialogButton.setText("Choose from Font Dialog");
        fontDialogButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var dialog = new FontDialog(Display.getCurrent().getActiveShell());
                dialog.setFontList(new FontData[] { opiFont.getRawFontData() });
                var fontdata = dialog.open();
                pixelsOrPointsBox.setEnabled(true);
                if (fontdata != null) {
                    opiFont = new OPIFont(fontdata);
                    opiFont.setSizeInPixels(pixelsOrPointsBox.isSizeInPixels());
                    preDefinedFontsViewer.setSelection(null);
                    outputTextLabel.setText(opiFont.getFontMacroName());
                    outputTextLabel.setFont(CustomMediaFactory.getInstance().getFont(opiFont.getFontData()));
                    getShell().layout(true, true);
                }
            }
        });

        SelectionListener radioSelectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                opiFont.setSizeInPixels(pixelsOrPointsBox.isSizeInPixels());
                outputTextLabel.setText(opiFont.getFontMacroName());
                outputTextLabel.setFont(CustomMediaFactory.getInstance().getFont(opiFont.getFontData()));
                getShell().layout(true, true);
            }
        };
        pixelsOrPointsBox.addSelectionListener(radioSelectionListener);

        var group = new Group(mainComposite, SWT.None);
        gd = new GridData(SWT.FILL, SWT.END, true, true, 2, 1);
        gd.heightHint = 100;
        group.setLayoutData(gd);

        group.setLayout(new GridLayout(1, false));
        group.setText("Output");

        outputTextLabel = new Label(group, SWT.None);
        outputTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        outputTextLabel.setText(opiFont.getFontMacroName());
        outputTextLabel.setFont(opiFont.getSWTFont());

        if (opiFont.isPreDefined()) {
            preDefinedFontsViewer.setSelection(new StructuredSelection(opiFont));
        } else {
            preDefinedFontsViewer.setSelection(null);
        }
        return parent_Composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        // this will help resolve a bug on GTK: The table widget in GTK
        // will force one item selected if it got the focus.
        getButton(IDialogConstants.OK_ID).setFocus();
    }

    private TableViewer createPredefinedFontsTableViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new BaseWorkbenchContentProvider() {
            @Override
            public Object[] getElements(Object element) {
                return (Object[]) element;
            }
        });
        viewer.setLabelProvider(new LabelProvider());
        viewer.addSelectionChangedListener(event -> refreshGUIOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.addDoubleClickListener(event -> okPressed());
        return viewer;
    }

    private void refreshGUIOnSelection() {
        var selection = (IStructuredSelection) preDefinedFontsViewer.getSelection();
        if (!selection.isEmpty() && selection.getFirstElement() instanceof OPIFont) {
            opiFont = new OPIFont((OPIFont) selection.getFirstElement());
            outputTextLabel.setText(opiFont.getFontMacroName());
            outputTextLabel.setFont(CustomMediaFactory.getInstance().getFont(opiFont.getFontData()));
            pixelsOrPointsBox.setSizeInPixels(opiFont.isSizeInPixels());
            pixelsOrPointsBox.setEnabled(false);
            getShell().layout(true, true);
        }
    }

    private void createLabel(Composite parent, String text) {
        var label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
    }

    public OPIFont getOutput() {
        return opiFont;
    }
}
