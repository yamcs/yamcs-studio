package org.yamcs.studio.commanding.stack;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class EditStackedCommandDialog extends TitleAreaDialog {

    private StackedCommand command;
    private CommandOptionsComposite composite;

    public EditStackedCommandDialog(Shell parentShell, StackedCommand command) {
        super(parentShell);
        this.command = command;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Edit Stacked Command");
        setMessage(AddToStackWizardPage1.getMessage(command.getMetaCommand()));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        composite = new CommandOptionsComposite(parent, SWT.NONE, command);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    @Override
    protected void okPressed() {
        composite.getAssignments().forEach((assignmentInfo, value) -> {
            command.addAssignment(assignmentInfo, value);
        });
        super.okPressed();
    }
}
