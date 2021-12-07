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

public class DecadeScale implements Scale {

    private static final DateTimeFormatter FORMAT_YEAR = DateTimeFormatter.ofPattern("yyyy");

    private Timeline timeline;

    public DecadeScale(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public int getPreferredUnitWidth() {
        return 49;
    }

    @Override
    public int measureUnitWidth() {
        var x1 = timeline.getStart();
        var x2 = x1.plusYears(10);
        return (int) Math.round(timeline.positionTime(x2) - timeline.positionTime(x1));
    }

    @Override
    public void drawContent(GC gc, Rectangle coords) {
        var year = timeline.getStart().getYear();
        var t = timeline.getStart().withYear(year - (year % 10)).withMonth(1).withDayOfMonth(1).truncatedTo(DAYS);

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

            var label = t.format(FORMAT_YEAR) + "s";
            gc.drawText(label, majorX + 2, coords.y + (halfHeight / 2));

            t = t.plusYears(10);
        }

        font.dispose();
    }
}
