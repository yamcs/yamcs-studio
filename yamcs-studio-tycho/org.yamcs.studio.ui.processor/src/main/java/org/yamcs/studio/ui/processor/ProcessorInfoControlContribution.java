package org.yamcs.studio.ui.processor;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.protobuf.YamcsManagement.ProcessorInfo;
import org.yamcs.protobuf.YamcsManagement.Statistics;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.core.model.ManagementListener;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.studio.core.ui.utils.RCPUtils;
import org.yamcs.utils.TimeEncoding;

/**
 * Shows a visual indicator for the currently subscribed processor.
 */
public class ProcessorInfoControlContribution extends WorkbenchWindowControlContribution
        implements StudioConnectionListener, ManagementListener, TimeListener {

    private static final int ANGLE_DELTA = 10;
    private static final int REC_WIDTH = 120;
    private static final int REC_HEIGHT = 20;
    private static final int X_INDENT = ANGLE_DELTA / 4;

    private static final int X_OVERLAP = ANGLE_DELTA - 2;
    private static final int TIMEREC_WIDTH = X_OVERLAP + 150;

    private Composite top;
    private Canvas canvas;
    private ProcessorInfo processorInfo;
    private long missionTime = TimeEncoding.INVALID_INSTANT;

    private Font timeFont;

    @Override
    protected Control createControl(Composite parent) {
        // GridLayout, so we can define widths
        top = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        top.setLayout(gl);

        canvas = new Canvas(top, SWT.NONE);
        canvas.setToolTipText("Subscribed Yamcs Processor");
        canvas.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

        canvas.addPaintListener(new MyPaintListener(parent));
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = X_INDENT + 2 * (REC_WIDTH + X_INDENT - X_OVERLAP) + TIMEREC_WIDTH;
        canvas.setLayoutData(gd);

        Label spacer = new Label(top, SWT.NONE);
        gd = new GridData();
        gd.widthHint = 40;
        spacer.setLayoutData(gd);

        canvas.addListener(SWT.MouseHover, evt -> {

            int x = evt.x;
            // hover instance
            if (x < (X_INDENT + REC_WIDTH - X_OVERLAP)) {
                canvas.setToolTipText("Yamcs Instance");
            }
            // hover processor
            else if (x < 2 * (X_INDENT + REC_WIDTH - X_OVERLAP)) {
                canvas.setToolTipText("Subscribed Yamcs Processor");
            } else {
                canvas.setToolTipText("Mission Time");
            }
        });

        canvas.addListener(SWT.MouseDown, evt -> {

            int x = evt.x;
            // click on instance or processor
            if (x < 2 * (X_INDENT + REC_WIDTH - X_OVERLAP)) {
                if (ConnectionManager.getInstance().isConnected()) {
                    RCPUtils.runCommand("org.yamcs.studio.ui.processor.choose");
                } else {
                    RCPUtils.runCommand("org.yamcs.studio.ui.connect");
                }
            }
        });

        FontData[] textFont = JFaceResources.getTextFont().getFontData();
        textFont[0].setHeight(textFont[0].getHeight() - 2);
        timeFont = new Font(parent.getDisplay(), textFont[0]);

        ManagementCatalogue.getInstance().addManagementListener(this);
        TimeCatalogue.getInstance().addTimeListener(this);
        ConnectionManager.getInstance().addStudioConnectionListener(this);
        return top;
    }

    @Override
    public void onStudioConnect() {
    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() -> {
            processorInfo = null;
            missionTime = TimeEncoding.INVALID_INSTANT;
            if (!canvas.isDisposed())
                canvas.redraw();
        });
    }

    @Override
    public void clientUpdated(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                ManagementCatalogue catalogue = ManagementCatalogue.getInstance();
                processorInfo = catalogue.getProcessorInfo(updatedInfo.getProcessorName());
                if (!canvas.isDisposed())
                    canvas.redraw();
            }
        });
    }

    @Override
    public void clientDisconnected(ClientInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (updatedInfo.getCurrentClient()) {
                processorInfo = null;
                if (!canvas.isDisposed())
                    canvas.redraw();
            }
        });
    }

    @Override
    public void processorUpdated(ProcessorInfo updatedInfo) {
        Display.getDefault().asyncExec(() -> {
            if (processorInfo != null && updatedInfo.getName().equals(processorInfo.getName())) {
                processorInfo = updatedInfo;
                if (!canvas.isDisposed())
                    canvas.redraw();
            }
        });
    }

    @Override
    public void processTime(long missionTime) {
        if (canvas.isDisposed())
            return;

        canvas.getDisplay().asyncExec(() -> {
            if (canvas.isDisposed())
                return;

            this.missionTime = missionTime;
            canvas.redraw();
        });
    }

    @Override
    public void processorClosed(ProcessorInfo updatedInfo) {
    }

    @Override
    public void statisticsUpdated(Statistics stats) {
    }

    @Override
    public void dispose() {
        if (timeFont != null)
            timeFont.dispose();
    }

    private class MyPaintListener implements PaintListener {

        private Composite parent;

        public MyPaintListener(Composite parent) {
            this.parent = parent;
        }

        private Color getSystemColor(int color) {
            return parent.getDisplay().getSystemColor(color);
        }

        @Override
        public void paintControl(PaintEvent evt) {
            GC gc = evt.gc;
            gc.setAntialias(SWT.ON);
            Color defaultBackground = gc.getBackground();

            paintInstanceInfo(gc);

            paintProcessorInfo(gc);

            gc.setBackground(defaultBackground);
            paintTimeInfo(gc);
        }

        private void paintInstanceInfo(GC gc) {
            if (processorInfo != null) {
                gc.setBackground(getSystemColor(SWT.COLOR_GREEN));
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GREEN));
            } else {
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
            }

            Rectangle bounds = canvas.getBounds();

            int y_indent = (bounds.height - REC_HEIGHT) / 2;
            int[] points = new int[] { X_INDENT, y_indent, X_INDENT + ANGLE_DELTA, bounds.height - y_indent - 1, // the
                                                                                                                 // -1
                                                                                                                 // is
                                                                                                                 // magic.
                                                                                                                 // Without
                                                                                                                 // it,
                                                                                                                 // it
                                                                                                                 // clips
                                                                                                                 // for
                                                                                                                 // no
                                                                                                                 // reason...
                    X_INDENT + REC_WIDTH - 1, bounds.height - y_indent - 1, // and
                                                                            // the
                                                                            // -1
                                                                            // is
                                                                            // magic
                                                                            // to
                                                                            // get
                                                                            // the
                                                                            // drawPolygon
                                                                            // nicely
                                                                            // contouring
                                                                            // the
                                                                            // shape
                    X_INDENT + REC_WIDTH - 1 - ANGLE_DELTA, y_indent };

            if (processorInfo != null)
                gc.fillPolygon(points);

            gc.drawPolygon(points);

            String text;
            if (processorInfo == null) {
                text = "not connected";
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
            } else {
                text = processorInfo.getInstance();
            }
            gc.setFont(JFaceResources.getTextFont());
            int textWidth = gc.getFontMetrics().getAverageCharWidth() * text.length();
            int text_x = X_INDENT + Math.max((REC_WIDTH - textWidth) / 2, 0);
            int text_y = (bounds.height - gc.getFontMetrics().getHeight()) / 2;
            gc.drawText(text, text_x, text_y, true /* transparent */);

        }

        private void paintProcessorInfo(GC gc) {
            if (processorInfo != null) {
                gc.setBackground(getSystemColor(SWT.COLOR_GREEN));
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GREEN));
                if (processorInfo.hasReplayState()) {
                    switch (processorInfo.getReplayState()) {
                    case INITIALIZATION:
                    case PAUSED:
                        gc.setBackground(getSystemColor(SWT.COLOR_YELLOW));
                        gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
                        break;
                    case RUNNING:
                        break;
                    case STOPPED:
                    case ERROR:
                    case CLOSED:
                        gc.setBackground(getSystemColor(SWT.COLOR_RED));
                        gc.setForeground(getSystemColor(SWT.COLOR_WHITE));
                        break;
                    }
                }
            } else {
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
            }

            Rectangle bounds = canvas.getBounds();

            int y_indent = (bounds.height - REC_HEIGHT) / 2;

            int offsetX = X_INDENT + REC_WIDTH - X_OVERLAP;
            int[] points = new int[] { offsetX + X_INDENT, y_indent, offsetX + X_INDENT + ANGLE_DELTA,
                    bounds.height - y_indent - 1, // the -1 is magic. Without
                                                  // it, it clips for no
                                                  // reason...
                    offsetX + X_INDENT + REC_WIDTH - 1, bounds.height - y_indent - 1, // and
                                                                                      // the
                                                                                      // -1
                                                                                      // is
                                                                                      // magic
                                                                                      // to
                                                                                      // get
                                                                                      // the
                                                                                      // drawPolygon
                                                                                      // nicely
                                                                                      // contouring
                                                                                      // the
                                                                                      // shape
                    offsetX + X_INDENT + REC_WIDTH - 1 - ANGLE_DELTA, y_indent };

            if (processorInfo != null)
                gc.fillPolygon(points);

            gc.drawPolygon(points);

            String text;
            if (processorInfo == null) {
                text = "---	";
                gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
            } else {
                text = processorInfo.getName();
            }
            gc.setFont(JFaceResources.getTextFont());
            int textWidth = gc.getFontMetrics().getAverageCharWidth() * text.length();
            int text_x = offsetX + X_INDENT + Math.max((REC_WIDTH - textWidth) / 2, 0);
            int text_y = (bounds.height - gc.getFontMetrics().getHeight()) / 2;
            gc.drawText(text, text_x, text_y, true /* transparent */);
        }

        private void paintTimeInfo(GC gc) {
            gc.setForeground(getSystemColor(SWT.COLOR_DARK_GRAY));
            Rectangle bounds = canvas.getBounds();

            int y_indent = (bounds.height - REC_HEIGHT) / 2;

            int offsetX = 2 * (X_INDENT + REC_WIDTH - X_OVERLAP);
            int[] points = new int[] { offsetX + X_INDENT, y_indent, offsetX + X_INDENT + ANGLE_DELTA,
                    bounds.height - y_indent - 1, // the -1 is magic. Without
                                                  // it, it clips for no
                                                  // reason...
                    offsetX + X_INDENT + TIMEREC_WIDTH - 1, bounds.height - y_indent - 1, // and
                                                                                          // the
                                                                                          // -1
                                                                                          // is
                                                                                          // magic
                                                                                          // to
                                                                                          // get
                                                                                          // the
                                                                                          // drawPolygon
                                                                                          // nicely
                                                                                          // contouring
                                                                                          // the
                                                                                          // shape
                    offsetX + X_INDENT + TIMEREC_WIDTH - 1 - ANGLE_DELTA, y_indent };

            if (missionTime != TimeEncoding.INVALID_INSTANT)
                gc.fillPolygon(points);

            gc.drawPolygon(points);

            String text = processorTimeToString();
            gc.setFont(timeFont);
            int textWidth = gc.getFontMetrics().getAverageCharWidth() * text.length();
            int text_x = offsetX + X_INDENT + Math.max((TIMEREC_WIDTH - textWidth) / 2, 0);
            int text_y = (bounds.height - gc.getFontMetrics().getHeight()) / 2;
            gc.drawText(text, text_x, text_y, true /* transparent */);
        }

        private String processorTimeToString() {
            if (missionTime == TimeEncoding.INVALID_INSTANT || missionTime == 0)
                return "---";
            else
                return TimeCatalogue.getInstance().toString(missionTime);
        }
    }
}
