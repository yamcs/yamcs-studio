/********************************************************************************
 * Copyright (c) 2006 DESY and others
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
import org.csstudio.swt.widgets.introspection.ShapeWidgetIntrospector;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * A rectangle figure.
 */
public final class OPIRectangleFigure extends RectangleFigure implements Introspectable {
    /**
     * The fill grade (0 - 100%).
     */
    private double fill = 100;
    private boolean runMode;

    /**
     * The orientation (horizontal==true | vertical==false).
     */
    private boolean horizontalFill = true;

    /** The transparent state of the background. */
    private boolean transparent = false;
    /** Whether the rectangle should be selectable at runtime. */
    private boolean selectable;

    private Color lineColor = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_PURPLE);

    private Color backGradientStartColor = ColorConstants.white;
    private Color foreGradientStartColor = ColorConstants.white;
    private boolean gradient = false;

    public OPIRectangleFigure(boolean runMode) {
        this.runMode = runMode;
    }

    @Override
    protected synchronized void fillShape(Graphics graphics) {
        var figureBounds = getClientArea();
        if (!transparent) {
            if (isEnabled()) {
                graphics.setBackgroundColor(getBackgroundColor());
            }
            if (gradient) {
                graphics.setForegroundColor(backGradientStartColor);
                graphics.fillGradient(figureBounds, horizontalFill);
            } else {
                graphics.fillRectangle(figureBounds);
            }
        }
        if (getFill() > 0) {
            if (isEnabled()) {
                graphics.setBackgroundColor(getForegroundColor());
            }
            Rectangle fillRectangle;
            if (horizontalFill) {
                var newW = (int) Math.round(figureBounds.width * (getFill() / 100));
                fillRectangle = new Rectangle(figureBounds.x, figureBounds.y, newW, figureBounds.height);
            } else {
                var newH = (int) Math.round(figureBounds.height * (getFill() / 100));
                fillRectangle = new Rectangle(figureBounds.x, figureBounds.y + figureBounds.height - newH,
                        figureBounds.width, newH);
            }
            if (gradient) {
                graphics.setForegroundColor(foreGradientStartColor);
                graphics.fillGradient(fillRectangle, horizontalFill);
            } else {
                graphics.fillRectangle(fillRectangle);
            }
        }
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new ShapeWidgetIntrospector().getBeanInfo(this.getClass());
    }

    /**
     * Gets the fill grade.
     *
     * @return the fill grade
     */
    public double getFill() {
        return fill;
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

    /**
     * @return the gradientStartColor
     */
    public Color getBackGradientStartColor() {
        return backGradientStartColor;
    }

    public Color getForeGradientStartColor() {
        return foreGradientStartColor;
    }

    /**
     * @return the gradient
     */
    public boolean isGradient() {
        return gradient;
    }

    /**
     * @param gradient
     *            the gradient to set
     */
    public void setGradient(boolean gradient) {
        this.gradient = gradient;
        repaint();
    }

    /**
     * Set gradient start color.
     * 
     * @param gradientStartColor
     */
    public void setBackGradientStartColor(Color gradientStartColor) {
        this.backGradientStartColor = gradientStartColor;
        repaint();
    }

    public void setForeGradientStartColor(Color foreGradientStartColor) {
        this.foreGradientStartColor = foreGradientStartColor;
        repaint();
    }

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        var lineInset = Math.max(1.0f, getLineWidth()) / 2.0f;
        var inset1 = (int) Math.floor(lineInset);
        var inset2 = (int) Math.ceil(lineInset);

        var r = Draw2dSingletonUtil.getRectangle().setBounds(getClientArea());
        r.x += inset1;
        r.y += inset1;
        r.width -= inset1 + inset2;
        r.height -= inset1 + inset2;
        if (isEnabled()) {
            graphics.setForegroundColor(lineColor);
        }
        graphics.drawRectangle(r);
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
        this.horizontalFill = horizontal;
        repaint();
    }

    /**
     * @param lineColor
     *            the lineColor to set
     */
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

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public boolean containsPoint(int x, int y) {
        if (runMode && !selectable) {
            return false;
        } else {
            return super.containsPoint(x, y);
        }
    }

}
