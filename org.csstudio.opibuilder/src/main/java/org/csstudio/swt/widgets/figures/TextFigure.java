/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
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

import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;

/**
 * A text figure without wrapping capability.
 */
public class TextFigure extends Figure implements Introspectable, ITextFigure {

    protected V_ALIGN verticalAlignment = V_ALIGN.TOP;
    protected H_ALIGN horizontalAlignment = H_ALIGN.LEFT;

    protected boolean runMode;

    protected boolean selectable = true;
    private String text = "";
    private Point textLocation;
    private Dimension textSize;

    private final Point POINT_ZERO = new Point(0, 0);

    private double rotate = 0;

    /**
     * The real font that is used for drawing. Sometime the font need to be shrinked to fit the widget.
     */
    private Font realFont;

    private boolean fontHeightInPixels = false;

    public TextFigure() {
        this(false);
    }

    /**
     * Constructor
     *
     * @param runMode
     *            true if this figure is in run mode; false if in edit mode.
     */
    public TextFigure(boolean runMode) {
        this.runMode = runMode;
    }

    protected void calculateTextLocation(Font font) {
        if (getRotate() == 0) {
            if (verticalAlignment == V_ALIGN.TOP && horizontalAlignment == H_ALIGN.LEFT) {
                textLocation = POINT_ZERO;
                return;
            }
            var textArea = getTextArea();
            var textSize = getTextSize(font);
            var x = 0;

            switch (horizontalAlignment) {
            case CENTER:
                x = (textArea.width - textSize.width) / 2;
                break;
            case RIGHT:
                x = textArea.width - textSize.width;
                break;
            case LEFT:
            default:
                break;
            }

            var y = 0;
            if (textArea.height > textSize.height) {
                switch (verticalAlignment) {
                case MIDDLE:
                    y = (textArea.height - textSize.height) / 2;
                    break;
                case BOTTOM:
                    y = textArea.height - textSize.height;
                    break;
                case TOP:
                default:
                    break;
                }
            }

            textLocation = new Point(x, y);
        } else {
            // rotated text
            var textArea = getTextArea();
            var textSize = getTextSize(font);
            var theta = Math.toRadians(getRotate());

            var x = textArea.width / 2;
            var y = textArea.height / 2;

            switch (horizontalAlignment) {
            case CENTER:
                if (getRotate() <= 90) {
                    var w = textSize.width * Math.cos(theta) + textSize.height * Math.sin(theta);
                    x = (int) ((textArea.width - w) / 2.0 + textSize.height * Math.sin(theta));
                } else if (getRotate() <= 180) {
                    var w = textSize.height * Math.sin(theta) - textSize.width * Math.cos(theta);
                    x = (int) (textArea.width - (textArea.width - w) / 2.0);
                } else if (getRotate() <= 270) {
                    var w = -textSize.width * Math.cos(theta) - textSize.height * Math.sin(theta);
                    x = (int) ((textArea.width - w) / 2.0 - textSize.width * Math.cos(theta));
                } else {
                    var w = textSize.width * Math.cos(theta) - textSize.height * Math.sin(theta);
                    x = (int) ((textArea.width - w) / 2.0);
                }
                break;
            case RIGHT:
                if (getRotate() <= 90) {
                    x = textArea.width - (int) (textSize.width * Math.cos(theta));
                } else if (getRotate() <= 180) {
                    x = textArea.width;
                } else if (getRotate() <= 270) {
                    x = textArea.width - (int) (-textSize.height * Math.sin(theta));
                } else {
                    x = textArea.width - (int) (textSize.width * Math.cos(theta) - textSize.height * Math.sin(theta));
                }
                break;
            case LEFT:
            default:
                if (getRotate() <= 90) {
                    x = (int) (textSize.height * Math.sin(theta));
                } else if (getRotate() <= 180) {
                    x = (int) (textSize.height * Math.sin(theta) - textSize.width * Math.cos(theta));
                } else if (getRotate() <= 270) {
                    x = (int) (-textSize.width * Math.cos(theta));
                } else {
                    x = 0;
                }
                break;
            }

            switch (verticalAlignment) {
            case MIDDLE:
                if (getRotate() <= 90) {
                    var h = textSize.width * Math.sin(theta) + textSize.height * Math.cos(theta);
                    y = (int) ((textArea.height - h) / 2.0);
                } else if (getRotate() <= 180) {
                    var h = textSize.width * Math.sin(theta) - textSize.height * Math.cos(theta);
                    y = (int) ((textArea.height - h) / 2.0 - textSize.height * Math.cos(theta));
                } else if (getRotate() <= 270) {
                    var h = -textSize.width * Math.sin(theta) - textSize.height * Math.cos(theta);
                    y = (int) (textArea.height - (textArea.height - h) / 2.0);
                } else {
                    var h = textSize.height * Math.cos(theta) - textSize.width * Math.sin(theta);
                    y = (int) ((textArea.height - h) / 2.0 - textSize.height * Math.sin(theta));
                }
                break;
            case BOTTOM:
                if (getRotate() <= 90) {
                    y = textArea.height - (int) (textSize.width * Math.sin(theta) + textSize.height * Math.cos(theta));
                } else if (getRotate() <= 180) {
                    y = textArea.height - (int) (textSize.width * Math.sin(theta));
                } else if (getRotate() <= 270) {
                    y = textArea.height;
                } else {
                    y = textArea.height - (int) (textSize.height * Math.cos(theta));
                }
                break;
            case TOP:
            default:
                if (getRotate() <= 90) {
                    y = 0;
                } else if (getRotate() <= 180) {
                    y = (int) (-textSize.height * Math.cos(theta));
                } else if (getRotate() <= 270) {
                    y = (int) (-textSize.width * Math.sin(theta) - textSize.height * Math.cos(theta));
                } else {
                    y = (int) (-textSize.width * Math.sin(theta));
                }
                break;
            }

            textLocation = new Point(x, y);
        }
    }

