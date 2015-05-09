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
import org.eclipse.swt.widgets.Label;
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
    private static final int REC_WIDTH = 120;
    private static final int X_INDENT = ANGLE_DELTA / 2;

    private Composite top;
    private Canvas processor;
    private String processorName;

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
        // We use whatever height we can obtain, since eclipse will add empty gutters around it anyway.
        // I could not find anything to control the height from within here
        // About the only thing that appears to enlarge the toolbar height, is a large-enough image, but then
        // it only works for that item anyway.
        processor.addPaintListener(evt -> {
            GC gc = evt.gc;
            if (processorName != null) {
                gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
            } else {
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            }

            Rectangle rec = processor.getBounds();
            int[] points = new int[] {
                    X_INDENT, 0,
                    X_INDENT + ANGLE_DELTA, rec.height - 1, // the -2 is magic. Without it, it clips for no reason...
                    rec.width - 1, rec.height - 1, // and the -1 is magic to get the drawPolygon nicely contouring the shape
                    rec.width - 1 - ANGLE_DELTA, 0
            };

            if (processorName != null)
                gc.fillPolygon(points);

            gc.drawPolygon(points);

            String text = processorName;
            if (text == null) {
                text = "not connected";
                gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            }
            gc.setFont(JFaceResources.getTextFont());
            int textWidth = gc.getFontMetrics().getAverageCharWidth() * text.length();
            int text_x = X_INDENT + Math.max((REC_WIDTH - textWidth) / 2, 0);
            int text_y = (rec.height - gc.getFontMetrics().getHeight()) / 2;
            gc.drawText(text, text_x, text_y, true /* transparent */);
        });
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = X_INDENT + REC_WIDTH;
        processor.setLayoutData(gd);

        // Just some spacer to have horizontalSpacing kick in
        Label spacer = new Label(top, SWT.NONE);
        gd = new GridData();
        gd.widthHint = 40;
        spacer.setLayoutData(gd);

        YamcsPlugin.getDefault().addProcessorListener(this);
        return top;
    }

    @Override
    public void onProcessorSwitch(String processorName) {
        Display.getDefault().asyncExec(() -> {
            this.processorName = processorName;
            processor.redraw();
        });
    }
}
