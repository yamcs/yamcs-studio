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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.widgets.datadefinition.IManualValueChangeListener;
import org.csstudio.swt.widgets.figureparts.Bulb;
import org.csstudio.swt.widgets.figureparts.PolarPoint;
import org.csstudio.swt.widgets.figureparts.RoundScale;
import org.csstudio.swt.widgets.figureparts.RoundScaledRamp;
import org.csstudio.swt.widgets.util.GraphicsUtil;
import org.csstudio.swt.widgets.util.PointsUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;

/**
 * The figure of knob
 */
public class KnobFigure extends AbstractRoundRampedFigure {

    class KnobLayout extends AbstractLayout {

        private static final int GAP_BTW_BULB_SCALE = 4;

        /** Used as a constraint for the scale. */
        public static final String SCALE = "scale";
        /** Used as a constraint for the bulb. */
        public static final String BULB = "bulb";
        /** Used as a constraint for the Ramp */
        public static final String RAMP = "ramp";
        /** Used as a constraint for the thumb */
        public static final String THUMB = "thumb";
        /** Used as a constraint for the value label */
        public static final String VALUE_LABEL = "valueLabel";

        private RoundScale scale;
        private RoundScaledRamp ramp;
        private Bulb bulb;
        private Thumb thumb;
        private Label valueLabel;

        @Override
        protected Dimension calculatePreferredSize(IFigure container, int w, int h) {
            var insets = container.getInsets();
            var d = new Dimension(256, 256);
            d.expand(insets.getWidth(), insets.getHeight());
            return d;
        }

        @Override
        public void layout(IFigure container) {
            var area = container.getClientArea();
            area.width = Math.min(area.width, area.height);
            area.height = area.width;
            area.shrink(BORDER_WIDTH, BORDER_WIDTH);

            var center = area.getCenter();
            Rectangle bulbBounds = null;

            if (scale != null) {
                scale.setBounds(area);
                bulbBounds = area.getCopy();
                bulbBounds.shrink(area.width / 2 - scale.getInnerRadius() + GAP_BTW_BULB_SCALE,
                        area.height / 2 - scale.getInnerRadius() + GAP_BTW_BULB_SCALE);
            }

            if (scale != null && ramp != null && ramp.isVisible()) {
                var rampBounds = area.getCopy();
                ramp.setBounds(rampBounds.shrink(area.width / 2 - scale.getInnerRadius() - ramp.getRampWidth() + 2,
                        area.height / 2 - scale.getInnerRadius() - ramp.getRampWidth() + 2));
            }

            if (valueLabel != null && valueLabel.isVisible()) {
                var labelSize = valueLabel.getPreferredSize();
                valueLabel.setBounds(new Rectangle(bulbBounds.x + bulbBounds.width / 2 - labelSize.width / 2,
                        bulbBounds.y + bulbBounds.height * 3 / 4 - labelSize.height / 2, labelSize.width,
                        labelSize.height));
            }

            if (bulb != null && scale != null && bulb.isVisible()) {
                bulb.setBounds(bulbBounds);
            }

            if (scale != null && thumb != null && thumb.isVisible()) {
                var thumbCenter = new Point(bulbBounds.x + bulbBounds.width * 7.0 / 8.0,
                        bulbBounds.y + bulbBounds.height / 2);
                var valuePosition = 360 - scale.getValuePosition(getCoercedValue(), false);
                thumbCenter = PointsUtil.rotate(thumbCenter, valuePosition, center);
                var thumbDiameter = bulbBounds.width / 6;

                thumb.setBounds(new Rectangle(thumbCenter.x - thumbDiameter / 2, thumbCenter.y - thumbDiameter / 2,
                        thumbDiameter, thumbDiameter));
            }
        }

        @Override
        public void setConstraint(IFigure child, Object constraint) {
            if (constraint.equals(SCALE)) {
                scale = (RoundScale) child;
            } else if (constraint.equals(RAMP)) {
                ramp = (RoundScaledRamp) child;
            } else if (constraint.equals(BULB)) {
                bulb = (Bulb) child;
            } else if (constraint.equals(THUMB)) {
                thumb = (Thumb) child;
            } else if (constraint.equals(VALUE_LABEL)) {
                valueLabel = (Label) child;
            }
        }
    }

    class Thumb extends Ellipse {

