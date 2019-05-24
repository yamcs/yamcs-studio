package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.ArgumentTypeInfo;
import org.yamcs.protobuf.Mdb.EnumValue;

public class CommandOptionsComposite extends ScrolledComposite {

    private Group argumentsGroup;
    private List<Control> controls = new ArrayList<>();

    public CommandOptionsComposite(Composite parent, int style, StackedCommand command) {
        super(parent, style | SWT.V_SCROLL);

        Composite scrollpane = new Composite(this, SWT.NONE);
        scrollpane.setLayout(new GridLayout());

        argumentsGroup = new Group(scrollpane, SWT.NONE);
        argumentsGroup.setText("Arguments");
        argumentsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        argumentsGroup.setLayout(gl);

        Collection<TelecommandArgument> allArguments = command.getEffectiveAssignments();

        List<TelecommandArgument> argumentsWithoutInitialValue = allArguments.stream()
                .filter(arg -> arg.isEditable() && !arg.getArgumentInfo().hasInitialValue())
                .collect(Collectors.toList());

        List<TelecommandArgument> argumentsWithInitialValue = allArguments.stream()
                .filter(arg -> arg.isEditable() && arg.getArgumentInfo().hasInitialValue())
                .collect(Collectors.toList());

        if (argumentsWithoutInitialValue.isEmpty()) {
            Label lbl = new Label(argumentsGroup, SWT.NONE);
            lbl.setText("No arguments required");
        } else {
            addArgumentControls(argumentsGroup, command, argumentsWithoutInitialValue);
        }

        if (!argumentsWithInitialValue.isEmpty()) {
            ExpandableComposite expandable = new ExpandableComposite(argumentsGroup, ExpandableComposite.TREE_NODE |
                    ExpandableComposite.CLIENT_INDENT);
            expandable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            expandable.setLayout(new GridLayout(1, false));
            expandable.setExpanded(false);
            if (argumentsWithInitialValue.size() == 1) {
                expandable.setText("1 argument with default");
            } else {
                expandable.setText(argumentsWithInitialValue.size() + " arguments with defaults");
            }
            Composite defaultArguments = new Composite(expandable, SWT.NONE);
            defaultArguments.setLayout(new FillLayout());
            addArgumentControls(defaultArguments, command, argumentsWithInitialValue);
            expandable.setClient(defaultArguments);
            expandable.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    parent.layout(true);
                }
            });
        }

        Group optionsGroup = new Group(scrollpane, SWT.NONE);
        optionsGroup.setText("Options");
        optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        optionsGroup.setLayout(new GridLayout(2, false));
        
        // option for stack delay
        Label l0 = new Label(optionsGroup, SWT.NONE);
        l0.setText("Issue Delay (ms)");
        GridData gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
        l0.setLayoutData(gridData);
        Spinner delayMs = new Spinner(optionsGroup, SWT.BORDER );
        delayMs.setValues(command.getDelayMs(), 0, Integer.MAX_VALUE, 0, 1, 100);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);        
        delayMs.setLayoutData(gridData);
        delayMs.addModifyListener(evt -> {           
                command.setDelayMs(delayMs.getSelection());            
        });
        
        // option for comment
        Label l1 = new Label(optionsGroup, SWT.NONE);
        l1.setText("Comment");
        gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
        l1.setLayoutData(gridData);
        Text comment = new Text(optionsGroup, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        comment.setText(command.getComment() != null ? command.getComment() : "");
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 3 * comment.getLineHeight();
        comment.setLayoutData(gridData);
        comment.addModifyListener(evt -> {
            if (comment.getText().trim().isEmpty()) {
                command.setComment(null);
            } else {
                command.setComment(comment.getText());
            }
        });

        setContent(scrollpane);
        setExpandVertical(true);
        setExpandHorizontal(true);

        Point size = scrollpane.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        setMinSize(size);
        scrollpane.setSize(size);
    }

    private void addArgumentControls(Composite parent, StackedCommand command,
            Collection<TelecommandArgument> arguments) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        composite.setLayout(gl);
        for (TelecommandArgument argument : arguments) {
            Label argumentLabel = new Label(composite, SWT.NONE);
            argumentLabel.setText(argument.getName());

            if ("integer".equals(argument.getType())) {
                if (argument.isIntegerWithinJavaIntRange()) {
                    Spinner argumentSpinner = new Spinner(composite, SWT.BORDER);
                    argumentSpinner.setMinimum(Integer.MIN_VALUE);
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
            } else if ("boolean".equals(argument.getType())) {
                Composite radios = new Composite(composite, SWT.NONE);
                radios.setData(argument);
                gl = new GridLayout(2, false);
                gl.marginHeight = 0;
                gl.marginWidth = 0;
                Button trueButton = new Button(radios, SWT.RADIO);
                trueButton.setText("true");
                Button falseButton = new Button(radios, SWT.RADIO);
                falseButton.setText("false");
                radios.setLayout(gl);
                if (command.isAssigned(argument.getArgumentInfo())) {
                    String stringValue = command.getAssignedStringValue(argument.getArgumentInfo());
                    trueButton.setSelection("true".equalsIgnoreCase(stringValue));
                    falseButton.setSelection("false".equalsIgnoreCase(stringValue));
                } else if (argument.getValue() != null) {
                    trueButton.setSelection("true".equalsIgnoreCase(argument.getValue()));
                    falseButton.setSelection("false".equalsIgnoreCase(argument.getValue()));
                }
                controls.add(radios);
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
            } else if (control instanceof Composite) { // boolean
                Button trueButton = (Button) ((Composite) control).getChildren()[0];
                Button falseButton = (Button) ((Composite) control).getChildren()[1];
                if (trueButton.getSelection()) {
                    assignments.put(argument.getArgumentInfo(), "true");
                } else if (falseButton.getSelection()) {
                    assignments.put(argument.getArgumentInfo(), "false");
                }
            } else {
                throw new UnsupportedOperationException("Unexpected control of type " + control.getClass());
            }
        }
        return assignments;
    }
}
