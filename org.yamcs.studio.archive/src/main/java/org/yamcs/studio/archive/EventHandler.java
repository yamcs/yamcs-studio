package org.yamcs.studio.archive;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.OffsetDateTime;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

public class EventHandler implements MouseListener, MouseMoveListener {

    private static enum Tool {
        HAND,
        RANGE_SELECT,
    }

    private Timeline timeline;

    private OffsetDateTime grabTime;
    private Tool tool;

    public EventHandler(Timeline timeline) {
        this.timeline = timeline;
        timeline.addCanvasMouseListener(this);
        timeline.addCanvasMouseMoveListener(this);
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if (e.button == 1) {
            if (grabTime == null && e.y < TimeRuler.LINE_HEIGHT) {
                timeline.clearSelection();
            }
            grabTime = timeline.mouse2time(e.x);
            tool = e.y < TimeRuler.LINE_HEIGHT ? Tool.RANGE_SELECT : Tool.HAND;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        grabTime = null;
        updateCursor(e);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        // Ignore
    }

    @Override
    public void mouseMove(MouseEvent e) {
        if (grabTime != null) {
            if (tool == Tool.HAND) {
                var time = timeline.mouse2time(e.x);
                var millisBetween = MILLIS.between(grabTime, time);
                timeline.panBy(-millisBetween, MILLIS);
            } else if (tool == Tool.RANGE_SELECT) {
                var time = timeline.mouse2time(e.x);
                if (Math.abs(e.x - timeline.positionTime(grabTime)) > 5) {
                    timeline.setSelection(grabTime, time);
                }
            }
        }

        updateCursor(e);
    }

    private void updateCursor(MouseEvent e) {
        int cursor = SWT.CURSOR_ARROW;
        if (grabTime == null && e.y < TimeRuler.LINE_HEIGHT) {
            cursor = SWT.CURSOR_SIZEWE;
        } else if (grabTime != null && tool == Tool.RANGE_SELECT) {
            cursor = SWT.CURSOR_SIZEWE;
        }
        timeline.setCursor(timeline.getDisplay().getSystemCursor(cursor));
    }
}
