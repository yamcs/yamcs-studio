package org.yamcs.studio.ui.commanding.stack;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestExceptionMessage;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.CommandParser;
import org.yamcs.xtce.Argument;
import org.yamcs.xtce.MetaCommand;

import com.google.protobuf.MessageLite;

public class AddToStackDialog extends TitleAreaDialog implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(AddToStackDialog.class.getName());

    private Collection<MetaCommand> commands;
    private StyledText text;
    private RestClient restClient = null;

    public AddToStackDialog(Shell parentShell) {
        super(parentShell);
        commands = YamcsPlugin.getDefault().getCommands();
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

    @Override
    public void create() {
        super.create();
        setTitle("Add New Command to Stack");
        // setMessage("informative message");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        Label lblCommand = new Label(container, SWT.NONE);
        lblCommand.setText("Template");

        Combo commandCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        commandCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (MetaCommand command : commands) {
            if (!command.isAbstract()) {
                commandCombo.add(command.getOpsName());
            }
        }

        text = new StyledText(container, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        text.setLayoutData(gd);
        text.addListener(SWT.Modify, evt -> setErrorMessage(null));

        commandCombo.addListener(SWT.Selection, event -> {
            for (MetaCommand command : commands) {
                String selected = ((Combo) event.widget).getText();
                if (!command.isAbstract() && command.getOpsName().equals(selected)) {

                    StringBuilder buf = new StringBuilder(command.getOpsName());
                    if (command.getArgumentList() != null) {
                        buf.append("(\n");
                        for (Argument arg : command.getArgumentList()) {
                            buf.append("\t" + arg.getName() + ": \n");
                        }
                        buf.append(")");
                    } else {
                        buf.append("()");
                    }
                    text.setText(buf.toString());
                    break;
                }
            }
        });

        return area;
    }

    private boolean checkConnected()
    {
        if (restClient == null)
        {
            Display.getDefault().asyncExec(() -> {
                setErrorMessage("Client disconnected from Yamcs server");
                log.log(Level.SEVERE, "Could not validate command string, client disconnected from Yamcs server");
            });
            return false;
        }
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        // Change parent layout data to fill the whole bar
        parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button validateButton = createButton(parent, IDialogConstants.NO_ID, "Validate", true);
        validateButton.addListener(SWT.Selection, evt -> {
            if (!checkConnected())
                return;
            RestValidateCommandRequest.Builder req = RestValidateCommandRequest.newBuilder();
            req.addCommands(CommandParser.toCommand(text.getText()));
            restClient.validateCommand(req.build(), new ResponseHandler() {
                @Override
                public void onMessage(MessageLite response) {
                    if (response instanceof RestExceptionMessage) {
                        Display.getDefault().asyncExec(() -> {
                            RestExceptionMessage exc = (RestExceptionMessage) response;
                            setErrorMessage("[" + exc.getType() + "] " + exc.getMsg());
                        });
                    } else {
                        Display.getDefault().asyncExec(() -> setMessage("Command is valid", MessageDialog.INFORMATION));
                        System.out.println("GOT response " + response);
                    }
                }

                @Override
                public void onException(Exception e) {
                    Display.getDefault().asyncExec(() -> {
                        setErrorMessage(e.getMessage());
                        log.log(Level.SEVERE, "Could not validate command string", e);
                    });
                }
            });
        });

        // Create a spacer label
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Update layout of the parent composite to count the spacer
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        Button okButton = createButton(parent, IDialogConstants.OK_ID, "Send", true);
        okButton.addListener(SWT.Selection, evt -> {
            if (!checkConnected())
                return;
            RestSendCommandRequest.Builder req = RestSendCommandRequest.newBuilder();
            req.addCommands(CommandParser.toCommand(text.getText()));
            restClient.sendCommand(req.build(), new ResponseHandler() {
                @Override
                public void onMessage(MessageLite response) {
                    if (response instanceof RestExceptionMessage) {
                        Display.getDefault().asyncExec(() -> {
                            RestExceptionMessage exc = (RestExceptionMessage) response;
                            setErrorMessage("[" + exc.getType() + "] " + exc.getMsg());
                        });
                    }
                    Display.getDefault().asyncExec(() -> close());
                    System.out.println("GOT response " + response);
                }

                @Override
                public void onException(Exception e) {
                    Display.getDefault().asyncExec(() -> {
                        setErrorMessage(e.getMessage());
                        log.log(Level.SEVERE, "Could not send command", e);
                    });
                }
            });
        });
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    public void okPressed() {
        // NOP
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}
