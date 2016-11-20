package org.yamcs.studio.ui.processor;

import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.protobuf.YamcsManagement.MissionDatabase;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.SpaceSystemInfo;
import org.yamcs.protobuf.YamcsManagement.YamcsInstance;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.web.ResponseHandler;

import com.google.protobuf.MessageLite;

/**
 * Ensemble of information for the subscribed instance (and matching MDB), and
 * processor.
 */
public class ProcessingInfoDialogHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ProcessingInfoDialogHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
        ProcessorInfo processor = catalogue.getCurrentProcessorInfo();
        if (processor != null) {
            catalogue.fetchInstanceInformationRequest(processor.getInstance(), new ResponseHandler() {

                @Override
                public void onMessage(MessageLite responseMsg) {
                    Display display = Display.getDefault();
                    if (!display.isDisposed()) {
                        display.asyncExec(() -> {
                            YamcsInstance instance = (YamcsInstance) responseMsg;
                            new ProcessingInfoDialog(shell, instance, processor).open();
                        });
                    }
                }

                @Override
                public void onException(Exception e) {
                    MessageDialog.openError(shell, "Error while fetching info", e.getMessage());
                }
            });
        }
        return null;
    }

    public static class ProcessingInfoDialog extends Dialog {

        private YamcsInstance instance;
        private ProcessorInfo processor;

        public ProcessingInfoDialog(Shell parentShell, YamcsInstance instance, ProcessorInfo processor) {
            super(parentShell);
            this.instance = instance;
            this.processor = processor;
            setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER);
            setBlockOnOpen(false);
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
        }

        @Override
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.CLOSE_ID)
                close();
            else
                super.buttonPressed(buttonId);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            parent.getShell().setText("Processing Info");

            Composite composite = new Composite(parent, SWT.NONE);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            composite.setLayoutData(gd);

            GridLayout gl = new GridLayout(2, false);
            gl.marginWidth = 10;
            gl.marginHeight = 10;
            composite.setLayout(gl);

            createHeader(composite, "Yamcs Instance");
            createKeyValueTextPair(composite, "Name", processor.getInstance());
            if (instance.hasMissionDatabase()) {
                MissionDatabase mdb = instance.getMissionDatabase();
                if (mdb.hasName() && !"".equals(mdb.getName())) {
                    createKeyValueTextPair(composite, "MDB", mdb.getName());
                } else {
                    createKeyValueTextPair(composite, "MDB", "Unnamed, instance-specific");
                }
                // TODO would like to show this
                createKeyValueTextPair(composite, "MDB Last Scanned", "(information not available)");

                createHeader(composite, "Top-level Space Systems", true);
                for (int i = 0; i < mdb.getSpaceSystemCount(); i++) {
                    SpaceSystemInfo ssInfo = mdb.getSpaceSystem(i);
                    if (i > 0) {
                        createSeparator(composite);
                    }
                    createKeyValueTextPair(composite, "Name", "" + ssInfo.getQualifiedName());
                    if (ssInfo.hasShortDescription()) {
                        createKeyValueTextPair(composite, "Description", "" + ssInfo.getShortDescription());
                    }
                    createKeyValueTextPair(composite, "Parameters", "" + ssInfo.getParameterCount());
                    createKeyValueTextPair(composite, "Containers", "" + ssInfo.getContainerCount());
                    createKeyValueTextPair(composite, "Commands", "" + ssInfo.getCommandCount());
                    createKeyValueTextPair(composite, "Algorithms", "" + ssInfo.getAlgorithmCount());
                }
            } else {
                createKeyValueTextPair(composite, "MDB", "none");
            }

            createHeader(composite, "Subscribed Processor", true);
            createKeyValueTextPair(composite, "Name", processor.getName());
            createKeyValueTextPair(composite, "Type", processor.getType());
            createKeyValueTextPair(composite, "Created by", processor.getCreator());
            if (processor.hasHasCommanding()) {
                String supportsCommands = (processor.getHasCommanding() ? "yes" : "no");
                createKeyValueTextPair(composite, "Commanding", supportsCommands);
            }
            createKeyValueTextPair(composite, "Alarms", "todo");

            createHeader(composite, "Runtime Information", true);
            long missionTime = TimeCatalogue.getInstance().getMissionTime();
            createKeyValueTextPair(composite, "Mission Time", TimeCatalogue.getInstance().toString(missionTime));
            createKeyValueTextPair(composite, "Processor State", "" + processor.getState());

            return composite;
        }

        private void createKeyValueTextPair(Composite parent, String key, String value) {
            Label lbl = new Label(parent, SWT.NONE);
            if (key != null)
                lbl.setText(key + ":");
            GridData gd = new GridData();
            gd.horizontalAlignment = SWT.END;
            gd.verticalAlignment = SWT.BEGINNING;
            lbl.setLayoutData(gd);

            // StyledText instead of Label, so that text is selectable
            // TODO should wrap, but can't get it to work right now
            StyledText txt = new StyledText(parent, SWT.WRAP);
            txt.setBackground(parent.getBackground());
            txt.setEditable(false);
            txt.setCaret(null);
            txt.setText(value);
            txt.setWordWrap(true);
        }

        private void createSeparator(Composite parent) {
            Label divider = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            divider.setLayoutData(gd);
        }

        private void createHeader(Composite parent, String title) {
            createHeader(parent, title, false);
        }

        private void createHeader(Composite parent, String title, boolean leadSpace) {
            Label header = new Label(parent, SWT.NONE);
            header.setText(title);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            if (leadSpace) {
                gd.verticalIndent = 20;
            }
            header.setLayoutData(gd);
            header.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        }

        private static String capitalize(String string) {
            char[] chars = string.toLowerCase().toCharArray();
            boolean found = false;
            for (int i = 0; i < chars.length; i++) {
                if (!found && Character.isLetter(chars[i])) {
                    chars[i] = Character.toUpperCase(chars[i]);
                    found = true;
                } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                    found = false;
                }
            }
            return String.valueOf(chars);
        }
    }
}
