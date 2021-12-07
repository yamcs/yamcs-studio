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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

public class TimeRuler extends Line {

    public static final int LINE_HEIGHT = 20;

    private Scale[] orderedScales;

    public TimeRuler(Timeline timeline) {
        super(timeline);

        orderedScales = new Scale[] { new DecadeScale(timeline), // Macro
                new YearScale(timeline), new MonthScale(timeline), new WeekScale(timeline), new WeekDayScale(timeline),
                new QuarterDayScale(timeline), new HourScale(timeline), // Micro
        };
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

    private Scale determineScale() {
        var bestCandidate = orderedScales[0];
        for (var i = 1; i < orderedScales.length; i++) {
            var candidate = orderedScales[i];
            var unitWidth = candidate.measureUnitWidth();
            if (unitWidth >= candidate.getPreferredUnitWidth()) {
                bestCandidate = candidate;
            } else {
                break;
            }
        }
        return bestCandidate;
    }
}
