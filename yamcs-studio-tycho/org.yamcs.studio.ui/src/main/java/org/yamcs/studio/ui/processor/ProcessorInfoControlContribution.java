package org.yamcs.studio.ui.processor;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.services.IEvaluationService;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.YamcsPlugin.ConnectionStatus;
import org.yamcs.studio.ui.connections.ConnectionsDialog;

/**
 * Shows a visual indicator for the currently subscribed processor.
 */
public class ProcessorInfoControlContribution extends WorkbenchWindowControlContribution implements ProcessorListener {

    private static final Logger log = Logger.getLogger(ProcessorInfoControlContribution.class.getName());

    private static final int ANGLE_DELTA = 10;
    private static final int REC_WIDTH = 120;
    private static final int REC_HEIGHT = 20;
    private static final int X_INDENT = ANGLE_DELTA / 2;

    private Composite top;
    private Canvas processor;
    private ProcessorInfo processorInfo;

    @Override
    protected Control createControl(Composite parent) {
        // GridLayout, so we can define widths
        top = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        top.setLayout(gl);

        processor = new Canvas(top, SWT.NONE);
        processor.setToolTipText("Subscribed Yamcs Processor");
        processor.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        processor.addPaintListener(evt -> {
            GC gc = evt.gc;
            gc.setAntialias(SWT.ON);
            if (processorInfo != null) {
                gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                if (processorInfo.hasReplayState()) {
                    switch (processorInfo.getReplayState()) {
                    case INITIALIZATION:
                    case PAUSED:
                        gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                        gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
                        break;
                    case RUNNING:
                        break;
                    case STOPPED:
                    case ERROR:
                    case CLOSED:
                        gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
                        gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                        break;
                    }
                }
            } else {
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            }

            Rectangle bounds = processor.getBounds();

            int y_indent = (bounds.height - REC_HEIGHT) / 2;

            int[] points = new int[] {
                    X_INDENT, y_indent,
                    X_INDENT + ANGLE_DELTA, bounds.height - y_indent - 1, // the -1 is magic. Without it, it clips for no reason...
                    bounds.width - 1, bounds.height - y_indent - 1, // and the -1 is magic to get the drawPolygon nicely contouring the shape
                    bounds.width - 1 - ANGLE_DELTA, y_indent
            };

            if (processorInfo != null)
                gc.fillPolygon(points);

            gc.drawPolygon(points);

            String text;
            if (processorInfo == null) {
                text = "not connected";
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            } else {
                text = processorInfo.getName();
            }
            gc.setFont(JFaceResources.getTextFont());
            int textWidth = gc.getFontMetrics().getAverageCharWidth() * text.length();
            int text_x = X_INDENT + Math.max((REC_WIDTH - textWidth) / 2, 0);
            int text_y = (bounds.height - gc.getFontMetrics().getHeight()) / 2;
            gc.drawText(text, text_x, text_y, true /* transparent */);
        });
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = X_INDENT + REC_WIDTH;
        processor.setLayoutData(gd);

        Label spacer = new Label(top, SWT.NONE);
        gd = new GridData();
        gd.widthHint = 40;
        spacer.setLayoutData(gd);

        processor.addListener(SWT.MouseDown, evt -> {
            if (YamcsPlugin.getDefault().getConnectionSatus() == ConnectionStatus.Connected) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
                IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
                try {
                    Command cmd = commandService.getCommand("org.yamcs.studio.ui.processor.switch");
                    cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
                } catch (Exception exception) {
                    log.log(Level.SEVERE, "Could not execute command", exception);
                }
            } else {
                ConnectionsDialog dialog = new ConnectionsDialog(processor.getShell());
                if (dialog.open() == Dialog.OK) {

                }
            }
        });

        YamcsPlugin.getDefault().addProcessorListener(this);
        return top;
    }

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getId() == YamcsPlugin.getDefault().getClientInfo().getId()) {
                processorInfo = YamcsPlugin.getDefault().getProcessorInfo(updatedInfo.getProcessorName());
                if (!processor.isDisposed())
                    processor.redraw();
            }
        });
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            processorInfo = null;
            if (!processor.isDisposed())
                processor.redraw();
        });
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (processorInfo != null && updatedInfo.getName().equals(processorInfo.getName())) {
                processorInfo = updatedInfo;
                if (!processor.isDisposed())
                    processor.redraw();
            }
        });
    }

    @Override
    public void yProcessorClosed(ProcessorInfo updatedInfo) {
    }

    @Override
    public void updateStatistics(Statistics stats) {
    }
}
