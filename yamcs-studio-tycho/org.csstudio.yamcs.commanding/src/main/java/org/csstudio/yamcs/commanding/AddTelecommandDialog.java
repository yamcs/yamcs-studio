package org.csstudio.yamcs.commanding;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.YamcsPlugin;
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
import org.yamcs.web.rest.protobuf.ExceptionMessage;
import org.yamcs.web.rest.protobuf.RESTService;
import org.yamcs.web.rest.protobuf.RESTService.ResponseHandler;
import org.yamcs.web.rest.protobuf.ValidateCommandRequest;
import org.yamcs.web.rest.protobuf.ValidateCommandResponse;
import org.yamcs.xtce.Argument;
import org.yamcs.xtce.MetaCommand;

public class AddTelecommandDialog extends TitleAreaDialog {
    
    private static final Logger log = Logger.getLogger(AddTelecommandDialog.class.getName());
    
    private Collection<MetaCommand> commands;
    private StyledText text;
    private RESTService restService = YamcsPlugin.getDefault().getRESTService();

    public AddTelecommandDialog(Shell parentShell) {
        super(parentShell);
        commands = YamcsPlugin.getDefault().getCommands();
    }
    
    @Override
    public void create() {
        super.create();
        setTitle("Send a telecommand");
        //setMessage("informative message");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        GridLayout layout = new GridLayout(2, false);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(layout);

        Label lblCommand = new Label(container, SWT.NONE);
        lblCommand.setText("Template");

        Combo commandCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
        commandCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (MetaCommand command : commands) {
            if (!command.isAbstract()) {
                commandCombo.add(command.getName());
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
                if (!command.isAbstract() && command.getName().equals(selected)) {
                    
                    StringBuilder buf = new StringBuilder(command.getName());
                    if (command.getArgumentList() != null) {
                        buf.append("(\n");
                        for (Argument arg : command.getArgumentList()) {
                            buf.append("\t" + arg.getName() + "=\n");
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

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Change parent layout data to fill the whole bar
        parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button validateButton = createButton(parent, IDialogConstants.NO_ID, "Validate", true);
        validateButton.addListener(SWT.Selection, evt -> {
            ValidateCommandRequest req = new ValidateCommandRequest();
            req.setCommandString(text.getText());
            restService.validateCommand(req, new ResponseHandler<ValidateCommandResponse>() {
                @Override
                public void onMessage(ValidateCommandResponse response) {
                    Display.getDefault().asyncExec(() -> {
                        if (response.getException() != null) {
                            ExceptionMessage ex = response.getException();
                            setErrorMessage("[" + ex.getType() + "] " + ex.getMsg());    
                        } else {
                            setMessage("Command is valid", MessageDialog.INFORMATION);
                        }
                    });
                    System.out.println("GOT response " + response);
                }

                @Override
                public void onFault(Throwable t) {
                    Display.getDefault().asyncExec(() -> {
                        setErrorMessage(t.getMessage());
                        log.log(Level.SEVERE, "Could not validate command string", t);
                    });
                }
            });
        });

        // Create a spacer label
        Label spacer = new Label(parent, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Update layout of the parent composite to count the spacer
        GridLayout layout = (GridLayout)parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        createButton(parent, IDialogConstants.OK_ID, "Send", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}
