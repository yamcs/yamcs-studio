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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.client.Command;
import org.yamcs.client.CommandSubscription;
import org.yamcs.protobuf.Mdb.SignificanceInfo.SignificanceLevelType;
import org.yamcs.protobuf.SubscribeCommandsRequest;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;
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

    private Label messageLabel;
    private Button armButton;
    private Button runButton;

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

    private Spinner waitTimeSpinner;

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

        var gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 1;
        parent.setLayout(gl);

        var resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
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

        var tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(getViewSite(), tableWrapper, tcl, this);
        commandTableViewer.addDoubleClickListener(evt -> {
            var sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                var cmd = (StackedCommand) sel.getFirstElement();
                if (cmd.getStackedState() != StackedState.ISSUED) {
                    var dialog = new EditStackedCommandDialog(parent.getShell(), cmd);
                    if (dialog.open() == Window.OK) {
                        refreshState();
                    }
                }
            }
        });

        var controls = new Composite(parent, SWT.NONE);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
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

        var bottomRight = new Composite(controls, SWT.NONE);
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

        // Stack parameters
        var stackParameters = new Composite(bottomRight, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 50;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        stackParameters.setLayout(gl);

        var labelUnit = new Label(stackParameters, SWT.NONE);
        labelUnit.setText("Wait (ms)");

        waitTimeSpinner = new Spinner(stackParameters, SWT.BORDER);
        waitTimeSpinner.setMinimum(0);
        waitTimeSpinner.setMaximum(Integer.MAX_VALUE);

        var initialWait = CommandingPlugin.getDefault().getDefaultStackWait();
        waitTimeSpinner.setSelection(Math.max(0, initialWait));
        waitTimeSpinner.setIncrement(500);
        waitTimeSpinner.setPageIncrement(1000);
        waitTimeSpinner.setEnabled(false);
        waitTimeSpinner.addListener(SWT.Selection, evt -> {
            var stack = CommandStack.getInstance();
            stack.setWaitTime(waitTimeSpinner.getSelection());
        });

        armButton = new Button(bottomRight, SWT.PUSH);
        armButton.setText("Arm");
        armButton.setToolTipText("Arm the selected command");
        armButton.setEnabled(false);
        armButton.addListener(SWT.Selection, evt -> {
            var commandService = getViewSite().getService(ICommandService.class);
            var evaluationService = getViewSite().getService(IEvaluationService.class);

            var cmd = commandService.getCommand("org.yamcs.studio.commanding.stack.arm");
            try {
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<>(), null,
                        evaluationService.getCurrentState()));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not run command", e);
            }
        });

        runButton = new Button(bottomRight, SWT.PUSH);
        runButton.setText("Run");
        runButton.setToolTipText("Run the selected command");
        runButton.setEnabled(false);
        runButton.addListener(SWT.Selection, evt -> {
            runButton.setEnabled(false);
            var commandService = getViewSite().getService(ICommandService.class);
            var evaluationService = getViewSite().getService(IEvaluationService.class);

            var cmd = commandService.getCommand("org.yamcs.studio.commanding.stack.issue");
            try {
                cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<>(), null,
                        evaluationService.getCurrentState()));
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not run command", e);
            }
        });

        commandTableViewer.addSelectionChangedListener(evt -> {
            refreshState();
        });

        getViewSite().setSelectionProvider(commandTableViewer);

        // Set up connection state, and listen to changes
        connectionStateProvider = RCPUtils.findSourceProvider(getViewSite(),
                ConnectionStateProvider.STATE_KEY_CONNECTED, ConnectionStateProvider.class);
        connectionStateProvider.addSourceProviderListener(sourceProviderListener);

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
            var client = YamcsPlugin.getYamcsClient();
            subscription = client.createCommandSubscription();
            subscription.addListener(command -> {
                Display.getDefault().asyncExec(() -> processCommand(command));
            });
            subscription.sendMessage(
                    SubscribeCommandsRequest.newBuilder().setInstance(instance).setProcessor(processor).build());
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
        var stack = CommandStack.getInstance();
        if (!stack.isEmpty()) {
            var sel = new StructuredSelection(stack.getCommands().get(0));
            commandTableViewer.setSelection(sel, true);
        }
    }

    public StackedCommand findNextCommand() {
        var stack = CommandStack.getInstance();
        if (!stack.isEmpty()) {
            var allCommands = stack.getCommands();
            var currentSelection = (IStructuredSelection) commandTableViewer.getSelection();
            if (currentSelection.isEmpty()) {
                return allCommands.get(0);
            } else {
                var selected = currentSelection.toArray();
                var lastSelected = selected[selected.length - 1];
                var idx = allCommands.indexOf(lastSelected);
                if (idx != -1 && idx != allCommands.size() - 1) {
                    return allCommands.get(idx + 1);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public void selectNextCommand() {
        var next = findNextCommand();
        selectCommand(next);
    }

    public void selectNextCommand(StackedCommand command) {
        var stack = CommandStack.getInstance();
        var allCommands = stack.getCommands();
        var idx = allCommands.indexOf(command);
        if (idx != -1 && idx != allCommands.size() - 1) {
            selectCommand(allCommands.get(idx + 1));
        } else {
            selectCommand(null);
        }
    }

    public void selectCommand(StackedCommand command) {
        if (command == null) {
            commandTableViewer.setSelection(null);
        } else {
            var sel = new StructuredSelection(command);
            commandTableViewer.setSelection(sel, true);
        }
    }

    private void updateMessagePanel(IStructuredSelection sel) {
        if (!sel.isEmpty()) {
            for (var element : sel.toArray()) {
                var cmd = (StackedCommand) element;
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
        }

        return null;
    }

    public Styler getBracketStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        }

        return bracketStyler;
    }

    public Styler getArgNameStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        }

        return argNameStyler;
    }

    public Styler getNumberStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        }

        return numberStyler;
    }

    public Styler getErrorStyler(StackedCommand cmd) {
        if (cmd.getStackedState() == StackedState.ISSUED) {
            return issuedStyler;
        }

        return errorStyler;
    }

    public void setWaitTime(int waitTime) {
        CommandStack.getInstance().setWaitTime(waitTime);
        if (waitTime >= 0) {
            waitTimeSpinner.setSelection(waitTime);
        } else {
            waitTimeSpinner.setSelection(0);
        }
    }

    public void addTelecommand(StackedCommand command) {
        commandTableViewer.addTelecommand(command);

        var sel = new StructuredSelection(command);
        commandTableViewer.setSelection(sel, true);
    }

    public void refreshState() {
        commandTableViewer.refresh();
        var stack = CommandStack.getInstance();

        var sel = (IStructuredSelection) commandTableViewer.getSelection();
        updateMessagePanel(sel);

        var mayCommand = YamcsPlugin.hasAnyObjectPrivilege("Command");
        var executing = stack.isExecuting();

        armButton.setEnabled(false);
        runButton.setEnabled(false);
        waitTimeSpinner.setEnabled(false);
        if (connectionStateProvider.isConnected() && !executing) {
            if (!sel.isEmpty()) {
                var enableArm = false;
                var enableIssue = true;
                // Enable arm, as soon as there's at least one command in the selection currently unarmed
                // Enable issue, only when all selected commands are armed
                if (stack.isValid() && !sel.isEmpty() && mayCommand) {
                    for (var o : sel.toArray()) {
                        var command = (StackedCommand) o;
                        if (!command.isArmed()) {
                            enableArm = true;
                            enableIssue = false;
                        }
                    }
                } else {
                    enableIssue = false;
                }
                armButton.setEnabled(enableArm);
                runButton.setEnabled(enableIssue);
            }
            waitTimeSpinner.setEnabled(true);
        }

        commandTableViewer.getTable().setEnabled(!executing);

        // State for plugin.xml handlers
        var executionStateProvider = RCPUtils.findSourceProvider(getSite(), CommandStackStateProvider.STATE_KEY_EMPTY,
                CommandStackStateProvider.class);
        executionStateProvider.refreshState(CommandStack.getInstance());
    }

    @Override
    public void setFocus() {
        commandTableViewer.getTable().setFocus();
    }

    // On the GUI thread
    private void processCommand(Command command) {
        for (var cmd : CommandStack.getInstance().getCommands()) {
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
        BEFORE_ITEM, AFTER_ITEM, APPEND
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
                var selection = commandTableViewer.getTable().getSelection();
                if (selection == null || selection.length == 0) {
                    return;
                }

                // copy each selected items
                var scs = new ArrayList<StackedCommand>();
                for (var ti : selection) {
                    var sc = (StackedCommand) (ti.getData());
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
                var selection = commandTableViewer.getTable().getSelection();
                if (selection != null && selection.length > 0) {
                    sc = (StackedCommand) (commandTableViewer.getTable().getSelection()[0].getData());
                }
                if (sc == null && pastingType != PastingType.APPEND) {
                    pastingType = PastingType.APPEND;
                }

                // get commands from clipboard
                List<StackedCommand> copiedCommands = new ArrayList<>();
                try {
                    copiedCommands = CommandClipboard.getCopiedCommands();
                } catch (Exception e) {
                    var errorMessage = "Unable to build Stacked Command from the specifed source: ";
                    log.log(Level.WARNING, errorMessage, e);
                    commandTableViewer.getTable().getDisplay().asyncExec(() -> {
                        var dialog = new MessageBox(commandTableViewer.getTable().getShell(), SWT.ICON_ERROR | SWT.OK);
                        dialog.setText("Command Stack Edition");
                        dialog.setMessage(errorMessage + e.getMessage());
                        // open dialog and await user selection
                        dialog.open();
                    });
                    return;
                }
                if (copiedCommands.isEmpty()) {
                    return;
                }

                // paste
                var index = commandTableViewer.getIndex(sc);
                for (var pastedCommand : copiedCommands) {
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
                var part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .findView(CommandStackView.ID);
                var commandStackView = (CommandStackView) part;
                commandStackView.refreshState();
            }
        }

        var table = commandTableViewer.getTable();
        var contextMenu = new Menu(commandTableViewer.getTable());
        table.setMenu(contextMenu);

        var mItemCopy = new MenuItem(contextMenu, SWT.None);
        mItemCopy.setText("Copy");
        mItemCopy.addSelectionListener(new CopySelectionListener(false));

        var mItemCut = new MenuItem(contextMenu, SWT.None);
        mItemCut.setText("Cut");
        mItemCut.addSelectionListener(new CopySelectionListener(true));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        var mItemPasteBefore = new MenuItem(contextMenu, SWT.None);
        mItemPasteBefore.setText("Paste Before");
        mItemPasteBefore.addSelectionListener(new PasteSelectionListener(PastingType.BEFORE_ITEM));

        var mItemPasteAfter = new MenuItem(contextMenu, SWT.None);
        mItemPasteAfter.setText("Paste After");
        mItemPasteAfter.addSelectionListener(new PasteSelectionListener(PastingType.AFTER_ITEM));

        var mItemPaste = new MenuItem(contextMenu, SWT.None);
        mItemPaste.setText("Paste Append");
        mItemPaste.addSelectionListener(new PasteSelectionListener(PastingType.APPEND));

        commandTableViewer.getTable().addListener(SWT.MouseDown, event -> {
            var selection = commandTableViewer.getTable().getSelection();

            var pastBeforeAuthorized = true;
            var pastAfterAuthorized = true;

            StackedCommand sc1 = null;
            StackedCommand sc2 = null;
            if (selection.length > 0) {
                // prevent to edit part of the command stack that has already been executed
                sc1 = (StackedCommand) selection[0].getData();
                sc2 = (StackedCommand) selection[selection.length - 1].getData();
                var lastSelectionIndex = commandTableViewer.getIndex(sc2);
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
