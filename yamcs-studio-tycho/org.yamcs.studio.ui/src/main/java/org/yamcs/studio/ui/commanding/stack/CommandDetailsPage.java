package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class CommandDetailsPage implements IDetailsPage {

    private IManagedForm mform;
    private Telecommand input;

    public CommandDetailsPage(Telecommand input) {
        this.input = input;
    }

    @Override
    public void initialize(IManagedForm mform) {
        this.mform = mform;
    }

    @Override
    public void createContents(Composite parent) {
        TableWrapLayout layout = new TableWrapLayout();
        parent.setLayout(layout);
        FormToolkit tk = mform.getToolkit();
        createArgumentsSection(tk, parent);
        createConstraintsSection(tk, parent);
    }

    private Section createArgumentsSection(FormToolkit tk, Composite parent) {
        Section section = tk.createSection(parent, Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Arguments");
        Composite sectionClient = tk.createComposite(section);
        sectionClient.setLayout(new GridLayout(2, false));

        tk.createLabel(sectionClient, "abc");
        Text text = tk.createText(sectionClient, "");
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        section.setClient(sectionClient);
        return section;
    }

    private Section createConstraintsSection(FormToolkit tk, Composite parent) {
        Section section = tk.createSection(parent, Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Constraints");
        Composite sectionClient = tk.createComposite(section);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        sectionClient.setLayout(gl);

        tk.createLabel(sectionClient, "Transmission Constraints");
        Label lbl = tk.createLabel(sectionClient, "ab\ncd", SWT.WRAP);
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        tk.createLabel(sectionClient, "Timeout");
        lbl = tk.createLabel(sectionClient, "10000");
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        section.setClient(sectionClient);
        return section;
    }

    private void update() {
        //flag1.setSelection(input != null && input.getFlag1());
        //flag2.setSelection(input != null && input.getFlag2());
    }

    @Override
    public void selectionChanged(IFormPart part, ISelection selection) {
        /*
         * IStructuredSelection ssel = (IStructuredSelection) selection; if (ssel.size() == 1) input
         * = (Telecommand) ssel.getFirstElement(); else input = null; update();
         */
    }

    @Override
    public void commit(boolean onSave) {
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
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
