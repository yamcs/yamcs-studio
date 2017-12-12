package org.yamcs.studio.css.theming;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class ThemeColorDetailsPage implements IDetailsPage {
    private IManagedForm mform;
    private ThemeColor input;

    private Scale redScale;
    private Scale greenScale;
    private Scale blueScale;

    private Text redText;
    private Text greenText;
    private Text blueText;

    private Color selectedColor;
    private Label colorLabel;

    public ThemeColorDetailsPage() {
    }

    @Override
    public void initialize(IManagedForm mform) {
        this.mform = mform;
        selectedColor = mform.getForm().getDisplay().getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
    public void createContents(Composite parent) {
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 5;
        layout.leftMargin = 5;
        layout.rightMargin = 2;
        layout.bottomMargin = 2;
        parent.setLayout(layout);

        FormToolkit tk = mform.getToolkit();
        Section s1 = tk.createSection(parent, Section.NO_TITLE);
        // s1.marginWidth = 10;

        TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
        td.grabHorizontal = true;
        s1.setLayoutData(td);
        Composite client = tk.createComposite(s1);
        GridLayout gl = new GridLayout();
        client.setLayout(gl);

        colorLabel = tk.createLabel(client, "   yy        ", SWT.BORDER);
        colorLabel.setBackground(selectedColor);

        Composite rgbScales = tk.createComposite(client);
        GridData gd = new GridData();
        rgbScales.setLayoutData(gd);
        gl = new GridLayout(3, false);
        rgbScales.setLayout(gl);

        tk.createLabel(rgbScales, "Red");
        redScale = new Scale(rgbScales, SWT.NONE);
        redScale.setMinimum(0);
        redScale.setMaximum(255);
        redScale.setIncrement(1);
        redScale.addListener(SWT.Selection, evt -> {
            redText.setText("" + redScale.getSelection());
            updateSelectedColor();
        });
        redText = tk.createText(rgbScales, "                ", SWT.RIGHT);
        redText.setEnabled(false);
        gd = new GridData();
        gd.widthHint = 50;
        redText.setLayoutData(gd);

        tk.createLabel(rgbScales, "Green");
        greenScale = new Scale(rgbScales, SWT.NONE);
        greenScale.setMinimum(0);
        greenScale.setMaximum(255);
        greenScale.setIncrement(1);
        greenScale.addListener(SWT.Selection, evt -> {
            greenText.setText("" + greenScale.getSelection());
            updateSelectedColor();
        });
        greenText = tk.createText(rgbScales, "                ", SWT.RIGHT);
        greenText.setEnabled(false);
        gd = new GridData();
        gd.widthHint = 50;
        greenText.setLayoutData(gd);

        tk.createLabel(rgbScales, "Blue");
        blueScale = new Scale(rgbScales, SWT.NONE);
        blueScale.setMinimum(0);
        blueScale.setMaximum(255);
        blueScale.setIncrement(1);
        blueScale.addListener(SWT.Selection, evt -> {
            blueText.setText("" + blueScale.getSelection());
            updateSelectedColor();
        });
        blueText = tk.createText(rgbScales, "                ", SWT.RIGHT);
        blueText.setEnabled(false);
        gd = new GridData();
        gd.widthHint = 50;
        blueText.setLayoutData(gd);

        tk.paintBordersFor(s1);
        s1.setClient(client);
    }

    private void update() {
        RGB rgb = input.getRGB();
        redScale.setSelection(rgb.red);
        greenScale.setSelection(rgb.green);
        blueScale.setSelection(rgb.blue);

        redText.setText("" + rgb.red);
        greenText.setText("" + rgb.green);
        blueText.setText("" + rgb.blue);
        updateSelectedColor();
    }

    private void updateSelectedColor() {
        selectedColor.dispose();
        selectedColor = new Color(mform.getForm().getDisplay(),
                redScale.getSelection(), greenScale.getSelection(), blueScale.getSelection());
        colorLabel.setBackground(selectedColor);
    }

    @Override
    public void selectionChanged(IFormPart part, ISelection selection) {
        IStructuredSelection ssel = (IStructuredSelection) selection;
        if (ssel.size() == 1) {
            input = (ThemeColor) ssel.getFirstElement();
        } else
            input = null;
        update();
    }

    @Override
    public void commit(boolean onSave) {
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        if (selectedColor != null)
            selectedColor.dispose();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isStale() {
        return false;
    }

    @Override
    public void refresh() {
        update();
    }

    @Override
    public boolean setFormInput(Object input) {
        return false;
    }
}
