/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.archive;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class WeekDayScale implements Scale {

    private static final DateTimeFormatter FORMAT_DAY_OF_MONTH = DateTimeFormatter.ofPattern("dd MMM yy");
    private static final DateTimeFormatter FORMAT_DAY_OF_WEEK = DateTimeFormatter.ofPattern("EEEEE");

    private Timeline timeline;

    public WeekDayScale(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public int getPreferredUnitWidth() {
        return 20;
    }

    @Override
    public int measureUnitWidth() {
        var x1 = timeline.getStart();
        var x2 = x1.plusDays(1);
        return (int) Math.round(timeline.positionTime(x2) - timeline.positionTime(x1));
    }

    @Override
    public void drawContent(GC gc, Rectangle coords) {
        var t = timeline.getStart().with(ChronoField.DAY_OF_WEEK, 1).truncatedTo(DAYS);

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

            var label = t.format(FORMAT_DAY_OF_MONTH);
            gc.drawText(label, majorX + 2, coords.y);

            for (var weekday = 1; weekday <= 7; weekday++) {
                var sub = t.with(ChronoField.DAY_OF_WEEK, weekday);
                if (weekday != 1) {
                    var subX = (int) Math.round(timeline.positionTime(sub));
                    gc.drawLine(subX, halfHeight, subX, coords.height);
                }

                var subLabelX = (int) Math.round(timeline.positionTime(sub.plusHours(12)));
                label = sub.format(FORMAT_DAY_OF_WEEK);
                var labelWidth = gc.textExtent(label).x;
                gc.drawText(label, subLabelX - (labelWidth / 2), halfHeight);
            }

            t = t.plusWeeks(1);
        }

        font.dispose();
    }
}
