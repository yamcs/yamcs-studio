package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.client.Command;
import org.yamcs.client.CommandSubscription;
import org.yamcs.client.YamcsClient;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.SubscribeCommandsRequest;
import org.yamcs.studio.commanding.stack.CommandStack.AutoMode;
import org.yamcs.studio.commanding.stack.CommandStack.StackMode;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
import org.yamcs.studio.core.Privileges;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.ui.connections.ConnectionStateProvider;
import org.yamcs.studio.core.utils.RCPUtils;

public class CommandStackView extends ViewPart implements YamcsAware {

    public static final String ID = "org.yamcs.studio.commanding.stack.CommandStackView";
    private static final Logger log = Logger.getLogger(CommandStackView.class.getName());

    private CommandSubscription subscription;

    private CommandStackTableViewer commandTableViewer;
    private ConnectionStateProvider connectionStateProvider;

    private ISourceProviderListener sourceProviderListener = new ISourceProviderListener() {
        @Override
        public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
            refreshState();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
            refreshState();
        }
    };

    private Color errorBackgroundColor;
    private Styler bracketStyler;
    private Styler argNameStyler;
    private Styler numberStyler;
    private Styler errorStyler;
    private Styler issuedStyler;
    private Styler skippedStyler;

    private Label messageLabel;
    private Button armButton;
    private Button issueButton;

    private ResourceManager resourceManager;
    private Image level0Image;
    private Image level1Image;
    private Image level2Image;
    private Image level3Image;
    private Image level4Image;
    private Image level5Image;

    private Composite bottomLeft;
    private Label clearanceLabel;
    private Label clearanceImageLabel;
    private Label clearanceSeparator;

    private Spinner fixDelaySpinner;

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        level0Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level0s.png"));
        level1Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level1s.png"));
        level2Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level2s.png"));
        level3Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level3s.png"));
        level4Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level4s.png"));
        level5Image = resourceManager
                .createImage(RCPUtils.getImageDescriptor(CommandStackTableViewer.class, "icons/level5s.png"));

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 1;
        parent.setLayout(gl);

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        errorBackgroundColor = resourceManager.createColor(new RGB(255, 221, 221));
        bracketStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
            }
        };
        argNameStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
            }
        };
        numberStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
            }
        };
        errorStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getTextFont();
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
                textStyle.underline = true;
                textStyle.underlineColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
                textStyle.underlineStyle = SWT.UNDERLINE_ERROR;
            }
        };
        issuedStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT);
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
            }
        };
        skippedStyler = new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
                textStyle.font = JFaceResources.getFontRegistry().getItalic(JFaceResources.TEXT_FONT);
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
                textStyle.strikeout = true;
                textStyle.strikeoutColor = textStyle.foreground;
            }
        };

        Composite tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(tableWrapper, tcl, this);
        commandTableViewer.addDoubleClickListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                StackedCommand cmd = (StackedCommand) sel.getFirstElement();
                if (cmd.getStackedState() != StackedState.ISSUED && cmd.getStackedState() != StackedState.SKIPPED) {
                    EditStackedCommandDialog dialog = new EditStackedCommandDialog(parent.getShell(), cmd);
                    if (dialog.open() == Window.OK) {
                        refreshState();
                    }
                }
            }
        });

        Composite controls = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 30;
        controls.setLayoutData(gd);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        controls.setLayout(gl);
        controls.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        bottomLeft = new Composite(controls, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        bottomLeft.setLayoutData(gd);

        gl = new GridLayout(4, false);
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        bottomLeft.setLayout(gl);

        clearanceLabel = new Label(bottomLeft, SWT.NONE);
        clearanceLabel.setVisible(false);

        clearanceImageLabel = new Label(bottomLeft, SWT.NONE);
        clearanceImageLabel.setVisible(false);

        clearanceSeparator = new Label(bottomLeft, SWT.SEPARATOR);
        gd = new GridData();
        gd.heightHint = 15;
        clearanceSeparator.setLayoutData(gd);
        clearanceSeparator.setVisible(false);

        messageLabel = new Label(bottomLeft, SWT.NONE);
        messageLabel.setText("");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomRight = new Composite(controls, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        gd.horizontalAlignment = SWT.RIGHT;
        bottomRight.setLayoutData(gd);
        gl = new GridLayout(4, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        bottomRight.setLayout(gl);

        // Stack parameters (manual/auto stack)
        Composite stackParameters = new Composite(bottomRight, SWT.NONE);
        gl = new GridLayout(5, false);
        gl.marginHeight = 0;
        gl.marginWidth = 50;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        stackParameters.setLayout(gl);
        // Group stackTypeGroup = new Group(stackParameters, SWT.NONE);
        // stackTypeGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Label label = new Label(stackParameters, SWT.NONE);
        label.setText("Stack mode: ");

        final Combo comboStackType = new Combo(stackParameters, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] items = new String[] { "Manual", "Automatic" };
        comboStackType.setItems(items);
        comboStackType.select(0);

        Combo comboAutoParameters = new Combo(stackParameters, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] items2 = new String[] { "AFAP (no delays)", "Fix Delays", "Stack Delays" };
        comboAutoParameters.setItems(items2);
        comboAutoParameters.select(0);
        comboAutoParameters.setVisible(false);

        fixDelaySpinner = new Spinner(stackParameters, SWT.BORDER);
        fixDelaySpinner.setMinimum(0);
        fixDelaySpinner.setMaximum(Integer.MAX_VALUE);
        fixDelaySpinner.setSelection(200);
        fixDelaySpinner.setIncrement(1);
        fixDelaySpinner.setPageIncrement(100);
        fixDelaySpinner.setVisible(false);
        fixDelaySpinner.addListener(SWT.Selection, evt -> {
            CommandStack stack = CommandStack.getInstance();
            stack.fixDelayMs = fixDelaySpinner.getSelection();
        });

        Label labelUnit = new Label(stackParameters, SWT.NONE);
        labelUnit.setText("ms");
        labelUnit.setVisible(false);
        comboStackType.addListener(SWT.Selection, evt -> {
            CommandStack stack = CommandStack.getInstance();
            int index = comboStackType.getSelectionIndex();
            if (index == StackMode.MANUAL.index()) {
                // Manual
                stack.stackMode = StackMode.MANUAL;
                comboAutoParameters.setVisible(false);
                fixDelaySpinner.setVisible(false);
                labelUnit.setVisible(false);
                commandTableViewer.hideDelayColumn();
            } else {
                // Automatic
                stack.stackMode = StackMode.AUTOMATIC;
                comboAutoParameters.setVisible(true);
                if (comboAutoParameters.getSelectionIndex() == AutoMode.FIX_DELAY.index()) {
                    // fix delay
                    fixDelaySpinner.setVisible(true);
                    labelUnit.setVisible(true);
                    commandTableViewer.hideDelayColumn();
                } else if (comboAutoParameters.getSelectionIndex() == AutoMode.STACK_DELAYS.index()) {
                    // stack delays
                    fixDelaySpinner.setVisible(false);
                    labelUnit.setVisible(false);
                    commandTableViewer.showDelayColumn();
                } else {
                    // afap
                    fixDelaySpinner.setVisible(false);
                    labelUnit.setVisible(false);
                    commandTableViewer.hideDelayColumn();
                }
            }
            // disarm the stack
            setButtonEnable(issueButton, false);
            armButton.setSelection(false);
            stack.disarmArmed();
            refreshState();

        });
        comboAutoParameters.addListener(SWT.Selection, evt -> {
            CommandStack stack = CommandStack.getInstance();
            if (stack.stackMode == StackMode.AUTOMATIC) {
                if (comboAutoParameters.getSelectionIndex() == AutoMode.FIX_DELAY.index()) {
                    // fix delay
                    fixDelaySpinner.setVisible(true);
                    labelUnit.setVisible(true);
                    commandTableViewer.hideDelayColumn();
                } else if (comboAutoParameters.getSelectionIndex() == AutoMode.STACK_DELAYS.index()) {
                    // stack delay
                    fixDelaySpinner.setVisible(false);
                    labelUnit.setVisible(false);
                    commandTableViewer.showDelayColumn();
                } else if (comboAutoParameters.getSelectionIndex() == AutoMode.AFAP.index()) {
                    // fix delay
                    fixDelaySpinner.setVisible(false);
                    labelUnit.setVisible(false);
                    commandTableViewer.hideDelayColumn();
                }
            } else {
                // manual mode
                fixDelaySpinner.setVisible(false);
                labelUnit.setVisible(false);
                commandTableViewer.hideDelayColumn();
            }
            if (comboAutoParameters.getSelectionIndex() == AutoMode.FIX_DELAY.index()) {
                stack.autoMode = AutoMode.FIX_DELAY;
            } else if (comboAutoParameters.getSelectionIndex() == AutoMode.STACK_DELAYS.index()) {
                stack.autoMode = AutoMode.STACK_DELAYS;
            } else {
                stack.autoMode = AutoMode.AFAP;
            }
        });

        armButton = new Button(bottomRight, SWT.TOGGLE);
        armButton.setText("1. Arm");
        armButton.setToolTipText("Arm the selected command");
        armButton.setEnabled(false);
        armButton.addListener(SWT.Selection, evt -> {
            CommandStack stack = CommandStack.getInstance();
            if (armButton.getSelection()) {
                ICommandService commandService = (ICommandService) getViewSite().getService(ICommandService.class);
                IEvaluationService evaluationService = (IEvaluationService) getViewSite()
                        .getService(IEvaluationService.class);

                if (stack.stackMode == StackMode.AUTOMATIC) {
                    // automatic stack, arm all commands
                    org.eclipse.core.commands.Command cmd = commandService
                            .getCommand("org.yamcs.studio.commanding.stack.armAll");
                    try {
                        cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null,
                                evaluationService.getCurrentState()));
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Could not execute command", e);
                    }
                } else {
                    // manual stack, individual arm
                    org.eclipse.core.commands.Command cmd = commandService
                            .getCommand("org.yamcs.studio.commanding.stack.arm");
                    try {
                        cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null,
                                evaluationService.getCurrentState()));
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Could not execute command", e);
                    }
                }

            } else {
                if (stack.getActiveCommand() != null && stack.getActiveCommand().isArmed()) {
                    stack.getActiveCommand().setStackedState(StackedState.DISARMED);
                    clearArm();
                    refreshState();
                }
            }
        });

        issueButton = new Button(bottomRight, SWT.NONE);
        issueButton.setText("2. Issue");
        issueButton.setToolTipText("Issue the selected command");
        issueButton.setEnabled(false);
        issueButton.addListener(SWT.Selection, evt -> {
            CommandStack stack = CommandStack.getInstance();
            ICommandService commandService = (ICommandService) getViewSite().getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) getViewSite()
                    .getService(IEvaluationService.class);

            org.eclipse.core.commands.Command cmd = null;
            if (stack.stackMode == StackMode.AUTOMATIC) {
                // automatic stack - issue all commands
                cmd = commandService.getCommand("org.yamcs.studio.commanding.stack.issueAll");
            } else {
                // manual stack - issue one command
                cmd = commandService.getCommand("org.yamcs.studio.commanding.stack.issue");
            }
            try {
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null,
                        evaluationService.getCurrentState()));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not execute command", e);
            }

        });

        commandTableViewer.addSelectionChangedListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            updateMessagePanel(sel);
            CommandStack stack = CommandStack.getInstance();
            armButton.setSelection(false);
            stack.disarmArmed();
            if (sel.isEmpty() || !stack.isValid() || !sel.getFirstElement().equals(stack.getActiveCommand())) {
                armButton.setEnabled(false);
                issueButton.setEnabled(false);
            } else if (stack.hasRemaining()) {
                setButtonEnable(armButton, true);
            }

            refreshState();
        });

        getViewSite().setSelectionProvider(commandTableViewer);

        // Set up connection state, and listen to changes
        connectionStateProvider = RCPUtils.findSourceProvider(getViewSite(),
                ConnectionStateProvider.STATE_KEY_CONNECTED,
                ConnectionStateProvider.class);
        connectionStateProvider.addSourceProviderListener(sourceProviderListener);

        // Add the popup menu for pasting commands
        addPopupMenu();

        // Set initial state
        refreshState();

        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        if (subscription != null) {
            subscription.cancel(true);
        }

        if (processor != null) {
            YamcsClient client = YamcsPlugin.getYamcsClient();
            subscription = client.createCommandSubscription();
            subscription.addListener(command -> {
                Display.getDefault().asyncExec(() -> processCommand(command));
            });
            subscription.sendMessage(SubscribeCommandsRequest.newBuilder()
                    .setInstance(instance)
                    .setProcessor(processor)
                    .build());
        }
    }

    @Override
    public void updateClearance(boolean enabled, SignificanceLevelType level) {
        Display.getDefault().asyncExec(() -> {
            if (!enabled) {
                clearanceLabel.setText("");
                clearanceLabel.setVisible(false);
                clearanceImageLabel.setImage(null);
                clearanceImageLabel.setVisible(false);
                clearanceSeparator.setVisible(false);
            } else if (level == null) {
                clearanceLabel.setVisible(true);
                clearanceLabel.setText("No clearance");
                clearanceImageLabel.setImage(null);
                clearanceImageLabel.setVisible(false);
                clearanceSeparator.setVisible(true);
            } else {
                clearanceLabel.setText("Clearance:");
                clearanceLabel.setVisible(true);
                clearanceImageLabel.setVisible(true);
                clearanceSeparator.setVisible(true);
                switch (level) {
                case NONE:
                    clearanceImageLabel.setImage(level0Image);
                    break;
                case WATCH:
                    clearanceImageLabel.setImage(level1Image);
                    break;
                case WARNING:
                    clearanceImageLabel.setImage(level2Image);
                    break;
                case DISTRESS:
                    clearanceImageLabel.setImage(level3Image);
                    break;
                case CRITICAL:
                    clearanceImageLabel.setImage(level4Image);
                    break;
                case SEVERE:
                    clearanceImageLabel.setImage(level5Image);
                    break;
                }
            }
            bottomLeft.layout(true);
        });
    }

    public void selectFirst() {
        CommandStack stack = CommandStack.getInstance();
        if (!stack.isEmpty()) {
            StructuredSelection sel = new StructuredSelection(stack.getCommands().get(0));
            commandTableViewer.setSelection(sel, true);
        }
    }

    public void selectActiveCommand() {
        CommandStack stack = CommandStack.getInstance();
        if (stack.hasRemaining()) {
            StructuredSelection sel = new StructuredSelection(stack.getActiveCommand());
            commandTableViewer.setSelection(sel, true);
        }
    }

    private void updateMessagePanel(IStructuredSelection sel) {
        if (!sel.isEmpty()) {
            for (Object element : sel.toArray()) {
                StackedCommand cmd = (StackedCommand) element;
                if (!cmd.isValid()) {
                    messageLabel.setText(cmd.getMessages().get(0));
                    return;
                }
            }
        }

        if (!CommandStack.getInstance().isValid()) {
            messageLabel.setText("Stack contains errors");
        } else {
            messageLabel.setText("");
        }
    }

    public Color getErrorBackgroundColor() {
        return errorBackgroundColor;
    }

    public Styler getIdentifierStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        } else if (cmd.getStackedState() == StackedState.SKIPPED) {
            return skippedStyler;
        }

        return null;
    }

    public Styler getBracketStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        } else if (cmd.getStackedState() == StackedState.SKIPPED) {
            return skippedStyler;
        }

        return bracketStyler;
    }

    public Styler getArgNameStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        } else if (cmd.getStackedState() == StackedState.SKIPPED) {
            return skippedStyler;
        }

        return argNameStyler;
    }

    public Styler getNumberStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        } else if (cmd.getStackedState() == StackedState.SKIPPED) {
            return skippedStyler;
        }

        return numberStyler;
    }

    public Styler getErrorStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        } else if (cmd.getStackedState() == StackedState.SKIPPED) {
            return skippedStyler;
        }

        return errorStyler;
    }

    public void addTelecommand(StackedCommand command) {
        commandTableViewer.addTelecommand(command);
        selectActiveCommand();
    }

    /**
     * Clear state of the arm button TODO should probably be refactored into refreshState instead
     */
    public void clearArm() {
        armButton.setSelection(false);
    }

    public void refreshState() {
        commandTableViewer.refresh();
        CommandStack stack = CommandStack.getInstance();

        IStructuredSelection sel = (IStructuredSelection) commandTableViewer.getSelection();
        updateMessagePanel(sel);
        if (connectionStateProvider.isConnected() && !sel.isEmpty()) {
            StackedCommand selectedCommand = (StackedCommand) sel.getFirstElement();
            if (selectedCommand == stack.getActiveCommand()) {
                if (stack.stackMode == StackMode.MANUAL && selectedCommand.isArmed()
                        || stack.stackMode == StackMode.AUTOMATIC && stack.areAllCommandsArmed()) {
                    setButtonEnable(armButton, true);
                    setButtonEnable(issueButton, armButton.getSelection());
                } else if (stack.isValid()) {
                    setButtonEnable(armButton, true);
                    setButtonEnable(issueButton, false);
                } else {
                    setButtonEnable(armButton, false);
                    setButtonEnable(issueButton, false);
                }
            } else {
                stack.disarmArmed();
                setButtonEnable(armButton, false);
                setButtonEnable(armButton, false);
                setButtonEnable(issueButton, false);
            }
        } else {
            stack.disarmArmed();
            setButtonEnable(armButton, false);
            setButtonEnable(armButton, false);
            setButtonEnable(issueButton, false);
        }

        // State for plugin.xml handlers
        CommandStackStateProvider executionStateProvider = RCPUtils.findSourceProvider(
                getSite(), CommandStackStateProvider.STATE_KEY_ARMED, CommandStackStateProvider.class);
        executionStateProvider.refreshState(CommandStack.getInstance());
    }

    // Enable the buttons if user is authorized to command payload
    private void setButtonEnable(Button button, boolean isEnabled) {
        if (YamcsPlugin.hasSystemPrivilege(Privileges.Command)) {
            button.setEnabled(isEnabled);
        } else {
            button.setEnabled(false);
        }
    }

    @Override
    public void setFocus() {
        commandTableViewer.getTable().setFocus();
    }

    // On the GUI thread
    private void processCommand(Command command) {
        for (StackedCommand cmd : CommandStack.getInstance().getCommands()) {
            if (command.getId().equals(cmd.getCommandId())) {
                cmd.updateExecutionState(command);
            }
        }
        commandTableViewer.refresh();
    }

    public Command getCommandExecution(String id) {
        return subscription.getCommand(id);
    }

    enum PastingType {
        BEFORE_ITEM,
        AFTER_ITEM,
        APPEND
    }

    private void addPopupMenu() {

        class CopySelectionListener implements SelectionListener {
            boolean cut = false;

            public CopySelectionListener(boolean cut) {
                this.cut = cut;
            }

            @Override
            public void widgetSelected(SelectionEvent event) {
                // check something is selected
                TableItem[] selection = commandTableViewer.getTable().getSelection();
                if (selection == null || selection.length == 0) {
                    return;
                }

                // copy each selected items
                List<StackedCommand> scs = new ArrayList<>();
                for (TableItem ti : selection) {
                    StackedCommand sc = (StackedCommand) (ti.getData());
                    if (sc == null) {
                        continue;
                    }
                    scs.add(sc);

                }
                CommandClipboard.addStackedCommands(scs, cut, event.display);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        }

        class PasteSelectionListener implements SelectionListener {
            PastingType pastingType;

            public PasteSelectionListener(PastingType pastingType) {
                this.pastingType = pastingType;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent event) {
                StackedCommand sc = null;

                // sanity checks
                TableItem[] selection = commandTableViewer.getTable().getSelection();
                if (selection != null && selection.length > 0) {
                    sc = (StackedCommand) (commandTableViewer.getTable().getSelection()[0].getData());
                }
                if (sc == null && pastingType != PastingType.APPEND) {
                    pastingType = PastingType.APPEND;
                }

                // get commands from clipboard
                List<StackedCommand> copyedCommands = new ArrayList<>();
                try {
                    copyedCommands = CommandClipboard.getCopiedCommands();
                } catch (Exception e) {
                    String errorMessage = "Unable to build Stacked Command from the specifed source: ";
                    log.log(Level.WARNING, errorMessage, e);
                    commandTableViewer.getTable().getDisplay().asyncExec(() -> {
                        MessageBox dialog = new MessageBox(commandTableViewer.getTable().getShell(),
                                SWT.ICON_ERROR | SWT.OK);
                        dialog.setText("Command Stack Edition");
                        dialog.setMessage(errorMessage + e.getMessage());
                        // open dialog and await user selection
                        dialog.open();
                    });
                    return;
                }
                if (copyedCommands.isEmpty()) {
                    return;
                }

                // paste
                int index = commandTableViewer.getIndex(sc);
                for (StackedCommand pastedCommand : copyedCommands) {
                    if (pastingType == PastingType.APPEND) {
                        commandTableViewer.addTelecommand(pastedCommand);
                    } else if (pastingType == PastingType.AFTER_ITEM) {
                        commandTableViewer.insertTelecommand(pastedCommand, index + selection.length);
                    } else if (pastingType == PastingType.BEFORE_ITEM) {
                        commandTableViewer.insertTelecommand(pastedCommand, index);
                    }
                    index++;
                }

                // delete cut commands
                CommandStack.getInstance().getCommands().removeAll(CommandClipboard.getCutCommands());

                // refresh command stack view state
                IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .findView(CommandStackView.ID);
                CommandStackView commandStackView = (CommandStackView) part;
                commandStackView.refreshState();
            }
        }

        Table table = commandTableViewer.getTable();
        Menu contextMenu = new Menu(commandTableViewer.getTable());
        table.setMenu(contextMenu);

        MenuItem mItemCopy = new MenuItem(contextMenu, SWT.None);
        mItemCopy.setText("Copy");
        mItemCopy.addSelectionListener(new CopySelectionListener(false));

        MenuItem mItemCut = new MenuItem(contextMenu, SWT.None);
        mItemCut.setText("Cut");
        mItemCut.addSelectionListener(new CopySelectionListener(true));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        MenuItem mItemPasteBefore = new MenuItem(contextMenu, SWT.None);
        mItemPasteBefore.setText("Paste Before");
        mItemPasteBefore.addSelectionListener(new PasteSelectionListener(PastingType.BEFORE_ITEM));

        MenuItem mItemPasteAfter = new MenuItem(contextMenu, SWT.None);
        mItemPasteAfter.setText("Paste After");
        mItemPasteAfter.addSelectionListener(new PasteSelectionListener(PastingType.AFTER_ITEM));

        MenuItem mItemPaste = new MenuItem(contextMenu, SWT.None);
        mItemPaste.setText("Paste Append");
        mItemPaste.addSelectionListener(new PasteSelectionListener(PastingType.APPEND));

        commandTableViewer.getTable().addListener(SWT.MouseDown, event -> {
            TableItem[] selection = commandTableViewer.getTable().getSelection();

            boolean pastBeforeAuthorized = true;
            boolean pastAfterAuthorized = true;

            StackedCommand sc1 = null;
            StackedCommand sc2 = null;
            if (selection.length > 0) {
                // prevent to edit part of the command stack that has already been executed
                sc1 = (StackedCommand) selection[0].getData();
                sc2 = (StackedCommand) selection[selection.length - 1].getData();
                int lastSelectionIndex = commandTableViewer.getIndex(sc2);
                sc2 = (StackedCommand) commandTableViewer.getElementAt(lastSelectionIndex + 1);

                pastBeforeAuthorized = sc1 != null && sc1.getStackedState() == StackedState.DISARMED;
                pastAfterAuthorized = sc2 == null || sc2.getStackedState() == StackedState.DISARMED;

            } else {
                pastBeforeAuthorized = false;
                pastAfterAuthorized = false;
            }

            if (event.button == 3) {
                contextMenu.setVisible(true);

                mItemCopy.setEnabled(selection.length != 0);
                mItemCut.setEnabled(selection.length != 0);

                mItemPaste.setEnabled(CommandClipboard.hasData());
                mItemPasteBefore.setEnabled(CommandClipboard.hasData() && pastBeforeAuthorized);
                mItemPasteAfter.setEnabled(CommandClipboard.hasData() && pastAfterAuthorized);

            } else {
                contextMenu.setVisible(false);
            }

        });
    }

    @Override
    public void dispose() {
        if (resourceManager != null) {
            resourceManager.dispose();
        }
        if (subscription != null) {
            subscription.cancel(true);
        }
        connectionStateProvider.removeSourceProviderListener(sourceProviderListener);
        YamcsPlugin.removeListener(this);
        super.dispose();
    }
}
