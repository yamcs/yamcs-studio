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
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The dialog for selecting OPIColor.
 */
public class OPIColorDialog extends TrayDialog {

    private OPIColor opiColor;
    private TableViewer preDefinedColorsViewer;
    private Label outputTextLabel;
    private String title;

    private Group rgbGroup = null;
    private Label redLabel = null;
    private Scale redScale = null;
    private Spinner redSpinner = null;
    private Label greenLabel = null;
    private Scale greenScale = null;
    private Spinner greenSpinner = null;
    private Label blueLabel = null;
    private Scale blueScale = null;
    private Spinner blueSpinner = null;
    private Canvas colorCanvas;

    protected OPIColorDialog(Shell parentShell, OPIColor color, String dialogTitle) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        title = dialogTitle;
        opiColor = color.getCopy();
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
        gd.widthHint = 220;
        leftComposite.setLayoutData(gd);
        createLabel(leftComposite, "Choose from Predefined Colors:");

        preDefinedColorsViewer = createPredefinedColorsTableViewer(leftComposite);
        preDefinedColorsViewer.setInput(MediaService.getInstance().getAllPredefinedColors());

        var rightComposite = new Composite(mainComposite, SWT.None);
        rightComposite.setLayout(new GridLayout(1, false));
        gd = new GridData(SWT.LEFT, SWT.BEGINNING, true, true);
        rightComposite.setLayoutData(gd);

        createLabel(rightComposite, "");