        class ThumbDragger extends MouseMotionListener.Stub implements MouseListener {
            private PolarPoint startPP;
            protected double oldValuePosition;
            protected boolean armed;
            Point pole;

            @Override
            public void mouseDoubleClicked(MouseEvent me) {
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                if (!armed) {
                    return;
                }

                var currentPP = PolarPoint.point2PolarPoint(pole, me.getLocation());
                // rotate axis to endAngle
                currentPP.rotateAxis(((RoundScale) scale).getEndAngle(), false);

                // coerce currentPP to min or max
                if (currentPP.theta * 180.0 / Math.PI > (((RoundScale) scale).getLengthInDegrees())) {
                    if (Math.abs(((RoundScale) scale).getValuePosition(getCoercedValue(), true)
                            - (((RoundScale) scale).getLengthInDegrees())) < ((RoundScale) scale).getLengthInDegrees()
                                    / 2.0) {
                        currentPP.theta = ((RoundScale) scale).getLengthInDegrees() * Math.PI / 180.0;
                    } else {
                        currentPP.theta = 0;
                    }
                }

                var difference = currentPP.theta * 180.0 / Math.PI - oldValuePosition;
                var valueChange = calcValueChange(difference, value);
                if (increment <= 0 || Math.abs(valueChange) > increment / 2.0) {
                    // manualSetValue = true;
                    if (increment > 0) {
                        manualSetValue(value + increment * Math.round(valueChange / increment));
                    } else {
                        manualSetValue(value + valueChange);
                    }

                    oldValuePosition = ((RoundScale) scale).getValuePosition(value, true);
                    fireManualValueChange(value);
                    revalidate();
                    KnobFigure.this.repaint();
                }
                me.consume();
            }

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.button != 1) {
                    return;
                }
                armed = true;
                pole = scale.getBounds().getCenter();
                startPP = PolarPoint.point2PolarPoint(pole, bounds.getCenter());
                // rotate axis to endAngle
                startPP.rotateAxis(((RoundScale) scale).getEndAngle(), false);

