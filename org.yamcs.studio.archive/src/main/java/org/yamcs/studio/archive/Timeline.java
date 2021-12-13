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

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.yamcs.studio.archive.Histogram.HistogramKind;

public class Timeline extends Composite {

    private Canvas canvas;
    private List<Drawable> drawables = new ArrayList<>();

    private OffsetDateTime start;
    private OffsetDateTime stop;

    private OffsetDateTime selectionStart;
    private OffsetDateTime selectionStop;

    private Set<ViewportChangeListener> viewportChangeListeners = new HashSet<>(1);

    public Timeline(Composite parent, int style) {
        super(parent, style);
        setLayout(new FillLayout());
        canvas = new Canvas(this, SWT.V_SCROLL);

        var vbar = canvas.getVerticalBar();
        vbar.addListener(SWT.Selection, evt -> {
            canvas.redraw();
        });

        canvas.addPaintListener(e -> {
            var bounds = ((Canvas) e.widget).getBounds();

            var background = getDisplay().getSystemColor(SWT.COLOR_WHITE);
            e.gc.setBackground(background);
            e.gc.fillRectangle(bounds);

            for (var drawable : drawables) {
                drawable.beforeDraw(e.gc);
            }

            var y = -vbar.getSelection();
            for (var drawable : drawables) {
                if (drawable instanceof Line) {
                    var line = (Line) drawable;
                    if (!line.isFrozen()) {
                        line.coords = new Rectangle(0, y, bounds.width, line.getHeight());
                        line.drawContent(e.gc);
                    }
                    y += line.getHeight() + 1;
                } else {
                    drawable.drawContent(e.gc);
                }
            }
            var frozenY = 0;
            for (var drawable : drawables) {
                if (drawable instanceof Line) {
                    var line = (Line) drawable;
                    if (line.isFrozen()) {
                        line.coords = new Rectangle(0, frozenY, bounds.width, line.getHeight());
                        line.drawContent(e.gc);
                        frozenY += line.getHeight() + 1;
                    }
                }
            }

            for (var drawable : drawables) {
                drawable.drawOverlay(e.gc);
            }

            drawSelection(e.gc);

            var contentHeight = vbar.getSelection() + y;
            vbar.setMaximum(contentHeight);
            vbar.setThumb(Math.min(contentHeight, bounds.height));
        });

        new EventHandler(this);
    }

    public void setBounds(OffsetDateTime start, OffsetDateTime stop) {
        this.start = start;
        this.stop = stop;

        var evt = new ViewportChangeEvent(start, stop);
        viewportChangeListeners.forEach(l -> l.onEvent(evt));

        requestRepaint();
    }

    public void setSelection(OffsetDateTime start, OffsetDateTime stop) {
        if (stop.isAfter(start)) {
            selectionStart = start;
            selectionStop = stop;
        } else {
            selectionStart = stop;
            selectionStop = start;
        }
        requestRepaint();
    }

    public void clearSelection() {
        selectionStart = null;
        selectionStop = null;
        requestRepaint();
    }

    public void panBy(long amountToPanBy, TemporalUnit unit) {
        var start = this.start.plus(amountToPanBy, MILLIS);
        var stop = this.stop.plus(amountToPanBy, MILLIS);
        setBounds(start, stop);
    }

    public OffsetDateTime getCenter() {
        var totalMillis = MILLIS.between(start, stop);
        return start.plus(totalMillis / 2, MILLIS);
    }

    void add(Drawable drawable) {
        if (!drawables.contains(drawable)) {
            drawables.add(drawable);
            requestRepaint();
        }
    }

    void requestRepaint() {
        canvas.redraw();
    }

    public OffsetDateTime mouse2time(int mouseX) {
        var totalMillis = MILLIS.between(start, stop);
        var totalPoints = getBounds().width;
        var offsetMillis = (mouseX / (double) totalPoints) * totalMillis;
        return start.plus((int) offsetMillis, MILLIS);
    }

    public double positionTime(OffsetDateTime time) {
        return distanceBetween(start, time);
    }

    public double distanceBetween(OffsetDateTime time1, OffsetDateTime time2) {
        var millis = MILLIS.between(time1, time2);
        var totalMillis = MILLIS.between(start, stop);
        return canvas.getBounds().width * (millis / (double) totalMillis);
    }

    public void zoomIn() {
        zoom(0.5, null);
    }

    public void zoomOut() {
        zoom(2, null);
    }

    public void zoom(double factor, OffsetDateTime relto) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Zoom factor should be a positive number");
        }
        if (relto == null) {
            relto = getCenter();
        }

        var reltoRatio = MILLIS.between(start, relto) / (double) MILLIS.between(start, stop);
        var prevRange = (double) MILLIS.between(start, stop);
        var nextRange = prevRange * factor;

        var start = relto.minus((long) (reltoRatio * nextRange), MILLIS);
        var stop = relto.plus((long) ((1 - reltoRatio) * nextRange), MILLIS);
        setBounds(start, stop);
    }

    public void addCanvasMouseListener(MouseListener listener) {
        canvas.addMouseListener(listener);
    }

    public void addCanvasMouseMoveListener(MouseMoveListener listener) {
        canvas.addMouseMoveListener(listener);
    }

    public void addViewportChangeListener(ViewportChangeListener listener) {
        viewportChangeListeners.add(listener);
    }

    public void removeViewportChangeListener(ViewportChangeListener listener) {
        viewportChangeListeners.remove(listener);
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getStop() {
        return stop;
    }

    public OffsetDateTime getSelectionStart() {
        return selectionStart;
    }

    public OffsetDateTime getSelectionStop() {
        return selectionStop;
    }

    public List<Histogram> getHistograms() {
        return drawables.stream().filter(drawable -> (drawable instanceof Histogram))
                .map(drawable -> (Histogram) drawable).collect(Collectors.toList());
    }

    public List<Histogram> getHistograms(HistogramKind kind) {
        return getHistograms().stream().filter(histogram -> histogram.getKind() == kind).collect(Collectors.toList());
    }

    private void drawSelection(GC gc) {
        if (selectionStart == null) {
            return;
        }

        var x1 = (int) Math.round(positionTime(selectionStart));
        var x2 = (int) Math.round(positionTime(selectionStop));

        gc.setAlpha((int) (255 * 0.15));
        var background = new Color(135, 206, 250);
        gc.setBackground(background);
        gc.fillRectangle(x1, 0, x2 - x1, canvas.getBounds().height);
        gc.setAlpha(255);
        background.dispose();

        var foreground = getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
        gc.setForeground(foreground);
        gc.setLineDash(new int[] { 4, 3 });
        gc.drawLine(x1, 0, x1, canvas.getBounds().height);
        gc.drawLine(x2, 0, x2, canvas.getBounds().height);
        gc.setLineDash(null);
    }

    public void clearLines() {
        drawables.removeIf(drawable -> (drawable instanceof Histogram) || (drawable instanceof Header));
    }
}
