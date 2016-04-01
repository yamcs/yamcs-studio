package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
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
        setMessage(command.getMetaCommand().getQualifiedName());
        desc.setText("Specify the command parameters:");

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

        ExpandableComposite ec = new ExpandableComposite(controlComposite, ExpandableComposite.TREE_NODE |
                ExpandableComposite.CLIENT_INDENT);
        ec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ec.setLayout(new GridLayout(1, false));
        ec.setExpanded(false);
        ec.setText("Command Options");
        Composite optionsComposite = new Composite(ec, SWT.NONE);
        optionsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        optionsComposite.setLayout(new GridLayout(2, false));
        Label l1 = new Label(optionsComposite, SWT.NONE);
        l1.setText("Comment");
        GridData gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
        l1.setLayoutData(gridData);
        Text comment = new Text(optionsComposite, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        comment.setText(command.getComment() != null ? command.getComment() : "");
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 2 * comment.getLineHeight();
        comment.setLayoutData(gridData);
        ec.setClient(optionsComposite);
        ec.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                parent.layout(true);
                parent.getShell().pack();
                updateControl();
            }
        });
        comment.addModifyListener(evt -> {
            if (comment.getText().trim().isEmpty()) {
                command.setComment(null);
            } else {
                command.setComment(comment.getText());
            }
        });

        desc = new Label(controlComposite, SWT.NONE);
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        argumentsComposite = new Composite(controlComposite, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argumentsComposite.setLayout(new GridLayout(2, false));
    }
}