                oldValuePosition = ((RoundScale) scale).getValuePosition(getCoercedValue(), true);
                me.consume();
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.button != 1) {
                    return;
                }
                if (!armed) {
                    return;
                }
                armed = false;
                me.consume();
            }
        }

        public Thumb() {
            setCursor(Cursors.HAND);
            var thumbDragger = new ThumbDragger();
            addMouseMotionListener(thumbDragger);
            addMouseListener(thumbDragger);
        }

        @Override
        public void setEnabled(boolean value) {
            super.setEnabled(value);
        }

        @Override
        protected void fillShape(Graphics graphics) {
            graphics.setAntialias(SWT.ON);
            Pattern pattern = null;
            graphics.setBackgroundColor(thumbColor);
            var support3D = GraphicsUtil.testPatternSupported(graphics);
            if (support3D && effect3D) {
                try {
                    graphics.setBackgroundColor(thumbColor);
                    super.fillShape(graphics);
                    pattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), bounds.x, bounds.y,
                            bounds.x + bounds.width, bounds.y + bounds.height, WHITE_COLOR, 0, WHITE_COLOR, 255);
                    graphics.setBackgroundPattern(pattern);
                } catch (Exception e) {
                    support3D = false;
                    pattern.dispose();
                }
            }
            super.fillShape(graphics);
            if (effect3D && support3D) {
                pattern.dispose();
            }
            graphics.setForegroundColor(thumbColor);
        }
    }

    private final static Color WHITE_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_WHITE);
    private final static Color GRAY_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_GRAY);

    private final static Font DEFAULT_LABEL_FONT = CustomMediaFactory.getInstance()
            .getFont(new FontData("Arial", 12, SWT.BOLD));

    private final static int BORDER_WIDTH = 2;

    /** The alpha (0 is transparency and 255 is opaque) for disabled paint */
    private static final int DISABLED_ALPHA = 100;

    private boolean effect3D = true;

    private double increment = 1;

    // private boolean manualSetValue = false;
    private Thumb thumb;

    private Bulb bulb;

    private Color thumbColor = GRAY_COLOR;

    private Label valueLabel;

    /**
     * Listeners that react on slider events.
     */
    private List<IManualValueChangeListener> knobListeners = new ArrayList<>();

    public KnobFigure() {
        transparent = true;
        scale.setScaleLineVisible(false);
        ramp.setRampWidth(12);
        valueLabel = new Label();
        valueLabel.setFont(DEFAULT_LABEL_FONT);
        bulb = new Bulb();

        thumb = new Thumb();
        thumb.setOutline(false);
        setLayoutManager(new KnobLayout());
        add(ramp, KnobLayout.RAMP);
        add(bulb, KnobLayout.BULB);
        add(scale, KnobLayout.SCALE);
        add(valueLabel, KnobLayout.VALUE_LABEL);

        add(thumb, KnobLayout.THUMB);
        addFigureListener(source -> {
            ramp.setDirty(true);
            revalidate();
        });
    }

    /**
     * Add a knob listener.
     *
     * @param listener
     *            The knob listener to add.
     */
    public void addManualValueChangeListener(IManualValueChangeListener listener) {
        knobListeners.add(listener);
    }

    /**
     * Convert the difference of two points to the corresponding value to be changed.
     * 
     * @param difference
     *            the different theta value in degrees between two polar points.
     * @param oldValue
     *            the old value before this change
     * @return the value to be changed
     */
    private double calcValueChange(double difference, double oldValue) {
        double change;
        var dragRange = ((RoundScale) scale).getLengthInDegrees();
        if (scale.isLogScaleEnabled()) {
            var c = dragRange / (Math.log10(scale.getRange().getUpper()) - Math.log10(scale.getRange().getLower()));

            change = oldValue * (Math.pow(10, -difference / c) - 1);
        } else {
            change = -(scale.getRange().getUpper() - scale.getRange().getLower()) * difference / dragRange;
        }
        return change;
    }

    /**
     * Inform all knob listeners, that the manual value has changed.
     *
     * @param newManualValue
     *            the new manual value
     */
    private void fireManualValueChange(double newManualValue) {

        for (var l : knobListeners) {
            l.manualValueChanged(newManualValue);
        }
    }

    /**
     * @return the increment
     */
    public double getIncrement() {
        return increment;
    }

    /**
     * @return the thumbColor
     */
    public Color getThumbColor() {
        return thumbColor;
    }

    /**
     * @return the effect3D
     */
    public boolean isEffect3D() {
        return effect3D;
    }

    /**
     * Set Value from manual control of the widget. Value will be coerced in range.
     * 
     * @param value
     */
    public void manualSetValue(double value) {
        setValue(getCoercedValue(value));
    }

    @Override
    protected void paintClientArea(Graphics graphics) {

        super.paintClientArea(graphics);
        if (!isEnabled()) {
            graphics.setAlpha(DISABLED_ALPHA);
            graphics.setBackgroundColor(GRAY_COLOR);
            graphics.fillRectangle(bounds);
        }
    }

    public void removeManualValueChangeListener(IManualValueChangeListener listener) {
        if (knobListeners.contains(listener)) {
            knobListeners.remove(listener);
        }
    }

    @Override
    public void setBounds(Rectangle rect) {

        super.setBounds(rect);
    }

    /**
     * @param color
     *            the bulb color to set
     */
    public void setBulbColor(Color color) {
        bulb.setBulbColor(color);
    }

    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        thumb.setCursor(cursor);
    }

    /**
     * @param effect3D
     *            the effect3D to set
     */
    public void setEffect3D(boolean effect3D) {
        this.effect3D = effect3D;
        bulb.setEffect3D(effect3D);
        repaint();
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if (value) {
            thumb.setCursor(Cursors.HAND);
        }
        // the disabled cursor should be controlled by widget controller.
        repaint();
    }

    @Override
    public void setFont(Font f) {
        scale.setFont(f);
        super.setFont(f);
    }

    /**
     * @param increment
     *            the increment to set
     */
    public void setIncrement(double increment) {
        this.increment = increment;
    }

    /**
     * @param thumbColor
     *            the thumbColor to set
     */
    public void setThumbColor(Color thumbColor) {
        if (this.thumbColor != null && this.thumbColor.equals(thumbColor)) {
            return;
        }
        this.thumbColor = thumbColor;
        repaint();
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);
        valueLabel.setText(getValueText());
        // manualSetValue = false;
    }

    public void setValueLabelVisibility(boolean visible) {
        valueLabel.setVisible(visible);
    }
}
