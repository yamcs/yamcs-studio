package org.yamcs.studio.archive;

import java.time.OffsetDateTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class ReplayOverlay extends Drawable {

    private OffsetDateTime start;
    private OffsetDateTime stop;

    public ReplayOverlay(Timeline timeline) {
        super(timeline);
    }

    @Override
    void drawOverlay(GC gc) {
        if (start != null && stop != null) {
            var x1 = (int) Math.round(timeline.positionTime(start));
            var x2 = (int) Math.round(timeline.positionTime(stop));

            var height = timeline.getCanvas().getBounds().height;

            gc.setAlpha((int) (255 * 0.15));
            var background = new Color(148, 0, 211);
            gc.setBackground(background);
            gc.fillRectangle(x1, 0, x2 - x1, height);
            gc.setAlpha(255);
            background.dispose();

            var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
            gc.setForeground(foreground);
            gc.drawLine(x1, 0, x1, height);
            gc.drawLine(x2, 0, x2, height);
        }
    }

    public void setReplayRange(OffsetDateTime start, OffsetDateTime stop, boolean loop) {
        this.start = start;
        this.stop = stop;
        this.reportMutation();
    }
}
