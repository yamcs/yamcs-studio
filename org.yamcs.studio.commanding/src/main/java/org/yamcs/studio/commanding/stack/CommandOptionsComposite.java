/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.core.YamcsPlugin;

public class CommandOptionsComposite extends ScrolledComposite {

    private Composite scrollpane;
    private Group argumentsGroup;
    private List<Control> controls = new ArrayList<>();
    private CommandOptionsValidityListener validityListener;

    public CommandOptionsComposite(Composite parent, int style, StackedCommand command,
            CommandOptionsValidityListener validityListener) {
        super(parent, style | SWT.V_SCROLL);

        this.validityListener = validityListener;

        scrollpane = new Composite(this, SWT.NONE);
        scrollpane.setLayout(new GridLayout());

        argumentsGroup = new Group(scrollpane, SWT.NONE);
        argumentsGroup.setText("Arguments");
        argumentsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        var gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        argumentsGroup.setLayout(gl);

        var allArguments = command.getEffectiveAssignments();

        List<TelecommandArgument> customArguments = allArguments
                .stream().filter(TelecommandArgument::isEditable).filter(
                        arg -> !arg.getArgumentInfo().hasInitialValue()
                                || command.isDefaultChanged(arg.getArgumentInfo()))
                .collect(Collectors.toList());

        List<TelecommandArgument> defaultArguments = allArguments
                .stream().filter(TelecommandArgument::isEditable).filter(
                        arg -> arg.getArgumentInfo().hasInitialValue()
                                && !command.isDefaultChanged(arg.getArgumentInfo()))
                .collect(Collectors.toList());

        if (customArguments.isEmpty()) {
            var lbl = new Label(argumentsGroup, SWT.NONE);
            lbl.setText("No arguments required");
        } else {
            addArgumentControls(argumentsGroup, command, customArguments);
        }

        if (!defaultArguments.isEmpty()) {
            var expandable = new ExpandableComposite(argumentsGroup,
                    ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
            expandable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            expandable.setLayout(new GridLayout(1, false));
            expandable.setExpanded(false);
            if (defaultArguments.size() == 1) {
                expandable.setText("1 argument with default");
            } else {
                expandable.setText(defaultArguments.size() + " arguments with defaults");
            }
            var defaultArgumentsComposite = new Composite(expandable, SWT.NONE);
            defaultArgumentsComposite.setLayout(new FillLayout());
            addArgumentControls(defaultArgumentsComposite, command, defaultArguments);
            expandable.setClient(defaultArgumentsComposite);
            expandable.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    resizeScrollpane();
                }
            });
        }

        var optionsGroup = new Group(scrollpane, SWT.NONE);
        optionsGroup.setText("Options");
        optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        optionsGroup.setLayout(new GridLayout(2, false));

        if (YamcsPlugin.hasSystemPrivilege("CommandOptions")) {
            for (var extra : YamcsPlugin.getServerInfo().getCommandOptionsList()) {
                var text = extra.hasVerboseName() ? extra.getVerboseName() : extra.getId();
                if ("BOOLEAN".equals(extra.getType())) {
                    var label = new Label(optionsGroup, SWT.NONE);
                    label.setText(text);
                    var gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
                    label.setLayoutData(gridData);
                    var check = new Button(optionsGroup, SWT.CHECK);
                    if (extra.hasHelp()) {
                        label.setToolTipText(extra.getHelp());
                    }
                    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
                    check.setLayoutData(gridData);
                    var initialValue = command.getExtra().get(extra.getId());
                    if (initialValue != null) {
                        check.setSelection(initialValue.getBooleanValue());
                    }
                    check.addListener(SWT.Selection, evt -> {
                        command.setExtra(extra.getId(), Value.newBuilder().setType(Value.Type.BOOLEAN)
                                .setBooleanValue(check.getSelection()).build());
                    });
                } else {
                    var label = new Label(optionsGroup, SWT.NONE);
                    label.setText(text);
                    var gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
                    label.setLayoutData(gridData);
                    var input = new Text(optionsGroup, SWT.BORDER);
                    if (extra.hasHelp()) {
                        label.setToolTipText(extra.getHelp());
                    }
                    gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
                    input.setLayoutData(gridData);
                    var initialValue = command.getExtra().get(extra.getId());
                    if (initialValue != null) {
                        input.setText(initialValue.getStringValue());
                    }
                    input.addModifyListener(evt -> {
                        if (input.getText().isEmpty()) {
                            command.setExtra(extra.getId(), null);
                        } else {
                            command.setExtra(extra.getId(), Value.newBuilder().setType(Value.Type.STRING)
                                    .setStringValue(input.getText()).build());
                        }
                    });
                }
            }
        }

        // option for stack delay
        var l0 = new Label(optionsGroup, SWT.NONE);
        l0.setText("Issue Delay (ms)");
        var gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
        l0.setLayoutData(gridData);
        var delayMs = new Spinner(optionsGroup, SWT.BORDER);
        delayMs.setValues(command.getDelayMs(), 0, Integer.MAX_VALUE, 0, 1, 100);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        delayMs.setLayoutData(gridData);
        delayMs.addModifyListener(evt -> {
            command.setDelayMs(delayMs.getSelection());
        });

        // option for comment
        var l1 = new Label(optionsGroup, SWT.NONE);
        l1.setText("Comment");
        gridData = new GridData(SWT.NONE, SWT.TOP, false, false);
        l1.setLayoutData(gridData);
        var comment = new Text(optionsGroup, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
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

        resizeScrollpane();
        updateValidity();
    }

    private void resizeScrollpane() {
        var size = scrollpane.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        setMinSize(size);
        scrollpane.setSize(size);
    }

    private void addArgumentControls(Composite parent, StackedCommand command,
            Collection<TelecommandArgument> arguments) {
        var composite = new Composite(parent, SWT.NONE);
        var gl = new GridLayout(3, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        composite.setLayout(gl);
        for (var argument : arguments) {
            var argumentLabel = new Label(composite, SWT.NONE);
            argumentLabel.setText(argument.getName());
            if (argument.getArgumentInfo().hasDescription()) {
                argumentLabel.setToolTipText(argument.getArgumentInfo().getDescription());
            }

            if ("enumeration".equals(argument.getType())) {
                var argumentCombo = new Combo(composite, SWT.READ_ONLY);
                argumentCombo.setLayoutData(new GridData(150, SWT.DEFAULT));
                argumentCombo.setData(argument);
                for (var enumValue : argument.getArgumentInfo().getType().getEnumValueList()) {
                    argumentCombo.add(enumValue.getLabel());
                    argumentCombo.setData(enumValue.getLabel(), enumValue);
                }
                if (command.isAssigned(argument.getArgumentInfo())) {
                    var value = command.getAssignedStringValue(argument.getArgumentInfo());
                    argumentCombo.setText(value);
                } else if (argument.getValue() != null) {
                    argumentCombo.setText(argument.getValue());
                }
                controls.add(argumentCombo);
            } else if ("boolean".equals(argument.getType())) {
                var oneStringValue = argument.getArgumentInfo().getType().getOneStringValue();
                var zeroStringValue = argument.getArgumentInfo().getType().getZeroStringValue();
                var radios = new Composite(composite, SWT.NONE);
                radios.setData(argument);
                gl = new GridLayout(2, false);
                gl.marginHeight = 0;
                gl.marginWidth = 0;
                var trueButton = new Button(radios, SWT.RADIO);
                trueButton.setText(oneStringValue);
                var falseButton = new Button(radios, SWT.RADIO);
                falseButton.setText(zeroStringValue);
                radios.setLayout(gl);
                if (command.isAssigned(argument.getArgumentInfo())) {
                    var stringValue = command.getAssignedStringValue(argument.getArgumentInfo());
                    trueButton.setSelection(oneStringValue.equalsIgnoreCase(stringValue));
                    falseButton.setSelection(zeroStringValue.equalsIgnoreCase(stringValue));
                } else if (argument.getValue() != null) {
                    trueButton.setSelection(oneStringValue.equalsIgnoreCase(argument.getValue()));
                    falseButton.setSelection(zeroStringValue.equalsIgnoreCase(argument.getValue()));
                }
                controls.add(radios);
            } else {
                var argumentText = new Text(composite, SWT.BORDER);
                argumentText.setLayoutData(new GridData(150, SWT.DEFAULT));
                argumentText.setData(argument);
                if (command.isAssigned(argument.getArgumentInfo())) {
                    var value = command.getAssignedStringValue(argument.getArgumentInfo());
                    argumentText.setText(value);
                } else if (argument.getValue() != null) {
                    argumentText.setText(argument.getValue());
                }

                if ("integer".equals(argument.getType()) || "float".equals(argument.getType())) {
                    argumentText.addListener(SWT.Modify, evt -> updateValidity());
                }

                controls.add(argumentText);
            }

            var hintLabel = new Label(composite, SWT.BORDER);
            hintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            var hint = "";

            var argumentType = argument.getArgumentInfo().getType();
            if (argumentType.hasRangeMin() || argumentType.hasRangeMax()) {
                // For floats, %s has the advantage of stripping unnecessary zeroes after the decimal
                var format = "integer".equals(argumentType.getEngType()) ? "%.0f" : "%s";
                if (argumentType.hasRangeMin() && argumentType.hasRangeMax()) {
                    hint += "Value must be in range [" + String.format(Locale.US, format, argumentType.getRangeMin());
                    hint += ", " + String.format(Locale.US, format, argumentType.getRangeMax()) + "]";
                } else if (argumentType.hasRangeMax()) {
                    hint += "Value must be <= " + String.format(Locale.US, format, argumentType.getRangeMax());
                } else if (argumentType.hasRangeMin()) {
                    hint += "Value must be >= " + String.format(Locale.US, format, argumentType.getRangeMin());
                }
            }
            hintLabel.setText(hint);
        }
    }

    public void updateValidity() {
        String invalidMessage = null;

        for (var control : controls) {
            var argument = (TelecommandArgument) control.getData();
            if ("integer".equals(argument.getType())) {
                var text = ((Text) control).getText();
                BigDecimal value;
                try {
                    value = new BigDecimal(text);
                } catch (NumberFormatException e) {
                    invalidMessage = argument.getName() + " is not a valid integer";
                    break;
                }

                var argumentType = argument.getArgumentInfo().getType();
                if (argumentType.hasRangeMin()) {
                    var doubleValue = value.doubleValue();
                    if (doubleValue < argumentType.getRangeMin()) {
                        invalidMessage = argument.getName() + " is out of range";
                        break;
                    }
                }
                if (argumentType.hasRangeMax()) {
                    var doubleValue = value.doubleValue();
                    if (doubleValue > argumentType.getRangeMax()) {
                        invalidMessage = argument.getName() + " is out of range";
                        break;
                    }
                }
            } else if ("float".equals(argument.getType())) {
                var text = ((Text) control).getText();
                Double value;
                try {
                    value = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    invalidMessage = argument.getName() + " is not a valid float";
                    break;
                }

                var argumentType = argument.getArgumentInfo().getType();
                if (argumentType.hasRangeMin()) {
                    if (value < argumentType.getRangeMin()) {
                        invalidMessage = argument.getName() + " is out of range";
                        break;
                    }
                }
                if (argumentType.hasRangeMax()) {
                    if (value > argumentType.getRangeMax()) {
                        invalidMessage = argument.getName() + " is out of range";
                        break;
                    }
                }
            }
        }

        validityListener.validityUpdated(invalidMessage);
    }

    public Map<ArgumentInfo, String> getAssignments() {
        Map<ArgumentInfo, String> assignments = new HashMap<>();
        for (var control : controls) {
            var argument = (TelecommandArgument) control.getData();
            if (control instanceof Text) {
                var text = ((Text) control).getText();
                if (text != null && !text.isEmpty()) {
                    assignments.put(argument.getArgumentInfo(), text);
                }
            } else if (control instanceof Combo) {
                var text = ((Combo) control).getText();
                assignments.put(argument.getArgumentInfo(), text);
            } else if (control instanceof Composite) { // boolean
                var trueButton = (Button) ((Composite) control).getChildren()[0];
                var falseButton = (Button) ((Composite) control).getChildren()[1];
                if (trueButton.getSelection()) {
                    var oneStringValue = argument.getArgumentInfo().getType().getOneStringValue();
                    assignments.put(argument.getArgumentInfo(), oneStringValue);
                } else if (falseButton.getSelection()) {
                    var zeroStringValue = argument.getArgumentInfo().getType().getZeroStringValue();
                    assignments.put(argument.getArgumentInfo(), zeroStringValue);
                }
            } else {
                throw new UnsupportedOperationException("Unexpected control of type " + control.getClass());
            }
        }
        return assignments;
    }
}
