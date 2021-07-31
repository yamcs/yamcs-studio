package org.yamcs.studio.archive;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

public class TimeRuler extends Line {

    public static final int LINE_HEIGHT = 20;

    public TimeRuler(Timeline timeline) {
        super(timeline);
    }

    @Override
    void drawContent(GC gc) {
        var background = timeline.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        gc.setBackground(background);
        var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(foreground);

        gc.fillRectangle(coords);
        determineScale().drawContent(gc, coords);
        gc.drawLine(coords.x, coords.y + coords.height, coords.x + coords.width, coords.y + coords.height);
    }

    @Override
    public int getHeight() {
        return LINE_HEIGHT;
    }

    private HourScale determineScale() {
        return new HourScale(timeline);
    }
}
