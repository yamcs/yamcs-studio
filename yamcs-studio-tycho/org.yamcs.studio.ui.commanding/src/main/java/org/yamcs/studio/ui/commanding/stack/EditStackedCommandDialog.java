package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.ui.commanding.stack.ArgumentTableBuilder.ArgumentAssignement;

public class EditStackedCommandDialog extends TitleAreaDialog {

    private StackedCommand command;
    private List<Text> textFields = new ArrayList<>();

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

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        // command namespace selection
        // populate namespace combo
        // (switching ops name and qualified name)
        List<String> aliases = new ArrayList<String>();
        for (NamedObjectId noi : command.getMetaCommand().getAliasList()) {
            aliases.add(noi.getNamespace() + "/" + noi.getName());
        }
        String temp = aliases.get(0);
        aliases.set(0, aliases.get(1));
        aliases.set(1, temp);

        Composite namespaceComposite = new Composite(composite, SWT.NONE);
        namespaceComposite.setLayout(new GridLayout(2, false));
        Label chooseNamespace = new Label(namespaceComposite, SWT.NONE);
        chooseNamespace.setText("Choose Command Namespace: ");
        Combo namespaceCombo = new Combo(namespaceComposite, SWT.READ_ONLY);
        namespaceCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                command.setSelectedAliase(aliases.get(namespaceCombo.getSelectionIndex()));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });
        namespaceCombo.setItems(aliases.toArray(new String[aliases.size()]));
        for (int i = 0; i < aliases.size(); i++) {
            if (aliases.get(i).equals(command.getSelectedAlias())) {
                namespaceCombo.select(i);
            }
        }

        ExpandableComposite ec = new ExpandableComposite(composite, ExpandableComposite.TREE_NODE |
                ExpandableComposite.CLIENT_INDENT);
        ec.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ec.setLayout(new GridLayout(1, false));
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
        ec.setExpanded(!comment.getText().isEmpty());
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 2 * comment.getLineHeight();
        comment.setLayoutData(gridData);
        ec.setClient(optionsComposite);
        ec.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                parent.layout(true);
                composite.layout();
                optionsComposite.layout();
            }
        });
        comment.addModifyListener(evt -> {
            if (comment.getText().trim().isEmpty()) {
                command.setComment(null);
            } else {
                command.setComment(comment.getText());
            }
        });

        TableViewer argumentTable = (new ArgumentTableBuilder(command)).createArgumentTable(composite);
        ArrayList<ArgumentAssignement> argumentAssignements = new ArrayList<>();
        for (ArgumentInfo arg : command.getMetaCommand().getArgumentList()) {
            String value = command.getAssignedStringValue(arg);
            if (value == null && arg.getInitialValue() != null) {
                command.addAssignment(arg, arg.getInitialValue());
            }
            argumentAssignements.add(new ArgumentAssignement(arg, value == null ? arg.getInitialValue() : value));
        }
        argumentTable.setInput(argumentAssignements);
        (new ArgumentTableBuilder(command)).pack(argumentTable);

        return composite;
    }

    @Override
    protected void okPressed() {
        for (Text textField : textFields) {
            ArgumentInfo arg = (ArgumentInfo) textField.getData();
            if (textField.getText().trim().isEmpty()) {
                command.addAssignment(arg, null);
            } else {
                command.addAssignment(arg, textField.getText());
            }
        }
        super.okPressed();
    }
}
