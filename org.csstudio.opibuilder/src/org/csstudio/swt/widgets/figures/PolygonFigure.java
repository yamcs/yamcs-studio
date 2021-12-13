/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figures;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.swt.widgets.introspection.PolyWidgetIntrospector;
import org.csstudio.swt.widgets.util.PointsUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.handles.HandleBounds;
import org.eclipse.swt.graphics.Color;

/**
 * A polygon figure.
 */
public final class PolygonFigure extends Polygon implements HandleBounds, Introspectable {

    /**
     * The fill grade (0 - 100%).
     */
    private double fill = 100.0;

    private boolean horizontalFill;

    private boolean transparent;

    private Color lineColor = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_BLUE);

    /**
     * Constructor.
     */
    public PolygonFigure() {
        setFill(true);
        setBackgroundColor(ColorConstants.darkGreen);
    }

    @Override
    protected void fillShape(Graphics graphics) {
        graphics.pushState();
        var figureBounds = getBounds();
        if (!transparent) {
            if (isEnabled()) {
                graphics.setBackgroundColor(getBackgroundColor());
            }
            graphics.fillPolygon(getPoints());
        }
        if (getFill() > 0) {
            if (isEnabled()) {
                graphics.setBackgroundColor(getForegroundColor());
            }
            if (horizontalFill) {
                var newW = (int) Math.round(figureBounds.width * (getFill() / 100));
                graphics.setClip(new Rectangle(figureBounds.x, figureBounds.y, newW, figureBounds.height));
            } else {
                var newH = (int) Math.round(figureBounds.height * (getFill() / 100));
                graphics.setClip(new Rectangle(figureBounds.x, figureBounds.y + figureBounds.height - newH,
                        figureBounds.width, newH));
            }
            graphics.fillPolygon(getPoints());
        }
        graphics.popState();
    }

    /**
     * Gets the fill grade.
     *
     * @return the fill grade
     */
    public double getFill() {
        return fill;
    }

    @Override
    public Rectangle getHandleBounds() {
        return getPoints().getBounds();
    }

    /**
     * @return the lineColor
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Gets the transparent state of the background.
     *
     * @return the transparent state of the background
     */
    public boolean getTransparent() {
        return transparent;
    }

    /**
     * Gets the orientation (horizontal==true | vertical==false).
     *
     * @return boolean The orientation
     */
    public boolean isHorizontalFill() {
        return horizontalFill;
    }

    @Override
    protected void outlineShape(Graphics g) {
        g.pushState();
        if (isEnabled()) {
            g.setForegroundColor(lineColor);
        }
        super.outlineShape(g);
        g.popState();
    }

    /**
     * Overridden, to ensure that the bounds rectangle gets repainted each time, the _points of the polygon change.
     */
    @Override
    public void setBounds(Rectangle rect) {
        var points = getPoints();
        if (!points.getBounds().equals(rect)) {
            var oldX = getLocation().x;
            var oldY = getLocation().y;
            points.translate(rect.x - oldX, rect.y - oldY);

            setPoints(PointsUtil.scalePointsBySize(points, rect.width, rect.height));
        }
        invalidate();
        fireFigureMoved();
        repaint();
    }

    /**
     * Sets the fill grade.
     *
     * @param fill
     *            the fill grade.
     */
    public void setFill(double fill) {
        if (this.fill == fill) {
            return;
        }
        this.fill = fill;
        repaint();
    }

    /**
     * Sets the orientation (horizontal==true | vertical==false).
     *
     * @param horizontal
     *            The orientation.
     */
    public void setHorizontalFill(boolean horizontal) {
        if (this.horizontalFill == horizontal) {
            return;
        }
        horizontalFill = horizontal;
        repaint();
    }

    public void setLineColor(Color lineColor) {
        if (this.lineColor != null && this.lineColor.equals(lineColor)) {
            return;
        }
        this.lineColor = lineColor;
        repaint();
    }

    /**
     * Sets the transparent state of the background.
     *
     * @param transparent
     *            the transparent state.
     */
    public void setTransparent(boolean transparent) {
        if (this.transparent == transparent) {
            return;
        }
        this.transparent = transparent;
        repaint();
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new PolyWidgetIntrospector().getBeanInfo(this.getClass());
    }
}
