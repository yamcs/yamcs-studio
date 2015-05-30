package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.yamcs.xtce.Argument;

public class AddToStackWizardPage2 extends WizardPage {

    private StackedCommand command;

    public AddToStackWizardPage2(StackedCommand command) {
        super("Specify Parameters");
        setTitle("Specify Parameters");
        this.command = command;
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout());
        Label desc = new Label(composite, SWT.NONE);
        desc.setText("Specify the parameters for command " + command.getMetaCommand().getQualifiedName());
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite argumentsComposite = new Composite(composite, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argumentsComposite.setLayout(new GridLayout(2, false));
        for (Argument arg : command.getMetaCommand().getArgumentList()) {
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
    }
}
