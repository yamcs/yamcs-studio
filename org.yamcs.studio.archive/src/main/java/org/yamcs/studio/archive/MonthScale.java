package org.yamcs.studio.archive;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class MonthScale implements Scale {

    private static final DateTimeFormatter FORMAT_YEAR = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter FORMAT_MONTH = DateTimeFormatter.ofPattern("LLL");

    private Timeline timeline;

    public MonthScale(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public int getPreferredUnitWidth() {
        return 32;
    }

    @Override
    public int measureUnitWidth() {
        var x1 = timeline.getStart();
        var x2 = x1.plusMonths(1);
        return (int) Math.round(timeline.positionTime(x2) - timeline.positionTime(x1));
    }

    @Override
    public void drawContent(GC gc, Rectangle coords) {
        var t = timeline.getStart()
                .withMonth(1)
                .withDayOfMonth(1)
                .truncatedTo(DAYS);

        var halfHeight = (int) Math.round(coords.height * 0.5);

        var background = timeline.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        gc.setBackground(background);
        var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(foreground);
        var fontData = JFaceResources.getTextFont().getFontData();
        var font = new Font(timeline.getDisplay(), fontData[0].getName(), halfHeight, fontData[0].getStyle());
        gc.setFont(font);

        while (t.isBefore(timeline.getStop())) {
            var x = timeline.positionTime(t);

            var majorX = (int) Math.round(x);
            gc.drawLine(majorX, coords.y, majorX, coords.y + coords.height);

            var label = t.format(FORMAT_YEAR);
            gc.drawText(label, majorX + 2, coords.y);

            for (var month = 1; month <= 12; month++) {
                var sub = t.withMonth(month);
                if (month != 1) {
                    var subX = (int) Math.round(timeline.positionTime(sub));
                    gc.drawLine(subX, halfHeight, subX, coords.height);
                }

                var m1 = sub.toInstant().toEpochMilli();
                var m2 = sub.plusMonths(1).toInstant().toEpochMilli();
                var mid = (m1 + m2) / 2;
                var tMid = Instant.ofEpochMilli(mid).atOffset(t.getOffset());

                var subLabelX = (int) Math.round(timeline.positionTime(tMid));
                label = sub.format(FORMAT_MONTH);
                var labelWidth = gc.textExtent(label).x;
                gc.drawText(label, subLabelX - (labelWidth / 2), halfHeight);
            }

            t = t.plusYears(1);
        }

        font.dispose();
    }
}
