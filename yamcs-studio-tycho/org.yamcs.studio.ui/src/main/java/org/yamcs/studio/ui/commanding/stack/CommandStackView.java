package org.yamcs.studio.ui.commanding.stack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.studio.ui.ConnectionStateProvider;
import org.yamcs.studio.ui.RCPUtils;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;

public class CommandStackView extends ViewPart {

    public static final String ID = "org.yamcs.studio.ui.commanding.stack.CommandStackView";
    private static final Logger log = Logger.getLogger(CommandStackView.class.getName());

    private CommandStackTableViewer commandTableViewer;
    private ConnectionStateProvider connectionStateProvider;

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

    @Override
    public void createPartControl(Composite parent) {
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
                if (cmd.getState() != State.ISSUED && cmd.getState() != State.SKIPPED) {
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

        Composite bottomLeft = new Composite(controls, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        bottomLeft.setLayoutData(gd);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        bottomLeft.setLayout(gl);
        messageLabel = new Label(bottomLeft, SWT.NONE);
        messageLabel.setText("");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomRight = new Composite(controls, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.verticalAlignment = SWT.CENTER;
        gd.horizontalAlignment = SWT.RIGHT;
        bottomRight.setLayoutData(gd);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        bottomRight.setLayout(gl);

        armButton = new Button(bottomRight, SWT.TOGGLE);
        armButton.setText("1. Arm");
        armButton.setToolTipText("Arm the selected command");
        armButton.setEnabled(false);
        armButton.addListener(SWT.Selection, evt -> {
            if (armButton.getSelection()) {
                ICommandService commandService = (ICommandService) getViewSite().getService(ICommandService.class);
                IEvaluationService evaluationService = (IEvaluationService) getViewSite().getService(IEvaluationService.class);
                Command cmd = commandService.getCommand("org.yamcs.studio.ui.commanding.stack.arm");
                try {
                    cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Could not execute command", e);
                }
            } else {
                CommandStack stack = CommandStack.getInstance();
                if (stack.getActiveCommand() != null && stack.getActiveCommand().isArmed()) {
                    stack.getActiveCommand().setState(State.UNARMED);
                }
            }
        });

        issueButton = new Button(bottomRight, SWT.NONE);
        issueButton.setText("2. Issue");
        issueButton.setToolTipText("Issue the selected command");
        issueButton.setEnabled(false);
        issueButton.addListener(SWT.Selection, evt -> {
            ICommandService commandService = (ICommandService) getViewSite().getService(ICommandService.class);
            IEvaluationService evaluationService = (IEvaluationService) getViewSite().getService(IEvaluationService.class);
            Command cmd = commandService.getCommand("org.yamcs.studio.ui.commanding.stack.issue");
            try {
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
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
                armButton.setEnabled(true);
            }

            refreshState();
        });

        getViewSite().setSelectionProvider(commandTableViewer);

        // Set up connection state, and listen to changes
        connectionStateProvider = RCPUtils.findSourceProvider(getViewSite(), ConnectionStateProvider.STATE_KEY_CONNECTED,
                ConnectionStateProvider.class);
        connectionStateProvider.addSourceProviderListener(new ISourceProviderListener() {
            @Override
            public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
                refreshState();
            }

            @Override
            @SuppressWarnings("rawtypes")
            public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
                refreshState();
            }
        });

        // Set initial state
        refreshState();
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

        if (!CommandStack.getInstance().isValid())
            messageLabel.setText("Stack contains errors");
        else
            messageLabel.setText("");
    }

    public Color getErrorBackgroundColor() {
        return errorBackgroundColor;
    }

    public Styler getIdentifierStyler(StackedCommand cmd) {
        if (cmd.getState() == State.ISSUED)
            return issuedStyler;
        else if (cmd.getState() == State.SKIPPED)
            return skippedStyler;

        return null;
    }

    public Styler getBracketStyler(StackedCommand cmd) {
        if (cmd.getState() == State.ISSUED)
            return issuedStyler;
        else if (cmd.getState() == State.SKIPPED)
            return skippedStyler;

        return bracketStyler;
    }

    public Styler getArgNameStyler(StackedCommand cmd) {
        if (cmd.getState() == State.ISSUED)
            return issuedStyler;
        else if (cmd.getState() == State.SKIPPED)
            return skippedStyler;

        return argNameStyler;
    }

    public Styler getNumberStyler(StackedCommand cmd) {
        if (cmd.getState() == State.ISSUED)
            return issuedStyler;
        else if (cmd.getState() == State.SKIPPED)
            return skippedStyler;

        return numberStyler;
    }

    public Styler getErrorStyler(StackedCommand cmd) {
        if (cmd.getState() == State.ISSUED)
            return issuedStyler;
        else if (cmd.getState() == State.SKIPPED)
            return skippedStyler;

        return errorStyler;
    }

    public void addTelecommand(StackedCommand command) {
        commandTableViewer.addTelecommand(command);
        refreshState();
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
                if (selectedCommand.isArmed()) {
                    armButton.setEnabled(true);
                    issueButton.setEnabled(armButton.getSelection());
                } else if (stack.isValid()) {
                    armButton.setEnabled(true);
                    issueButton.setEnabled(false);
                } else {
                    armButton.setEnabled(false);
                    issueButton.setEnabled(false);
                }
            } else {
                stack.disarmArmed();
                armButton.setEnabled(false);
                armButton.setSelection(false);
                issueButton.setEnabled(false);
            }
        } else {
            stack.disarmArmed();
            armButton.setEnabled(false);
            armButton.setSelection(false);
            issueButton.setEnabled(false);
        }

        // State for plugin.xml handlers
        CommandStackStateProvider executionStateProvider = RCPUtils.findSourceProvider(
                getSite(), CommandStackStateProvider.STATE_KEY_ARMED, CommandStackStateProvider.class);
        executionStateProvider.refreshState(CommandStack.getInstance());
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
