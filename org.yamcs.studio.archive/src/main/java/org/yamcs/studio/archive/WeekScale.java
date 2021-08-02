package org.yamcs.studio.archive;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class WeekScale implements Scale {

    private static final DateTimeFormatter FORMAT_MONTH = DateTimeFormatter.ofPattern("MMMM");
    private static final DateTimeFormatter FORMAT_DAY_OF_MONTH = DateTimeFormatter.ofPattern("dd/MM");

    private Timeline timeline;

    public WeekScale(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public int getPreferredUnitWidth() {
        return 50;
    }

    @Override
    public int measureUnitWidth() {
        var x1 = timeline.getStart();
        var x2 = x1.plusWeeks(1);
        return (int) Math.round(timeline.positionTime(x2) - timeline.positionTime(x1));
    }

    @Override
    public void drawContent(GC gc, Rectangle coords) {
        var t = timeline.getStart()
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
            gc.drawLine(majorX, coords.y, majorX, coords.y + coords.height / 2);

            var label = t.format(FORMAT_MONTH);
            gc.drawText(label, majorX + 2, coords.y);

            t = t.plusMonths(1);
        }

        t = timeline.getStart()
                .withDayOfMonth(1)
                .with(ChronoField.DAY_OF_WEEK, 1)
                .truncatedTo(DAYS);
        while (t.isBefore(timeline.getStop())) {
            var x = timeline.positionTime(t);

            var majorX = (int) Math.round(x);
            gc.drawLine(majorX, coords.y + (coords.height / 2), majorX, coords.y + coords.height);

            var subLabelX = (int) Math.round(timeline.positionTime(t.plusDays(3).plusHours(12)));
            var label = t.format(FORMAT_DAY_OF_MONTH);
            var labelWidth = gc.textExtent(label).x;
            gc.drawText(label, subLabelX - (labelWidth / 2), coords.y + coords.height / 2);

            t = t.plusWeeks(1);
        }

        font.dispose();
    }
}
