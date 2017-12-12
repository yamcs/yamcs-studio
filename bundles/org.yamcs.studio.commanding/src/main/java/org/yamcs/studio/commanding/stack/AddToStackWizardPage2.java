package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class AddToStackWizardPage2 extends WizardPage {

    private StackedCommand command;
    ArgumentTableBuilder atb;
    private Composite controlComposite;
    private Combo namespaceCombo;
    private Label desc;
    TableViewer argumentTable;
    List<String> aliases;

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

        // populate namespace combo
        // (switching ops name and qualified name)
        aliases = new ArrayList<String>();
        aliases.add(command.getMetaCommand().getQualifiedName());
        for (NamedObjectId noi : command.getMetaCommand().getAliasList()) {
            String alias = noi.getNamespace() + "/" + noi.getName();
            if (alias.equals(command.getMetaCommand().getQualifiedName()))
                continue;
            aliases.add(alias);
        }
        namespaceCombo.setItems(aliases.toArray(new String[aliases.size()]));
        namespaceCombo.select(0);
        command.setSelectedAliase(aliases.get(0));

        // Register new state
        atb.updateCommandArguments();
        atb.pack();
        controlComposite.layout();
    }

    @Override
    public void createControl(Composite parent) {

        controlComposite = new Composite(parent, SWT.NONE);

        setControl(controlComposite);
        controlComposite.setLayout(new GridLayout());

        // command namespace selection
        Composite namespaceComposite = new Composite(controlComposite, SWT.NONE);
        namespaceComposite.setLayout(new GridLayout(2, false));
        Label chooseNamespace = new Label(namespaceComposite, SWT.NONE);
        chooseNamespace.setText("Choose Command Namespace: ");
        namespaceCombo = new Combo(namespaceComposite, SWT.READ_ONLY);
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

        // expandable command options
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
                controlComposite.layout(true);
            }
        });
        comment.addModifyListener(evt -> {
            if (comment.getText().trim().isEmpty()) {
                command.setComment(null);
            } else {
                command.setComment(comment.getText());
            }
        });

        // argument table
        atb = new ArgumentTableBuilder(command);
        atb.createArgumentTable(controlComposite);
    }

    public void applyArgumentsToCommand() {
        if (atb != null)
            atb.applyArgumentsToCommands();
    }

}
