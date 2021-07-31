package org.yamcs.studio.archive;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class HourScale {

    private static final DateTimeFormatter FORMAT_HOUR = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter FORMAT_DAY_OF_MONTH = DateTimeFormatter.ofPattern("LLL dd");

    private Timeline timeline;

    public HourScale(Timeline timeline) {
        this.timeline = timeline;
    }

    public void drawContent(GC gc, Rectangle coords) {

        // Trunc to hours before positioning
        var t = timeline.getStart().withHour(0).withMinute(0).withSecond(0).withNano(0);

        var halfHourDistance = timeline.distanceBetween(t, t.plusMinutes(30));
        var quarterHourDistance = timeline.distanceBetween(t, t.plusMinutes(15));

        var majorX = new ArrayList<Double>();
        var majorLabels = new ArrayList<String>();
        var midX = new ArrayList<Double>();
        var minorX = new ArrayList<Double>();

        while (t.isBefore(timeline.getStop())) {
            var x = timeline.positionTime(t);

            majorX.add(x);
            minorX.add(x + quarterHourDistance);
            midX.add(x + halfHourDistance);
            minorX.add(x + halfHourDistance + quarterHourDistance);

            var label = t.format(FORMAT_HOUR);
            if (label.equals("00")) {
                label = t.format(FORMAT_DAY_OF_MONTH);
            }
            majorLabels.add(label);

            t = t.plusHours(1);
        }

        var background = timeline.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        gc.setBackground(background);
        var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(foreground);
        for (var x : majorX) {
            gc.drawLine((int) Math.round(x), coords.y, (int) Math.round(x), coords.y + coords.height);
        }
        var midHeight = (int) Math.round(coords.height * 0.6);
        for (var x : midX) {
            gc.drawLine((int) Math.round(x), coords.y + midHeight, (int) Math.round(x), coords.y + coords.height);
        }
        var minorHeight = (int) Math.round(coords.height * 0.8);
        for (var x : minorX) {
            gc.drawLine((int) Math.round(x), coords.y + minorHeight, (int) Math.round(x), coords.y + coords.height);
        }

        var fontData = JFaceResources.getTextFont().getFontData();
        var font = new Font(timeline.getDisplay(), fontData[0].getName(), coords.height / 2, fontData[0].getStyle());
        gc.setFont(font);
        for (var i = 0; i < majorLabels.size(); i++) {
            var label = majorLabels.get(i);
            var x = (int) Math.round(majorX.get(i));
            if (label.length() > 2) {
                gc.drawText(label, x + 2, coords.y);
                gc.drawText("00", x + 2, coords.y + coords.height / 2);
            } else {
                gc.drawText(label, x + 2, coords.y + (int) Math.round(coords.height * 0.25));
            }
        }
        font.dispose();
    }
}
