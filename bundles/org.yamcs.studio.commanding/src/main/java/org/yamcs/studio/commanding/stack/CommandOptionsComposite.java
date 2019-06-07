package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.ArgumentTypeInfo;
import org.yamcs.protobuf.Mdb.EnumValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;

public class CommandOptionsComposite extends Composite {

    private Composite argumentsComposite;
    private Combo namespaceCombo;
    private List<String> aliases;
    private List<Control> controls = new ArrayList<>();

    public CommandOptionsComposite(Composite parent, int style, StackedCommand command) {
        super(parent, style);
        setLayout(new GridLayout());
        argumentsComposite = new Composite(this, SWT.NONE);
        argumentsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        argumentsComposite.setLayout(new GridLayout(3, false));
        addArgumentControls(argumentsComposite, command);

        // expandable command options
        ExpandableComposite expandable = new ExpandableComposite(this, ExpandableComposite.TREE_NODE |
                ExpandableComposite.CLIENT_INDENT);
        expandable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        expandable.setLayout(new GridLayout(1, false));
        expandable.setExpanded(false);
        expandable.setText("Command Options");

        Composite optionsComposite = new Composite(expandable, SWT.NONE);
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

        // command namespace selection
        Label chooseNamespace = new Label(optionsComposite, SWT.NONE);
        chooseNamespace.setText("Alias");
        namespaceCombo = new Combo(optionsComposite, SWT.READ_ONLY);
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

        // populate namespace combo
        // (switching ops name and qualified name)
        aliases = new ArrayList<>();
        aliases.add(command.getMetaCommand().getQualifiedName());
        for (NamedObjectId noi : command.getMetaCommand().getAliasList()) {
            String alias = noi.getNamespace() + "/" + noi.getName();
            if (alias.equals(command.getMetaCommand().getQualifiedName())) {
                continue;
            }
            aliases.add(alias);
        }
        namespaceCombo.setItems(aliases.toArray(new String[aliases.size()]));
        namespaceCombo.select(0);
        command.setSelectedAliase(aliases.get(0));

        expandable.setClient(optionsComposite);
        expandable.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                parent.layout(true);
            }
        });
        comment.addModifyListener(evt -> {
            if (comment.getText().trim().isEmpty()) {
                command.setComment(null);
            } else {
                command.setComment(comment.getText());
            }
        });
    }

    private void addArgumentControls(Composite composite, StackedCommand command) {
        for (TelecommandArgument argument : command.getEffectiveAssignments()) {
            if (!argument.isEditable()) {
                continue;
            }
            Label argumentLabel = new Label(composite, SWT.NONE);
            argumentLabel.setText(argument.getName());

            if ("integer".equals(argument.getType())) {
                Spinner argumentSpinner = new Spinner(composite, SWT.BORDER);
                argumentSpinner.setMaximum(Integer.MAX_VALUE);
                argumentSpinner.setLayoutData(new GridData(150, SWT.DEFAULT));
                argumentSpinner.setData(argument);
                if (command.isAssigned(argument.getArgumentInfo())) {
                    String value = command.getAssignedStringValue(argument.getArgumentInfo());
                    argumentSpinner.setSelection(Integer.parseInt(value));
                } else if (argument.getValue() != null) {
                    argumentSpinner.setSelection(Integer.parseInt(argument.getValue()));
                }
                controls.add(argumentSpinner);
            } else if ("enumeration".equals(argument.getType())) {
                Combo argumentCombo = new Combo(composite, SWT.READ_ONLY);
                argumentCombo.setLayoutData(new GridData(150, SWT.DEFAULT));
                argumentCombo.setData(argument);
                for (EnumValue enumValue : argument.getArgumentInfo().getType().getEnumValueList()) {
                    argumentCombo.add(enumValue.getLabel());
                    argumentCombo.setData(enumValue.getLabel(), enumValue);
                }
                if (command.isAssigned(argument.getArgumentInfo())) {
                    String value = command.getAssignedStringValue(argument.getArgumentInfo());
                    argumentCombo.setText(value);
                } else if (argument.getValue() != null) {
                    argumentCombo.setText(argument.getValue());
                }
                controls.add(argumentCombo);
            } else {
                Text argumentText = new Text(composite, SWT.BORDER);
                argumentText.setLayoutData(new GridData(150, SWT.DEFAULT));
                argumentText.setData(argument);
                if (command.isAssigned(argument.getArgumentInfo())) {
                    String value = command.getAssignedStringValue(argument.getArgumentInfo());
                    argumentText.setText(value);
                } else if (argument.getValue() != null) {
                    argumentText.setText(argument.getValue());
                }
                controls.add(argumentText);
            }

            Label hintLabel = new Label(composite, SWT.BORDER);
            hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            String hint = "";

            ArgumentTypeInfo argumentType = argument.getArgumentInfo().getType();
            if (argumentType.hasRangeMin() || argumentType.hasRangeMax()) {
                String format = "integer".equals(argumentType.getEngType()) ? "%.0f" : "%f";
                if (argumentType.hasRangeMin() && argumentType.hasRangeMax()) {
                    hint += "Value must be in range [" + String.format(format, argumentType.getRangeMin());
                    hint += ", " + String.format(format, argumentType.getRangeMax()) + "]";
                } else if (argumentType.hasRangeMax()) {
                    hint += "Value must be <= " + String.format(format, argumentType.getRangeMax());
                } else if (argumentType.hasRangeMin()) {
                    hint += "Value must be >= " + String.format(format, argumentType.getRangeMin());
                }
            }
            hintLabel.setText(hint);
        }
    }

    public Map<ArgumentInfo, String> getAssignments() {
        Map<ArgumentInfo, String> assignments = new HashMap<>();
        for (Control control : controls) {
            TelecommandArgument argument = (TelecommandArgument) control.getData();
            if (control instanceof Text) {
                String text = ((Text) control).getText();
                assignments.put(argument.getArgumentInfo(), text);
            } else if (control instanceof Combo) {
                String text = ((Combo) control).getText();
                assignments.put(argument.getArgumentInfo(), text);
            } else if (control instanceof Spinner) {
                int number = ((Spinner) control).getSelection();
                assignments.put(argument.getArgumentInfo(), Integer.toString(number));
            } else {
                throw new UnsupportedOperationException("Unexpected control of type " + control.getClass());
            }
        }
        return assignments;
    }
}
