package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class EditStackedCommandDialog extends TitleAreaDialog {

    private StackedCommand command;
    ArgumentTableBuilder atb;

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
        aliases.add(command.getMetaCommand().getQualifiedName());
        for (NamedObjectId noi : command.getMetaCommand().getAliasList()) {
            String alias = noi.getNamespace() + "/" + noi.getName();
            if (alias.equals(command.getMetaCommand().getQualifiedName()))
                continue;
            aliases.add(alias);
        }

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

        atb = new ArgumentTableBuilder(command);
        atb.createArgumentTable(composite);
        atb.updateCommandArguments();
        atb.pack();

        return composite;
    }

    @Override
    protected void okPressed() {
        atb.applyArgumentsToCommands();
        super.okPressed();
    }
}
