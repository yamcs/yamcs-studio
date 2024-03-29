/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util.widgets;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Simple meter widget.
 */
public class MeterWidget extends Canvas {
    /** Line width for scale outline (rest uses width 1) */
    final private static int LINE_WIDTH = 5;

    /** Number of labels (and ticks) */
    final private static int LABEL_COUNT = 5;

    final private static int startAngle = 140;
    final private static int endAngle = 40;
    final private static double scaleWidth = 0.35;

    final private Color backgroundColor = new Color(null, 255, 255, 255);
    final private Color faceColor = new Color(null, 20, 10, 10);
    final private Color needleColor = new Color(null, 20, 0, 200);
    final private Color okColor = new Color(null, 0, 200, 0);
    final private Color warningColor = new Color(null, 200, 200, 0);
    final private Color alarmColor = new Color(null, 250, 0, 0);

    /** Minimum value. */
    private double min = -10.0;

    /** Lower alarm limit. */
    private double lowAlarm = -5.0;

    /** Lower warning limit. */
    private double lowWarning = -4.0;

    /** Upper warning limit. */
    private double highWarning = 4.0;

    /** Upper alarm limit. */
    private double highAlarm = 5.0;

    /** Maximum value. */
    private double max = +10.0;

    /** Display precision. */
    private int precision = 4;

    /** Current value. */
    private double value = 1.0;

    /** Most recent scale image or <code>null</code>. */
    private Image scaleImage;

    /** ClientRect for which the image was created. */
    private Rectangle old_client_rect = new Rectangle(0, 0, 0, 0);

    /** X-coord of needle pivot point */
    private int pivot_x;

    /** Y-coord of needle pivot point */
    private int pivot_y;

    /** X-Radius of scale */
    private int x_radius;

    /** Y-Radius of scale */
    private int y_radius;

    /** Constructor */
    public MeterWidget(Composite parent, int style) {
        // To reduce flicker, don't clear the background.
        // On Linux, however, that seems to corrupt the overall
        // widget layout, so we don't use that option.
        // super(parent, style | SWT.NO_BACKGROUND);
        super(parent, SWT.NO_BACKGROUND);
        addDisposeListener(e -> {
            invalidateScale();
            alarmColor.dispose();
            warningColor.dispose();
            okColor.dispose();
            needleColor.dispose();
            faceColor.dispose();
            backgroundColor.dispose();
        });
        addPaintListener(paintListener);
    }

    /**
     * Configure the meter.
     *
     * @param min
     *            Minimum value.
     * @param lowAlarm
     *            Lower alarm limit.
     * @param lowWarning
     *            Lower warning limit.
     * @param highWarning
     *            Upper warning limit.
     * @param highAlarm
     *            Upper alarm limit.
     * @param max
     *            Maximum value.
     * @param precision
     *            Display precision
     */
    public void setLimits(double min, double lowAlarm, double lowWarning, double highWarning, double highAlarm,
            double max, int precision) {
        if (this.min == min && this.lowAlarm == lowAlarm && this.lowWarning == lowWarning
                && this.highWarning == highWarning && this.highAlarm == highAlarm && this.max == max
                && this.precision == precision) {
            return;
        }

        if (min > max) { // swap
            this.min = min;
            this.max = max;
        } else if (min == max) { // Some fake default range
            this.min = min;
            this.max = min + 10.0;
        } else { // Set as given
            this.min = min;
            this.max = max;
        }

        // Check for limits that are outside the value range
        // or NaN (since EPICS R3.14.11)
        if (lowAlarm > this.min && lowAlarm < this.max) {
            this.lowAlarm = lowAlarm;
        } else {
            this.lowAlarm = this.min;
        }

        if (lowWarning > this.min && lowWarning < this.max) {
            this.lowWarning = lowWarning;
        } else {
            this.lowWarning = this.lowAlarm;
        }

        if (highAlarm > this.min && highAlarm < this.max) {
            this.highAlarm = highAlarm;
        } else {
            this.highAlarm = this.max;
        }

        if (highWarning > this.min && highWarning < this.max) {
            this.highWarning = highWarning;
        } else {
            this.highWarning = this.highAlarm;
        }

        this.precision = precision;
        invalidateScale();
        redraw();
    }

