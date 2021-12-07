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

import java.time.OffsetDateTime;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class MouseTracker extends Drawable implements MouseMoveListener {

    private OffsetDateTime time;

    public MouseTracker(Timeline timeline) {
        super(timeline);
        timeline.addCanvasMouseMoveListener(this);
    }

    @Override
    public void mouseMove(MouseEvent e) {
        time = timeline.mouse2time(e.x);
        reportMutation();
    }

    @Override
    void drawOverlay(GC gc) {
        if (time != null) {
            var x = (int) Math.round(timeline.positionTime(time));

            var foreground = new Color(204, 204, 204);
            gc.setForeground(foreground);
            gc.setLineDash(new int[] { 4, 3 });
            gc.drawLine(x, 0, x, timeline.getBounds().height);
            gc.setLineDash(null);
            foreground.dispose();
        }
    }
}
