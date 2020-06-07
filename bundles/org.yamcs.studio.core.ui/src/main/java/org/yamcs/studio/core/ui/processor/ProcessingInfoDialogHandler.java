package org.yamcs.studio.core.ui.processor;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import org.yamcs.client.YamcsClient;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.protobuf.Mdb.MissionDatabase;
import org.yamcs.protobuf.Mdb.SpaceSystemInfo;
import org.yamcs.protobuf.ProcessorInfo;
import org.yamcs.protobuf.YamcsInstance;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.YamcsAware;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

/**
 * Ensemble of information for the subscribed instance (and matching MDB), and processor.
 */
public class ProcessingInfoDialogHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShellChecked(event);
        String instance = YamcsPlugin.getInstance();
        String processor = YamcsPlugin.getProcessor();
        if (instance != null && processor != null) {
            YamcsClient client = YamcsPlugin.getYamcsClient();
            ProcessorClient processorClient = client.createProcessorClient(instance, processor);

            CompletableFuture<YamcsInstance> instanceFuture = client.getInstance(instance);
            CompletableFuture<ProcessorInfo> processorFuture = processorClient.getInfo();
            CompletableFuture.allOf(instanceFuture, processorFuture).whenComplete((done, exc) -> {
                if (exc == null) {
                    Display display = Display.getDefault();
                    if (!display.isDisposed()) {
                        display.asyncExec(() -> {
                            try {
                                YamcsInstance instanceInfo = instanceFuture.get();
                                ProcessorInfo processorInfo = processorFuture.get();
                                new ProcessingInfoDialog(shell, instanceInfo, processorInfo).open();
                            } catch (java.util.concurrent.ExecutionException e) {
                                throw new RuntimeException(e.getCause());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                }
            });
        }
        return null;
    }

    public static class ProcessingInfoDialog extends Dialog implements YamcsAware {

        private YamcsInstance instance;
        private ProcessorInfo processor;

        private StyledText missionTimeTxt;

        public ProcessingInfoDialog(Shell parentShell, YamcsInstance instance, ProcessorInfo processor) {
            super(parentShell);
            this.instance = instance;
            this.processor = processor;
            setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER);
            setBlockOnOpen(false);

            YamcsPlugin.addListener(this);
        }

        @Override
        public boolean close() {
            YamcsPlugin.removeListener(this);
            return super.close();
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
        }

        @Override
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.CLOSE_ID) {
                close();
            } else {
                super.buttonPressed(buttonId);
            }
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

            createHeader(composite, "Subscribed Processor", true);
            createKeyValueTextPair(composite, "Name", processor.getName());
            createKeyValueTextPair(composite, "Type", processor.getType());
            createKeyValueTextPair(composite, "Created by", processor.getCreator());

            Instant missionTime = YamcsPlugin.getMissionTime();
            missionTimeTxt = createKeyValueTextPair(composite, "Mission Time",
                    YamcsUIPlugin.getDefault().formatInstant(missionTime));

            if (instance.hasMissionDatabase()) {
                MissionDatabase mdb = instance.getMissionDatabase();
                createHeader(composite, "Top-level Space Systems", true);

                Composite tableWrapper = new Composite(composite, SWT.BORDER);
                TableColumnLayout tcl = new TableColumnLayout();
                tableWrapper.setLayout(tcl);
                gd = new GridData(GridData.FILL_HORIZONTAL);
                gd.heightHint = 100;
                gd.horizontalSpan = 2;
                tableWrapper.setLayoutData(gd);

                TableViewer tableViewer = new TableViewer(tableWrapper, SWT.FULL_SELECTION);
                tableViewer.setContentProvider(ArrayContentProvider.getInstance());
                tableViewer.getTable().setHeaderVisible(true);
                tableViewer.getTable().setLinesVisible(true);

                TableViewerColumn ssColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                ssColumn.setLabelProvider(new ColumnLabelProvider() {
                    @Override
                    public String getText(Object element) {
                        SpaceSystemInfo ss = (SpaceSystemInfo) element;
                        return ss.getQualifiedName();
                    }
                });
                tcl.setColumnData(ssColumn.getColumn(), new ColumnPixelData(150));

                tableViewer.setInput(mdb.getSpaceSystemList());
            }

            return composite;
        }

        @Override
        public void updateTime(Instant missionTime) {
            Display.getDefault().asyncExec(() -> {
                if (missionTimeTxt.isDisposed()) {
                    return;
                }
                if (missionTimeTxt != null && !missionTimeTxt.isDisposed()) {
                    missionTimeTxt.setText(YamcsUIPlugin.getDefault().formatInstant(missionTime));
                } else {
                    missionTimeTxt.setText("---");
                }
            });
        }

        private StyledText createKeyValueTextPair(Composite parent, String key, String value) {
            Label lbl = new Label(parent, SWT.NONE);
            if (key != null) {
                lbl.setText(key + ":");
            }
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
            return txt;
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

        @SuppressWarnings("unused")
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