    protected Dimension calculateTextSize(Font font) {
        return Draw2dSingletonUtil.getTextUtilities().getTextExtents(text, font);
    }

    protected void clearLocationSize() {
        textLocation = null;
        textSize = null;
    }

    @Override
    public boolean containsPoint(int x, int y) {
        if (runMode && !selectable) {
            return false;
        } else {
            return super.containsPoint(x, y);
        }
    }

    public Dimension getAutoSizeDimension() {
        return getPreferredSize().getCopy().expand(getInsets().getWidth(), getInsets().getHeight());
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    /**
     * @return the h_alignment
     */
    public H_ALIGN getHorizontalAlignment() {
        return horizontalAlignment;
    }

    @Override
    public Dimension getMinimumSize(int wHint, int hHint) {
        return getTextSize(getFont());
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        return getTextSize(getFont());
    }

    @Override
    public String getText() {
        return text;
    }

    protected Rectangle getTextArea() {
        return getClientArea();
    }

    protected Point getTextLocation(Font font) {
        if (textLocation != null) {
            return textLocation;
        }
        calculateTextLocation(font);
        return textLocation;
    }

    protected Dimension getTextSize(Font font) {
        if (font != getFont()) {
            return calculateTextSize(font);
        }
        if (textSize == null) {
            textSize = calculateTextSize(font);
        }
        return textSize;
    }

    /**
     * @return the v_alignment
     */
    public V_ALIGN getVerticalAlignment() {
        return verticalAlignment;
    }

    @Override
    public void invalidate() {
        clearLocationSize();
        super.invalidate();
    }

    /**
     * @return the runMode
     */
    public boolean isRunMode() {
        return runMode;
    }

    /**
     * @return the selectable
     */
    public boolean isSelectable() {
        return selectable;
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        if (text.length() == 0) {
            return;
        }
        var clientArea = getClientArea();
        if (realFont == null) {
            realFont = getFont();
        }
        var h = getTextSize(realFont).height;
        if (realFont != getFont() && h < clientArea.height - 2) {
            realFont = getFont();
            h = getTextSize(realFont).height;
        }
        var font = realFont;

        var i = 0;
        // Shrink font size to fit the figure, if fonts are not
        // explicitly sized in pixels.
        if (!fontHeightInPixels) {
            while (h > (clientArea.height + 2) && h > 10 && i++ < 20) {
                var fd = font.getFontData()[0];
                fd.setHeight(fd.getHeight() - 1);
                font = CustomMediaFactory.getInstance().getFont(fd);
                h = getTextSize(font).height;
            }
        }
        realFont = font;
        graphics.setFont(font);
        var textArea = getTextArea();
        graphics.translate(textArea.x, textArea.y);
        if (getRotate() == 0) {
            graphics.drawText(text, getTextLocation(font));
        } else {
            try {
                graphics.pushState();
                graphics.translate(getTextLocation(font));
                graphics.rotate((float) getRotate());
                graphics.drawText(text, 0, 0);
            } finally {
                graphics.popState();
            }
        }

        graphics.translate(-textArea.x, -textArea.y);
    }

    @Override
    public void setFont(Font f) {
        realFont = f;
        super.setFont(f);
    }

    public void setFontPixels(boolean fontPixels) {
        if (fontHeightInPixels == fontPixels) {
            return;
        }
        fontHeightInPixels = fontPixels;
        revalidate();
        repaint();
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        repaint();
    }

    public void setHorizontalAlignment(H_ALIGN hAlignment) {
        if (horizontalAlignment == hAlignment) {
            return;
        }
        horizontalAlignment = hAlignment;
        revalidate();
        repaint();
    }

    /**
     * @param runMode
     *            the runMode to set
     */
    public void setRunMode(boolean runMode) {
        this.runMode = runMode;
    }

    /**
     * @param selectable
     *            the selectable to set
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void setText(String s) {
        // "text" will never be null.
        if (s == null) {
            s = "";
        }
        if (text.equals(s)) {
            return;
        }
        clearLocationSize();
        text = s;

        repaint();
    }

    public void setVerticalAlignment(V_ALIGN vAlignment) {
        if (verticalAlignment == vAlignment) {
            return;
        }
        verticalAlignment = vAlignment;
        revalidate();
        repaint();
    }

    public double getRotate() {
        return rotate;
    }

    public void setRotate(double rotate) {
        this.rotate = rotate;
        revalidate();
        repaint();
    }

    public enum H_ALIGN {
        LEFT("Left"), CENTER("Center"), RIGHT("Right");

        public static String[] stringValues() {
            var result = new String[values().length];
            var i = 0;
            for (var h : values()) {
                result[i++] = h.toString();
            }
            return result;
        }

        String descripion;

        H_ALIGN(String description) {
            descripion = description;
        }

        @Override
        public String toString() {
            return descripion;
        }
    }

    public enum V_ALIGN {
        TOP("Top"), MIDDLE("Middle"), BOTTOM("Bottom");

        public static String[] stringValues() {
            var result = new String[values().length];
            var i = 0;
            for (var h : values()) {
                result[i++] = h.toString();
            }
            return result;
        }

        String descripion;

        V_ALIGN(String description) {
            descripion = description;
        }

        @Override
        public String toString() {
            return descripion;
        }
    }
}
