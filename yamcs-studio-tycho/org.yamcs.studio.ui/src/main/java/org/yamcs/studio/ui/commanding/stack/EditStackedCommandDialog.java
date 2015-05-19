package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.xtce.Argument;

public class EditStackedCommandDialog extends TitleAreaDialog {

    private StackedCommand command;
    private List<Text> textFields = new ArrayList<>();

    public EditStackedCommandDialog(Shell parentShell, StackedCommand command) {
        super(parentShell);
        this.command = command;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Edit Stacked Command");
        // setMessage("informative message");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite.setLayout(new GridLayout());
        Label desc = new Label(composite, SWT.NONE);
        desc.setText("Specify the parameters for command " + command.getMetaCommand().getName());
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite argumentsComposite = new Composite(composite, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argumentsComposite.setLayout(new GridLayout(2, false));
        for (Argument arg : command.getMetaCommand().getArgumentList()) {
            Label lbl = new Label(argumentsComposite, SWT.NONE);
            lbl.setText(arg.getName());

            Text text = new Text(argumentsComposite, SWT.BORDER);
            text.setData(arg);
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            String value = command.getAssignedStringValue(arg);
            text.setText(value == null ? "" : value);
            text.addModifyListener(evt -> command.addAssignment(arg, text.getText()));
            textFields.add(text);
        }
        return composite;
    }

    @Override
    protected void okPressed() {
        for (Text textField : textFields) {
            Argument arg = (Argument) textField.getData();
            if (textField.getText().trim().isEmpty()) {
                command.addAssignment(arg, null);
            } else {
                command.addAssignment(arg, textField.getText());
            }
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}
