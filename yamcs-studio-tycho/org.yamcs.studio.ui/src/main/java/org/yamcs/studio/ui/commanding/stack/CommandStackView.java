package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.part.ViewPart;

public class CommandStackView extends ViewPart {

    private LocalResourceManager resourceManager;
    private CommandStackTableViewer commandTableViewer;
    private ArgumentTableViewer argumentTableViewer;

    // Toolit for ArgumentsForm
    private FormToolkit tk;

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        parent.setLayout(new FillLayout());

        ManagedForm managedForm = new ManagedForm(parent);
        managedForm.getForm().setText("Overview");
        managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

        CommandStackMasterDetailsBlock block = new CommandStackMasterDetailsBlock(this);
        block.createContent(managedForm);
    }

    private Section createArgumentsSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
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

    private Section createConstraintsSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
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

    public void addTelecommand(Telecommand command) {
        commandTableViewer.addTelecommand(command);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        tk.dispose();
        super.dispose();
    }
}