    /** Set current value. */
    public void setValue(double value) {
        if (this.value == value) {
            return;
        }

        this.value = value;
        if (!isDisposed()) {
            redraw();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        // When the widget is disabled, force the redraw.
        var oldEnabled = getEnabled();
        super.setEnabled(enabled);
        if (oldEnabled != enabled) {
            invalidateScale();
            redraw();
        }
    }

    /**
     * Reset the scale.
     * <p>
     * Clears the scale image, so it will be re-computed on redraw.
     */
    private void invalidateScale() {
        if (scaleImage != null) {
            scaleImage.dispose();
            scaleImage = null;
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int width, height;
        height = 100;
        width = 100;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point(width, height);
    }

    /** @return Angle in degrees for given value on scale. */
    private double getAngle(double value) {
        if (value <= min) {
            return startAngle;
        }
        if (value >= max) {
            return endAngle;
        }
        return endAngle + (startAngle - endAngle) * (max - value) / (max - min);
    }

    private PaintListener paintListener = e -> {
        // long start = System.nanoTime();

        var gc = e.gc;

        // Get the rectangle that exactly fills the 'inner' area
        // such that drawRectangle() will match.
        var displayArea = getClientArea();

        // paintScale(client_rect, gc);

        // Background and border
        gc.setForeground(faceColor);
        gc.setBackground(backgroundColor);
        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(SWT.CAP_ROUND);
        gc.setLineJoin(SWT.JOIN_ROUND);

        // To reduce flicker, the scale is drawn as a prepared image into
        // the widget whose background has not been cleared.
        createScaleImage(gc, displayArea);
        if (getEnabled()) {
            gc.drawImage(scaleImage, 0, 0);

            paintNeedle(gc);
        } else { // Not enabled
            var grayed = new Image(gc.getDevice(), scaleImage, SWT.IMAGE_DISABLE);
            gc.drawImage(grayed, 0, 0);
            grayed.dispose();

            var message = "No numeric display info";
            var size = gc.textExtent(message);
            gc.drawString(message, (displayArea.width - size.x) / 2, (displayArea.height - size.y) / 2, true);
        }

        // System.out.println("MeterWidget paint: " + (System.nanoTime() - start));
    };

    private void paintNeedle(GC gc) {
        gc.setLineWidth(LINE_WIDTH);
        gc.setLineCap(SWT.CAP_ROUND);
        gc.setLineJoin(SWT.JOIN_ROUND);

        var needle_angle = getAngle(value);
        var needle_x_radius = (int) ((1 - 0.5 * scaleWidth) * x_radius);
        var needle_y_radius = (int) ((1 - 0.5 * scaleWidth) * y_radius);
        gc.setForeground(needleColor);
        gc.drawLine(pivot_x, pivot_y, (int) (pivot_x + needle_x_radius * Math.cos(Math.toRadians(needle_angle))),
                (int) (pivot_y - needle_y_radius * Math.sin(Math.toRadians(needle_angle))));
    }

    /** Create image of the scale (labels etc.) _if_needed_ */
    private void createScaleImage(GC gc, Rectangle client_rect) {
        // Is there already a matching image?
        if ((scaleImage != null) && old_client_rect.equals(client_rect)) {
            return;
        }

        // Remember the client rect for the next call:
        old_client_rect = client_rect;

        // The area that one can use with drawRectangle()
        // is actually one pixel smaller...
        var real_client_rect = new Rectangle(client_rect.x, client_rect.y, client_rect.width - 1,
                client_rect.height - 1);

        // Create image buffer, prepare GC for it.
        // In case there's old one, delete it.
        if (scaleImage != null) {
            scaleImage.dispose();
        }
        scaleImage = new Image(gc.getDevice(), client_rect);
        var scale_gc = new GC(scaleImage);
        paintScale(real_client_rect, scale_gc);

        scale_gc.dispose();
    }

    private void paintScale(Rectangle displayArea, GC scale_gc) {
        scale_gc.setForeground(faceColor);
        scale_gc.setBackground(backgroundColor);
        scale_gc.setLineWidth(LINE_WIDTH);
        scale_gc.setLineCap(SWT.CAP_ROUND);
        scale_gc.setLineJoin(SWT.JOIN_ROUND);

        // Background, border
        scale_gc.fillRectangle(displayArea);
        scale_gc.drawRectangle(displayArea);

        // Calculate meter area and center it.
        var ratio = 1.654;
        var meterWidth = (int) Math.min(displayArea.width, displayArea.height * ratio);
        var meterHeight = (int) Math.min(displayArea.height, displayArea.width / ratio);
        var meterX = (displayArea.width - meterWidth) / 2;
        var meterY = (displayArea.height - meterHeight) / 2;
        var meterArea = new Rectangle(meterX, meterY, meterWidth, meterHeight);
        pivot_x = meterArea.x + meterArea.width / 2;
        pivot_y = meterArea.y + meterArea.height;
        var fmt = NumberFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(precision);

        var min_size = scale_gc.textExtent(fmt.format(min));
        var max_size = scale_gc.textExtent(fmt.format(max));
        var text_width_idea = Math.max(min_size.x / 2, max_size.x / 2);
        var text_height_idea = min_size.y;

        // Labels should somehow fit around the outside of the scale
        var tick_x_radius = meterArea.width / 2 - text_width_idea;
        var tick_y_radius = meterArea.height - 2 * text_height_idea;
        y_radius = tick_y_radius - text_height_idea;
        x_radius = tick_x_radius * y_radius / tick_y_radius;
        // Inner radius of scale.
        var x_radius2 = (int) ((1 - scaleWidth) * x_radius);
        var y_radius2 = (int) ((1 - scaleWidth) * y_radius);
        // Lower end of ticks.
        var tick_x_radius2 = (int) (0.6 * x_radius2);
        var tick_y_radius2 = (int) (0.6 * y_radius2);

        // Path for outline of scale
        var scale_path = createSectionPath(scale_gc.getDevice(), pivot_x, pivot_y, x_radius, y_radius, x_radius2,
                y_radius2, startAngle, endAngle);
        // Fill scale with 'ok' color
        scale_gc.setBackground(okColor);
        scale_gc.fillPath(scale_path);
        // Border around the scale drawn later...

        // Colored alarm sections
        var high_alarm_start = (int) getAngle(highAlarm);
        var high_alarm_path = createSectionPath(scale_gc.getDevice(), pivot_x, pivot_y, x_radius, y_radius, x_radius2,
                y_radius2, high_alarm_start, endAngle);
        scale_gc.setBackground(alarmColor);
        scale_gc.fillPath(high_alarm_path);
        high_alarm_path.dispose();

        var low_alarm_end = (int) getAngle(lowAlarm);
        var low_alarm_path = createSectionPath(scale_gc.getDevice(), pivot_x, pivot_y, x_radius, y_radius, x_radius2,
                y_radius2, startAngle, low_alarm_end);
        scale_gc.fillPath(low_alarm_path);
        low_alarm_path.dispose();

        // Warning sections
        var high_warning_start = (int) getAngle(highWarning);
        var high_warning_path = createSectionPath(scale_gc.getDevice(), pivot_x, pivot_y, x_radius, y_radius, x_radius2,
                y_radius2, high_warning_start, high_alarm_start);
        scale_gc.setBackground(warningColor);
        scale_gc.fillPath(high_warning_path);
        high_warning_path.dispose();

        var low_warning_end = (int) getAngle(lowWarning);
        var low_warning_path = createSectionPath(scale_gc.getDevice(), pivot_x, pivot_y, x_radius, y_radius, x_radius2,
                y_radius2, low_alarm_end, low_warning_end);
        scale_gc.fillPath(low_warning_path);
        low_warning_path.dispose();

        // Scale outline
        scale_gc.drawPath(scale_path);
        scale_path.dispose();

        // Labels and tick marks
        scale_gc.setLineWidth(1);
        for (var i = 0; i < LABEL_COUNT; ++i) {
            var label_value = min + (max - min) * i / (LABEL_COUNT - 1);
            var angle = getAngle(label_value);
            var cos_angle = Math.cos(Math.toRadians(angle));
            var sin_angle = Math.sin(Math.toRadians(angle));
            scale_gc.drawLine((int) (pivot_x + tick_x_radius2 * cos_angle),
                    (int) (pivot_y - tick_y_radius2 * sin_angle), (int) (pivot_x + tick_x_radius * cos_angle),
                    (int) (pivot_y - tick_y_radius * sin_angle));

            var label_text = fmt.format(label_value);
            var size = scale_gc.textExtent(label_text);

            // Don't print the numbers if disabled
            if (getEnabled()) {
                scale_gc.drawString(label_text, (int) (pivot_x + tick_x_radius * cos_angle) - size.x / 2,
                        (int) (pivot_y - tick_y_radius * sin_angle) - size.y, true);
            }
        }
    }

    /**
     * Create path for a section of the colored scale.
     *
     * @param device
     *            Device
     * @param x0
     *            Center of arcs
     * @param y0
     *            Center of arcs
     * @param x_radius
     *            Outer arc radius
     * @param y_radius
     *            Outer arc radius
     * @param x_radius2
     *            Inner arc radius
     * @param y_radius2
     *            Inner arc radius
     * @param start_angle
     *            degrees
     * @param end_angle
     *            degrees
     * @return
     */
    private Path createSectionPath(Device device, int x0, int y0, int x_radius, int y_radius,
            int x_radius2, int y_radius2, int start_angle, int end_angle) {
        var path = new Path(device);
        // Right edge
        path.moveTo((float) (x0 + x_radius2 * Math.cos(Math.toRadians(end_angle))),
                (float) (y0 - y_radius2 * Math.sin(Math.toRadians(end_angle))));
        // Upper edge
        path.addArc(x0 - x_radius, y0 - y_radius, 2 * x_radius, 2 * y_radius, end_angle, start_angle - end_angle);
        // Left edge
        path.lineTo((float) (x0 + x_radius2 * Math.cos(Math.toRadians(start_angle))),
                (float) (y0 - y_radius2 * Math.sin(Math.toRadians(start_angle))));
        addClockwiseArc(path, x0, y0, x_radius2, y_radius2, start_angle, end_angle);
        path.close();
        return path;
    }

    /**
     * Add a clockwise arc from start to end angle to path.
     *
     * @param path
     * @param x0
     *            Center of arc
     * @param y0
     *            Center of arc
     * @param x_radius
     *            X radius of arc
     * @param y_radius
     *            Y radius of arc
     * @param start_angle
     *            start degrees (0=east, 90=north)
     * @param end_angle
     *            end degrees
     */
    private void addClockwiseArc(Path path, int x0, int y0, int x_radius, int y_radius,
            float start_angle, float end_angle) {
        // TODO Would like to draw arc back, i.e. go clockwise,
        // but SWT didn't do that on all platforms, so we draw the arc ourselves.
        // Linux: OK
        // OS X : Rendering errors
        // if (false)
        // path.addArc(x0 - x_radius, y0 - y_radius,
        // 2*x_radius, 2*y_radius,
        // start_angle, end_angle-start_angle);
        // else
        // {
        var d_rad = Math.toRadians(5);
        var start_rad = Math.toRadians(start_angle);
        var end_rad = Math.toRadians(end_angle);
        var rad = start_rad;
        while (rad >= end_rad) {
            path.lineTo((float) (x0 + x_radius * Math.cos(rad)), (float) (y0 - y_radius * Math.sin(rad)));
            rad -= d_rad;
        }
        path.lineTo((float) (x0 + x_radius * Math.cos(end_rad)), (float) (y0 - y_radius * Math.sin(end_rad)));
        // }
    }
}
