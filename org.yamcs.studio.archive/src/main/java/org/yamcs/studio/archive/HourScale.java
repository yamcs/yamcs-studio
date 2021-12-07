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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class HourScale implements Scale {

    private static final DateTimeFormatter FORMAT_HOUR = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter FORMAT_DAY_OF_MONTH = DateTimeFormatter.ofPattern("LLL dd");

    private Timeline timeline;

    public HourScale(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public int getPreferredUnitWidth() {
        return 38;
    }

    @Override
    public int measureUnitWidth() {
        var x1 = timeline.getStart();
        var x2 = x1.plusHours(1);
        return (int) Math.round(timeline.positionTime(x2) - timeline.positionTime(x1));
    }

    @Override
    public void drawContent(GC gc, Rectangle coords) {
        var t = timeline.getStart().truncatedTo(DAYS);

        var halfHourDistance = timeline.distanceBetween(t, t.plusMinutes(30));
        var quarterHourDistance = timeline.distanceBetween(t, t.plusMinutes(15));
        var minorHeight = (int) Math.round(coords.height * 0.8);
        var midHeight = (int) Math.round(coords.height * 0.6);

        var background = timeline.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        gc.setBackground(background);
        var foreground = timeline.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        gc.setForeground(foreground);
        var fontData = JFaceResources.getTextFont().getFontData();
        var font = new Font(timeline.getDisplay(), fontData[0].getName(), coords.height / 2, fontData[0].getStyle());
        gc.setFont(font);

        while (t.isBefore(timeline.getStop())) {
            var x = timeline.positionTime(t);

            // Major
            var majorX = (int) Math.round(x);
            gc.drawLine((int) Math.round(x), coords.y, majorX, coords.y + coords.height);
            var label = t.format(FORMAT_HOUR);
            if (label.equals("00")) {
                label = t.format(FORMAT_DAY_OF_MONTH);
            }
            if (label.length() > 2) {
                gc.drawText(label, majorX + 2, coords.y);
                gc.drawText("00", majorX + 2, coords.y + coords.height / 2);
            } else {
                gc.drawText(label, majorX + 2, coords.y + (int) Math.round(coords.height * 0.25));
            }

            // Mid
            var midX = (int) Math.round(x + halfHourDistance);
            gc.drawLine(midX, coords.y + midHeight, midX, coords.y + coords.height);

            // Minor
            var minorX = (int) Math.round(x + quarterHourDistance);
            gc.drawLine(minorX, coords.y + minorHeight, minorX, coords.y + coords.height);
            minorX = (int) Math.round(x + halfHourDistance + quarterHourDistance);
            gc.drawLine(minorX, coords.y + minorHeight, minorX, coords.y + coords.height);

            t = t.plusHours(1);
        }

        font.dispose();
    }
}
