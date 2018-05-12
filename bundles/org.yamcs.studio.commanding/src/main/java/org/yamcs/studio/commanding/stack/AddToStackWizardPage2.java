package org.yamcs.studio.commanding.stack;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.yamcs.protobuf.Mdb.ArgumentInfo;

public class AddToStackWizardPage2 extends WizardPage {

    private StackedCommand command;
    private Composite controlComposite;
    private CommandOptionsComposite optionsComposite;

    private String previousCommand = "";

    public AddToStackWizardPage2(StackedCommand command) {
        super("Specify Arguments");
        setTitle("Specify Arguments");
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

    @Override
    public void createControl(Composite parent) {
        controlComposite = new Composite(parent, SWT.NONE);
        controlComposite.setLayout(new FillLayout());
        setControl(controlComposite);
    }

    private void updateControl() {

        // Check if we keep state if the user just flips between back and next without actually changing the command.
        if (previousCommand.equals(command.getMetaCommand().getQualifiedName())) {
            return;
        }
        previousCommand = command.getMetaCommand().getQualifiedName();

        // Clear previous state
        command.getAssignments().clear();

        // set header message
        setMessage(AddToStackWizardPage1.getMessage(command.getMetaCommand()));

        // Register new state
        if (optionsComposite != null) {
            optionsComposite.dispose();
        }
        optionsComposite = new CommandOptionsComposite(controlComposite, SWT.NONE, command);
        optionsComposite.layout();
        controlComposite.layout();
    }

    public Map<ArgumentInfo, String> getAssignments() {
        return optionsComposite.getAssignments();
    }
}
