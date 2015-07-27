package org.yamcs.studio.ui.commanding.queue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.protobuf.Commanding.CommandId;

public class CommandFateDialog extends Dialog {
    int result;
    static CommandFateDialog cfd1 = null;
    static CommandFateDialog cfd2 = null;

    private Shell shell;

    public CommandFateDialog(Shell parent, int style) {
        super(parent, style);
    }

    public CommandFateDialog(Shell parent) {
        this(parent, 0); // your default style bits go here (not the Shell's style bits)
    }

    Button[] radios;
    Label messageLabel;

    public static int showDialog(Shell parent, CommandId cmdId) {
        if (cfd1 == null)
            cfd1 = new CommandFateDialog(parent);
        return cfd1.open(1, "The command '" + cmdId.getSequenceNumber() + "' is older than " + CommandQueueView.oldCommandWarningTime + " seconds");
    }

    public static int showDialog2(Shell parent) {
        if (cfd2 == null)
            cfd2 = new CommandFateDialog(parent);
        return cfd2.open(2, "Enabling the queue would cause some commands older than " + CommandQueueView.oldCommandWarningTime
                + " seconds to be sent.");
    }

    public int open(int type, String message) {

        Shell parent = getParent();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setLayout(new GridLayout(1, false));

        // title
        if (type == 1)
            shell.setText("Command older than " + CommandQueueView.oldCommandWarningTime + " seconds");
        else
            shell.setText("Command(s) older than " + CommandQueueView.oldCommandWarningTime + " seconds");
        // shell.pack();

        // message
        messageLabel = new Label(shell, SWT.NONE);
        messageLabel.setText(message);

        // radio buttons
        if (type == 1) {
            radios = new Button[3];
            radios[0] = new Button(shell, SWT.RADIO);
            radios[0].setSelection(true);
            radios[0].setText("Send the command with updated generation time");

            radios[1] = new Button(shell, SWT.RADIO);
            radios[1].setText("Send the command with the current (old) generation time");

            radios[2] = new Button(shell, SWT.RADIO);
            radios[2].setText("Reject the command");
        } else {
            radios = new Button[2];
            radios[0] = new Button(shell, SWT.RADIO);
            radios[0].setSelection(true);
            radios[0].setText("Send all the commands with updated generation time");

            radios[1] = new Button(shell, SWT.RADIO);
            radios[1].setText("Send all the commands with the current (old) generation time");
        }

        // add buttons OK Cancel
        Composite buttonCompo = new Composite(shell, SWT.NONE);
        buttonCompo.setLayout(new RowLayout());
        Button ok = new Button(buttonCompo, SWT.PUSH);
        ok.setText("Ok");
        Button cancel = new Button(buttonCompo, SWT.PUSH);
        cancel.setText("Cancel");
        shell.setDefaultButton(ok);

        // Add events to buttons
        ok.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                result = -1;
                for (int i = 0; i < radios.length; i++) {
                    if (radios[i].getSelection()) {
                        result = i;
                        break;
                    }
                }
                shell.dispose();
            }
        });

        cancel.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                widgetSelected(arg0);
            }

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                result = -1;
                shell.dispose();
            }

        });

        shell.open();
        shell.pack();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return result;
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("dialog test");
        shell.open();

        CommandId cmdId = CommandId.newBuilder().setSequenceNumber(0).setGenerationTime(0).setOrigin("test").build();

        int result = CommandFateDialog.showDialog2(shell);
        System.out.println("result = " + result);

        result = CommandFateDialog.showDialog(shell, cmdId);
        System.out.println("result = " + result);

        result = CommandFateDialog.showDialog2(shell);
        System.out.println("result = " + result);

        result = CommandFateDialog.showDialog(shell, cmdId);
        System.out.println("result = " + result);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}
