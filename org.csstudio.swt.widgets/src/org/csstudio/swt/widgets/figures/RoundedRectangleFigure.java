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
import org.csstudio.swt.widgets.util.GraphicsUtil;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;

/**
 * An rounded rectangle figure.
 */
public final class RoundedRectangleFigure extends RoundedRectangle implements Introspectable {

    /**
     * The fill grade (0 - 100%).
     */
    private double fill = 100.0;

    /**
     * The orientation (horizontal==true | vertical==false).
     */
    private boolean horizontalFill = true;

    /**
     * The transparent state of the background.
     */
    private boolean transparent = false;

    private Color lineColor = ColorConstants.blue;

    private Color backGradientStartColor = ColorConstants.white;
    private Color foreGradientStartColor = ColorConstants.white;
    private boolean gradient = false;
    private Boolean support3D = null;

    @Override
    protected void fillShape(final Graphics graphics) {

        Rectangle figureBounds = getClientArea();

        if (support3D == null)
            support3D = GraphicsUtil.testPatternSupported(graphics);

        if (!transparent) {
            graphics.pushState();
            if (isEnabled())
                graphics.setBackgroundColor(getBackgroundColor());
            Pattern pattern = null;
            if (gradient && support3D && isEnabled()) {
                pattern = setGradientPattern(graphics, figureBounds, backGradientStartColor, getBackgroundColor());
            }
            graphics.fillRoundRectangle(figureBounds, corner.width, corner.height);
            if (pattern != null)
                pattern.dispose();
            graphics.popState();
        }

        if (getFill() > 0) {
            Rectangle fillRectangle;
            if (horizontalFill) {
                int newW = (int) Math.round(figureBounds.width * (getFill() / 100));
                fillRectangle = new Rectangle(figureBounds.x, figureBounds.y, newW,
                        figureBounds.height);
            } else {
                int newH = (int) Math
                        .round(figureBounds.height * (getFill() / 100));
                fillRectangle = new Rectangle(figureBounds.x, figureBounds.y
                        + figureBounds.height - newH, figureBounds.width, newH);
            }

            graphics.pushState();

            graphics.setClip(fillRectangle);
            if (isEnabled())
                graphics.setBackgroundColor(getForegroundColor());
            Pattern pattern = null;
            if (gradient && support3D && isEnabled()) {
                pattern = setGradientPattern(graphics, figureBounds, foreGradientStartColor, getForegroundColor());
            }
            graphics.fillRoundRectangle(figureBounds, corner.width, corner.height);
            if (pattern != null)
                pattern.dispose();
            graphics.popState();
        }
    }

    /**
     * @param graphics
     * @param figureBounds
     * @return
     */
    protected Pattern setGradientPattern(final Graphics graphics,
            Rectangle figureBounds, Color gradientStartColor, Color fillColor) {
        Pattern pattern;
        int tx = figureBounds.x;
        int ty = figureBounds.y + figureBounds.height;
        if (!horizontalFill) {
            tx = figureBounds.x + figureBounds.width;
            ty = figureBounds.y;
        }
        int alpha = getAlpha() == null ? 255 : getAlpha();
        // Workaround for the pattern zoom bug on ScaledGraphics:
        // The coordinates need to be scaled for ScaledGraphics.
        double scale = graphics.getAbsoluteScale();
        pattern = new Pattern(Display.getCurrent(),
                (int) (figureBounds.x * scale),
                (int) (figureBounds.y * scale),
                (int) (tx * scale),
                (int) (ty * scale),
                gradientStartColor, alpha, fillColor, alpha);
        graphics.setBackgroundPattern(pattern);
        return pattern;
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new ShapeWidgetIntrospector().getBeanInfo(this.getClass());
    }

    public int getCornerHeight() {
        return corner.height;
    }

    public int getCornerWidth() {
        return corner.width;
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
     * @param gradientStartColor
     *            the gradientStartColor to set
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
     * @param gradient
     *            the gradient to set
     */
    public void setGradient(boolean gradient) {
        this.gradient = gradient;
        repaint();
    }

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        float lineInset = Math.max(1.0f, getLineWidth()) / 2.0f;
        int inset1 = (int) Math.floor(lineInset);
        int inset2 = (int) Math.ceil(lineInset);

        Rectangle r = Draw2dSingletonUtil.getRectangle().setBounds(getClientArea());
        r.x += inset1;
        r.y += inset1;
        r.width -= inset1 + inset2;
        r.height -= inset1 + inset2;
        graphics.pushState();
        if (isEnabled())
            graphics.setForegroundColor(lineColor);
        graphics.drawRoundRectangle(r, Math.max(0, corner.width - (int) lineInset),
                Math.max(0, corner.height - (int) lineInset));
        graphics.popState();
    }

    public void setCornerHeight(int value) {
        setCornerDimensions(new Dimension(corner.width, value));
        repaint();
    }

    public void setCornerWidth(int value) {
        setCornerDimensions(new Dimension(value, corner.height));
        repaint();
    }

    /**
     * Sets the fill grade.
     *
     * @param fill
     *            the fill grade.
     */
    public void setFill(final double fill) {
        this.fill = fill;
        repaint();
    }

    /**
     * Sets the orientation (horizontal==true | vertical==false).
     *
     * @param horizontal
     *            The orientation.
     */
    public void setHorizontalFill(final boolean horizontal) {
        this.horizontalFill = horizontal;
        repaint();
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        repaint();
    }

    /**
     * Sets the transparent state of the background.
     *
     * @param transparent
     *            the transparent state.
     */
    public void setTransparent(final boolean transparent) {
        this.transparent = transparent;
        repaint();
    }

}
