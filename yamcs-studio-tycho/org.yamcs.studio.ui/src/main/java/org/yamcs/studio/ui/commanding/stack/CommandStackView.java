package org.yamcs.studio.ui.commanding.stack;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Commanding.CommandSignificance;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandResponse;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;

import com.google.protobuf.MessageLite;

public class CommandStackView extends ViewPart implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(CommandStackView.class.getName());
    public static final String ID = "org.yamcs.studio.ui.commanding.stack.CommandStackView";

    private CommandStackTableViewer commandTableViewer;
    private Label nextCommandState;
    private StyledText nextCommandLabel;
    private Button armToggle;
    private Button goButton;

    private FormToolkit tk;
    private ScrolledForm form;

    private Styler bracketStyler;
    private Styler argNameStyler;
    private Styler numberStyler;
    private Styler errorStyler;

    private RestClient restClient = null;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        tk = new FormToolkit(parent.getDisplay());

        ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        Color errorBackgroundColor = resourceManager.createColor(new RGB(255, 221, 221));
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
                textStyle.background = errorBackgroundColor;
                textStyle.foreground = Display.getDefault().getSystemColor(SWT.COLOR_RED);
            }
        };

        SashForm sash = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
        sash.setLayout(new FillLayout());

        Composite tableWithControls = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.numColumns = 2;
        tableWithControls.setLayout(gl);
        tableWithControls.setBackground(tk.getColors().getBackground());

        Composite tableWrapper = new Composite(tableWithControls, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        commandTableViewer = new CommandStackTableViewer(tableWrapper, tcl, this);
        Composite tableControls = new Composite(tableWithControls, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalAlignment = SWT.BOTTOM;
        tableControls.setLayoutData(gd);
        gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        tableControls.setLayout(gl);

        // This wrapper is just to achieve bottom alignment compared to the table
        Composite tableControlsButtonWrapper = new Composite(tableControls, SWT.NONE);
        tableControlsButtonWrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        gl.verticalSpacing = 0;
        tableControlsButtonWrapper.setLayout(gl);

        // This verbose way of creating buttons, ensures that state and handling
        // is hooked with the command framework
        CommandContributionItemParameter parms = new CommandContributionItemParameter(
                getSite(),
                "org.yamcs.studio.ui.commanding.stack.addCommand",
                "org.yamcs.studio.ui.commanding.stack.addCommand",
                CommandContributionItem.STYLE_PUSH);
        parms.label = "Add...";
        CommandContributionItem item = new CommandContributionItem(parms);
        Composite singleBtnWrapper = new Composite(tableControlsButtonWrapper, SWT.NONE);
        singleBtnWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        singleBtnWrapper.setLayout(new FillLayout());
        item.fill(tableControlsButtonWrapper);

        parms = new CommandContributionItemParameter(
                getSite(),
                "org.yamcs.studio.ui.commanding.stack.moveUpCommand",
                "org.yamcs.studio.ui.commanding.stack.moveUpCommand",
                CommandContributionItem.STYLE_PUSH);
        parms.label = "Up";
        item = new CommandContributionItem(parms);
        singleBtnWrapper = new Composite(tableControlsButtonWrapper, SWT.NONE);
        singleBtnWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        singleBtnWrapper.setLayout(new FillLayout());
        item.fill(singleBtnWrapper);

        parms = new CommandContributionItemParameter(
                getSite(),
                "org.yamcs.studio.ui.commanding.stack.moveDownCommand",
                "org.yamcs.studio.ui.commanding.stack.moveDownCommand",
                CommandContributionItem.STYLE_PUSH);
        parms.label = "Down";
        item = new CommandContributionItem(parms);
        singleBtnWrapper = new Composite(tableControlsButtonWrapper, SWT.NONE);
        singleBtnWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        singleBtnWrapper.setLayout(new FillLayout());
        item.fill(tableControlsButtonWrapper);

        Composite bottomPane = new Composite(sash, SWT.NONE);
        bottomPane.setLayout(new FillLayout());

        form = tk.createScrolledForm(bottomPane);
        TableWrapLayout layout = new TableWrapLayout();
        layout.leftMargin = 10;
        layout.rightMargin = 10;
        layout.topMargin = 10;
        layout.bottomMargin = 10;
        layout.verticalSpacing = 20;
        form.getBody().setLayout(layout);
        createNextCommandSection();
        commandTableViewer.addDoubleClickListener(evt -> {
            IStructuredSelection sel = (IStructuredSelection) evt.getSelection();
            if (sel.getFirstElement() != null) {
                EditStackedCommandDialog dialog = new EditStackedCommandDialog(parent.getShell(), (StackedCommand) sel.getFirstElement());
                if (dialog.open() == Window.OK) {
                    refreshState();
                }
            }
        });
        commandTableViewer.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    IStructuredSelection sel = (IStructuredSelection) commandTableViewer.getSelection();
                    if (!sel.isEmpty()) {
                        CommandStack.getInstance().getCommands().removeAll(sel.toList());
                        refreshState();
                    }
                }
            }
        });

        sash.setWeights(new int[] { 60, 40 });

        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    @Override
    public void processConnectionInfo(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        this.restClient = restclient;
    }

    @Override
    public void disconnect() {
        if (restClient == null)
            return;
        restClient.shutdown();
        restClient = null;
    }

    public Styler getBracketStyler() {
        return bracketStyler;
    }

    public Styler getArgNameStyler() {
        return argNameStyler;
    }

    public Styler getNumberStyler() {
        return numberStyler;
    }

    public Styler getErrorStyler() {
        return errorStyler;
    }

    private Section createNextCommandSection() {
        Section section = tk.createSection(form.getForm().getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Next Command");
        Composite sectionClient = tk.createComposite(section);
        sectionClient.setLayout(new GridLayout(2, false));

        nextCommandLabel = new StyledText(sectionClient, SWT.NONE);
        tk.adapt(nextCommandLabel, false, false);
        nextCommandLabel.setText("Empty Stack");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        nextCommandLabel.setLayoutData(gd);

        Composite controls = tk.createComposite(sectionClient, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = SWT.CENTER;
        gd.horizontalSpan = 2;
        controls.setLayoutData(gd);
        controls.setLayout(new RowLayout());
        armToggle = tk.createButton(controls, "Arm", SWT.TOGGLE);
        armToggle.setEnabled(false);
        goButton = tk.createButton(controls, "Fire", SWT.PUSH);
        goButton.setEnabled(false);

        nextCommandState = new Label(section, SWT.NONE);
        nextCommandState.setText("                       "); // TODO i know i know
        section.setTextClient(nextCommandState);

        armToggle.addListener(SWT.Selection, evt -> {
            StackedCommand command = CommandStack.getInstance().getNextCommand();
            if (armToggle.getSelection()) {
                nextCommandState.setText("checking...");
                armCommand(command);
            } else {
                command.setState(State.UNARMED);
                nextCommandState.setText(command.getState().getText());
                goButton.setEnabled(false);
            }
        });
        goButton.addListener(SWT.Selection, evt -> {
            StackedCommand command = CommandStack.getInstance().getNextCommand();
            fireCommand(command);
        });

        section.setClient(sectionClient);
        return section;
    }

    private boolean checkConnected(String action)
    {

        if (restClient == null)
        {
            Display.getDefault().asyncExec(() -> {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        "Could not " + action, "Client disconnected from Yamcs server");

            });
            return false;
        }
        return true;
    }

    // TODO move this to a handler
    private void armCommand(StackedCommand command) {
        if (!checkConnected("arm command"))
        {
            return;
        }

        armToggle.setEnabled(false);
        RestValidateCommandRequest req = RestValidateCommandRequest.newBuilder().addCommands(command.toRestCommandType()).build();

        restClient.validateCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    if (response instanceof RestExceptionMessage) {
                        RestExceptionMessage exc = (RestExceptionMessage) response;
                        command.setState(State.REJECTED);
                        nextCommandState.setText(command.getState().getText());
                        armToggle.setSelection(false);
                        MessageDialog.openError(Display.getDefault().getActiveShell(),
                                "Could not arm command", exc.getMsg());
                    } else {
                        RestValidateCommandResponse validateResponse = (RestValidateCommandResponse) response;

                        boolean doArm = false;
                        if (validateResponse.getCommandsSignificanceCount() > 0) {
                            CommandSignificance significance = validateResponse.getCommandsSignificance(0);
                            switch (significance.getConsequenceLevel()) {
                            case watch:
                            case warning:
                            case distress:
                            case critical:
                            case severe:
                                String level = Character.toUpperCase(significance.getConsequenceLevel().toString().charAt(0))
                                        + significance.getConsequenceLevel().toString().substring(1);
                                if (MessageDialog.openConfirm(armToggle.getDisplay().getActiveShell(), "Confirm",
                                        level + ": " +
                                                "Are you sure you want to arm this command?\n" +
                                                "    " + command.toStyledString(CommandStackView.this).getString() + "\n\n" +
                                                significance.getReasonForWarning())) {
                                    doArm = true;
                                }
                                break;
                            default:
                                break;
                            }
                        } else {
                            doArm = true;
                        }

                        if (doArm) {
                            doArm(command);
                        } else {
                            refreshState();
                        }
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not arm command", e);
                Display.getDefault().asyncExec(() -> {
                    command.setState(State.REJECTED);
                    nextCommandState.setText(command.getState().getText());
                    armToggle.setSelection(false);
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                            "Could not arm command", e.getMessage());
                    armToggle.setEnabled(true);
                });
            }
        });
    }

    /**
     * Arming is a client thing only
     */
    private void doArm(StackedCommand command) {
        log.fine(String.format("Command armed %s", command));
        command.setState(State.ARMED);
        goButton.setEnabled(true);
        nextCommandState.setText(command.getState().getText());
        armToggle.setEnabled(true);
    }

    // TODO move this to a handler
    private void fireCommand(StackedCommand command) {

        if (!checkConnected("fire command"))
        {
            return;
        }

        armToggle.setEnabled(false);
        armToggle.setSelection(false);
        goButton.setEnabled(false);
        RestSendCommandRequest req = RestSendCommandRequest.newBuilder().addCommands(command.toRestCommandType()).build();

        restClient.sendCommand(req, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite response) {
                Display.getDefault().asyncExec(() -> {
                    if (response instanceof RestExceptionMessage) {
                        RestExceptionMessage exc = (RestExceptionMessage) response;
                        command.setState(State.REJECTED);
                        nextCommandState.setText(command.getState().getText());
                        MessageDialog.openError(Display.getDefault().getActiveShell(),
                                "Could not fire command", exc.getMsg());
                    } else {
                        log.fine(String.format("Command fired", req));
                        command.setState(State.ISSUED);
                        jumpToNextCommand();
                    }
                });
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not fire command", e);
                Display.getDefault().asyncExec(() -> {
                    command.setState(State.REJECTED);
                    nextCommandState.setText(command.getState().getText());
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                            "Could not fire command", e.getMessage());
                });
            }
        });
    }

    private void jumpToNextCommand() {
        CommandStack.getInstance().incrementAndGet();
        refreshState();
    }

    private Section createConstraintsSection(Form form) {
        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);
        section.setText("Constraints");
        Composite sectionClient = tk.createComposite(section);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        sectionClient.setLayout(gl);

        tk.createLabel(sectionClient, "Transmission Constraints");
        Label lbl = tk.createLabel(sectionClient, "ab\ncd", SWT.WRAP);
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        tk.createLabel(sectionClient, "Timeout");
        lbl = tk.createLabel(sectionClient, "10000");
        lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lbl.setEnabled(false);

        section.setClient(sectionClient);
        return section;
    }

    public void addTelecommand(StackedCommand command) {
        commandTableViewer.addTelecommand(command);
        refreshState();
    }

    private void refreshState() {
        commandTableViewer.refresh();

        CommandStack stack = CommandStack.getInstance();
        List<String> errorMessages = stack.getErrorMessages();

        armToggle.setSelection(false);
        goButton.setEnabled(false);
        if (stack.getNextCommand() != null) {
            stack.getNextCommand().setState(State.UNARMED);
            nextCommandState.setText(stack.getNextCommand().getState().getText());
        } else {
            nextCommandState.setText("");
        }

        nextCommandLabel.setStyleRanges(new StyleRange[] {});
        if (!errorMessages.isEmpty()) {
            nextCommandLabel.setText("Fix SPTV checks first ");
        } else if (!stack.hasRemaining()) {
            nextCommandLabel.setText("Empty Stack");
        } else {
            StackedCommand cmd = stack.getNextCommand();
            StyledString str = cmd.toStyledString(this);
            nextCommandLabel.setText(str.getString());
            nextCommandLabel.setStyleRanges(str.getStyleRanges());
        }

        if (errorMessages.isEmpty() && stack.hasRemaining()) {
            armToggle.setSelection(false);
            armToggle.setEnabled(true);
        } else {
            armToggle.setSelection(false);
            armToggle.setEnabled(false);
        }

        form.reflow(true);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        tk.dispose();
        super.dispose();
    }
}