        var colorDialogButton = new Button(rightComposite, SWT.PUSH);
        colorDialogButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        colorDialogButton.setText("Choose from Color Dialog");
        colorDialogButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var dialog = new ColorDialog(Display.getCurrent().getActiveShell());
                dialog.setRGB(opiColor.getRGBValue());
                var rgb = dialog.open();
                if (rgb != null) {
                    opiColor.setColorValue(rgb);
                    preDefinedColorsViewer.setSelection(null);

                    setRGBEditValue(rgb);

                    outputTextLabel.setText(opiColor.getColorName());
                    colorCanvas.setBackground(CustomMediaFactory.getInstance().getColor(opiColor.getRGBValue()));
                }
            }
        });

        createRGBEditGroup(rightComposite);

        var group = new Group(rightComposite, SWT.None);
        group.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));
        group.setLayout(new GridLayout(3, false));
        group.setText("Output");

        colorCanvas = new Canvas(group, SWT.BORDER);
        colorCanvas.setBackground(CustomMediaFactory.getInstance().getColor(opiColor.getRGBValue()));
        gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
        gd.widthHint = 30;
        gd.heightHint = 30;
        colorCanvas.setLayoutData(gd);

        outputTextLabel = new Label(group, SWT.None);
        outputTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        outputTextLabel.setText(opiColor.getColorName());

        if (opiColor.isPreDefined()) {
            preDefinedColorsViewer.setSelection(new StructuredSelection(opiColor));
        } else {
            preDefinedColorsViewer.setSelection(null);
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

    /**
     * @param rightComposite
     */
    private void createRGBEditGroup(Composite rightComposite) {

        var rgbGD = new GridData();
        rgbGD.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        rgbGD.grabExcessHorizontalSpace = true;
        var gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        rgbGroup = new Group(rightComposite, SWT.NONE);
        rgbGroup.setLayout(gridLayout);
        redLabel = new Label(rgbGroup, SWT.NONE);
        redLabel.setText("Red");
        redLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
        redLabel.setLayoutData(rgbGD);
        redScale = new Scale(rgbGroup, SWT.NONE);
        redScale.setMaximum(255);
        redScale.setIncrement(1);
        redScale.setPageIncrement(51);
        redSpinner = new Spinner(rgbGroup, SWT.BORDER);
        redSpinner.setMaximum(255);
        redSpinner.setLayoutData(rgbGD);

        greenLabel = new Label(rgbGroup, SWT.NONE);
        greenLabel.setText("Green");
        greenLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
        greenLabel.setLayoutData(rgbGD);
        greenScale = new Scale(rgbGroup, SWT.NONE);
        greenScale.setPageIncrement(51);
        greenScale.setMaximum(255);

        greenSpinner = new Spinner(rgbGroup, SWT.BORDER);
        greenSpinner.setMaximum(255);
        greenSpinner.setLayoutData(rgbGD);
        blueLabel = new Label(rgbGroup, SWT.NONE);
        blueLabel.setText("Blue");
        blueLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        blueScale = new Scale(rgbGroup, SWT.NONE);
        blueScale.setPageIncrement(51);
        blueScale.setMaximum(255);
        blueSpinner = new Spinner(rgbGroup, SWT.BORDER);
        blueSpinner.setMaximum(255);

        setRGBEditValue(opiColor.getRGBValue());

        redScale.addSelectionListener(new RGBEditListener(0));
        redSpinner.addSelectionListener(new RGBEditListener(0));

        greenScale.addSelectionListener(new RGBEditListener(1));
        greenSpinner.addSelectionListener(new RGBEditListener(1));

        blueScale.addSelectionListener(new RGBEditListener(2));
        blueSpinner.addSelectionListener(new RGBEditListener(2));

        rgbGroup.setTabList(new Control[] { redScale, greenScale, blueScale, redSpinner, greenSpinner, blueSpinner });
    }

    /**
     * Creates and configures a {@link TableViewer}.
     *
     * @param parent
     *            The parent for the table
     * @return The {@link TableViewer}
     */
    private TableViewer createPredefinedColorsTableViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new BaseWorkbenchContentProvider() {
            @Override
            public Object[] getElements(Object element) {
                return (Object[]) element;
            }
        });
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.addSelectionChangedListener(event -> refreshGUIOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.addDoubleClickListener(event -> okPressed());
        return viewer;
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshGUIOnSelection() {
        var selection = (IStructuredSelection) preDefinedColorsViewer.getSelection();
        if (!selection.isEmpty() && selection.getFirstElement() instanceof OPIColor) {
            opiColor = ((OPIColor) selection.getFirstElement()).getCopy();
            setRGBEditValue(opiColor.getRGBValue());
            outputTextLabel.setText(opiColor.getColorName());
            colorCanvas.setBackground(CustomMediaFactory.getInstance().getColor(opiColor.getRGBValue()));
        }
    }

    /**
     * Creates a label with the given text.
     *
     * @param parent
     *            The parent for the label
     * @param text
     *            The text for the label
     */
    private void createLabel(Composite parent, String text) {
        var label = new Label(parent, SWT.WRAP);
        label.setText(text);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
    }

    public OPIColor getOutput() {
        return opiColor;
    }

    /**
     * @param rgb
     */
    private void setRGBEditValue(RGB rgb) {
        redScale.setSelection(rgb.red);
        redSpinner.setSelection(rgb.red);
        greenScale.setSelection(rgb.green);
        greenSpinner.setSelection(rgb.green);
        blueScale.setSelection(rgb.blue);
        blueSpinner.setSelection(rgb.blue);
    }

    class RGBEditListener extends SelectionAdapter {
        private int type;
        private Scale scale;
        private Spinner spinner;

        public RGBEditListener(int type) {
            this.type = type;
            if (type == 0) {
                scale = redScale;
                spinner = redSpinner;
            } else if (type == 1) {
                scale = greenScale;
                spinner = greenSpinner;
            } else if (type == 2) {
                scale = blueScale;
                spinner = blueSpinner;
            }
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            var rgb = opiColor.getRGBValue();
            var newValue = 0;

            if (e.getSource() instanceof Scale) {
                newValue = ((Scale) e.getSource()).getSelection();
                spinner.setSelection(newValue);
            } else if (e.getSource() instanceof Spinner) {
                newValue = ((Spinner) e.getSource()).getSelection();
                scale.setSelection(newValue);
            }
            if (type == 0) {
                rgb.red = newValue;
            } else if (type == 1) {
                rgb.green = newValue;
            } else if (type == 2) {
                rgb.blue = newValue;
            }
            opiColor.setColorValue(rgb);
            preDefinedColorsViewer.setSelection(null);
            colorCanvas.setBackground(CustomMediaFactory.getInstance().getColor(opiColor.getRGBValue()));
            outputTextLabel.setText(opiColor.getColorName());
        }
    }
}
