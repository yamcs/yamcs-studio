package org.yamcs.studio.core.ui;

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
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.yamcs.studio.core.ProcessorListener;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * This is a bit fragile at the moment. Needs more work to get the actual available rectangle
 * because this is reported wrongly at the beginning, which is why we hardcode 25.
 * <p>
 * Perhaps we need to hook to a resize event on the parent.
 */
public class ProcessorInfo extends WorkbenchWindowControlContribution implements ProcessorListener {
    private static final int ANGLE_DELTA = 10;
    private static final int Y_INDENT = 5;
    private static final int REC_WIDTH = 100;
    private static final int REC_HEIGHT = 25;

    private Composite top;
    private Canvas processor;
    private String processorName = "none";

    @Override
    protected Control createControl(Composite parent) {
        // GridLayout, so we can define widths
        top = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        top.setLayout(gl);

        processor = new Canvas(top, SWT.NONE);
        processor.setToolTipText("Subscribed Yamcs Processor");
        processor.addPaintListener(evt -> {
            GC gc = evt.gc;
            if (processorName != null) {
                gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
                Rectangle rec = processor.getBounds();
                int[] points = new int[] {
                        0, 0,
                        0, REC_HEIGHT,
                        rec.width, REC_HEIGHT,
                        rec.width - ANGLE_DELTA, 0
                };
                gc.fillPolygon(points);
                gc.drawLine(rec.width - ANGLE_DELTA, 0, rec.width, rec.height);

                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                gc.setFont(JFaceResources.getTextFont());
                int textWidth = gc.getFontMetrics().getAverageCharWidth() * processorName.length();
                int x_indent = Math.max((REC_WIDTH - textWidth) / 2, 0);
                gc.drawText(processorName, x_indent, Y_INDENT, true /* transparent */);
            }
        });
        GridData gd = new GridData();
        gd.widthHint = REC_WIDTH;
        gd.heightHint = REC_HEIGHT;
        gd.verticalAlignment = SWT.CENTER;
        processor.setLayoutData(gd);

        YamcsPlugin.getDefault().addProcessorListener(this);
        return top;
    }

    @Override
    public void onProcessorSwitch(String processorName) {
        System.out.println("called with " + processorName);
        Display.getDefault().asyncExec(() -> {
            this.processorName = processorName;
            top.layout();
            processor.redraw();
        });
    }
}
