package org.yamcs.studio.ui.commanding.stack;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
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
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Rest.RestSendCommandRequest;
import org.yamcs.protobuf.Rest.RestValidateCommandRequest;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.commanding.CommandParser;
import org.yamcs.studio.ycl.dsl.ui.internal.YCLActivator;
import org.yamcs.xtce.Argument;
import org.yamcs.xtce.MetaCommand;

import com.google.inject.Injector;
import com.google.protobuf.MessageLite;

/**
 * WIP Reads any number of textual commands and adds them all at once to the stack. Useful for
 * copy/pasting from another source.
 * <p>
 * Inspiration for embedded editors:
 * https://github.com/eclipse/xtext/tree/master/plugins/org.eclipse
 * .xtext.ui.codetemplates.ui/src/org/eclipse/xtext/ui/codetemplates/ui/preferences
 */
@SuppressWarnings("restriction")
public class AddToStackFromScriptDialog extends TitleAreaDialog implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(AddToStackFromScriptDialog.class.getName());

    private Collection<MetaCommand> commands;
    private StyledText text;
    private RestClient restClient = null;

    public AddToStackFromScriptDialog(Shell parentShell) {
        super(parentShell);
        commands = YamcsPlugin.getDefault().getCommands();
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        this.restClient = restclient;
    }

    @Override
    public void onStudioDisconnect() {
        restClient = null;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Add one or more commands to Stack");
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
                commandCombo.add(command.getQualifiedName());
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
                if (!command.isAbstract() && command.getQualifiedName().equals(selected)) {

                    StringBuilder buf = new StringBuilder(command.getQualifiedName());
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

        Composite entryPanel = new Composite(area, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        entryPanel.setLayout(gl);
        installSyntaxHighlighting(entryPanel);

        return area;
    }

    private void installSyntaxHighlighting(Composite composite) {
        YCLActivator activator = YCLActivator.getInstance();
        Injector injector = activator.getInjector(YCLActivator.ORG_YAMCS_STUDIO_YCL_DSL_YCL);

        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE); // TODO needed?

        IEditedResourceProvider resourceProvider = new IEditedResourceProvider() {
            @Override
            public XtextResource createResource() {
                try {
                    Resource resource = resourceSet.createResource(URI.createURI("dummy:/ex.ycl"));
                    return (XtextResource) resource;
                } catch (Exception e) {
                    return null;
                }
            }
        };

        EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
        EmbeddedEditor handle = factory.newEditor(resourceProvider).withParent(composite);
        EmbeddedEditorModelAccess partialEditor = handle.createPartialEditor();
    }

    private boolean checkConnected() {
        if (restClient == null) {
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
                    Display.getDefault().asyncExec(() -> setMessage("Command is valid", MessageDialog.INFORMATION));
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
                    Display.getDefault().asyncExec(() -> close());
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
