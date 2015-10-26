package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.yamcs.protobuf.Mdb.ArgumentInfo;

public class AddToStackWizardPage2 extends WizardPage {

    private StackedCommand command;
    private Composite controlComposite;
    private Label desc;
    private Composite argumentsComposite;

    public AddToStackWizardPage2(StackedCommand command) {
        super("Specify Parameters");
        setTitle("Specify Parameters");
        this.command = command;
        setPageComplete(true);
    }

    @Override
    public void setVisible(boolean visible) {
        // If the user is flipping back and forth between the pages, we may need
        // to update this page if another command was selected than before.
        if (visible) {
            updateControl();
        }
        super.setVisible(visible);
    }

    private void updateControl() {
        String qname = command.getMetaCommand().getQualifiedName();
        desc.setText("Specify the parameters for command " + qname);

        // Clear previous state. This is slightly suboptimal since we also lose state
        // If the user just flips between back and next without actually changing the command.
        command.getAssignments().clear();
        for (Control child : argumentsComposite.getChildren())
            child.dispose();

        // Register new state
        for (ArgumentInfo arg : command.getMetaCommand().getArgumentList()) {
            Label lbl = new Label(argumentsComposite, SWT.NONE);
            lbl.setText(arg.getName());

            Text text = new Text(argumentsComposite, SWT.BORDER);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (arg.getInitialValue() != null) {
                text.setText(arg.getInitialValue());
                command.addAssignment(arg, arg.getInitialValue());
            }
            text.addModifyListener(evt -> {
                if (text.getText().trim().isEmpty()) {
                    command.addAssignment(arg, null);
                } else {
                    command.addAssignment(arg, text.getText());
                }
            });
        }

        argumentsComposite.layout();
        controlComposite.layout();
    }

    @Override
    public void createControl(Composite parent) {
        controlComposite = new Composite(parent, SWT.NONE);
        setControl(controlComposite);

        controlComposite.setLayout(new GridLayout());
        desc = new Label(controlComposite, SWT.NONE);
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        argumentsComposite = new Composite(controlComposite, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argumentsComposite.setLayout(new GridLayout(2, false));
    }
}
