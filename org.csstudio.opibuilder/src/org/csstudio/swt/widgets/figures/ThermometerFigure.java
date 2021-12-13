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

import org.csstudio.swt.widgets.util.GraphicsUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScale;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScale.Orientation;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScaledMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A Thermometer figure
 */
public class ThermometerFigure extends AbstractLinearMarkedFigure {

    /**
     * Temperature Unit Enum
     */
    public enum TemperatureUnit {
        CELSIUS("Celsius", "\u2103"), FAHRENHEIT("Fahrenheit", "\u2109"), KELVIN("Kelvin", "K"), NONE("None", "");

        public static String[] stringValues() {
            var result = new String[values().length];
            var i = 0;
            for (var t : values()) {
                result[i++] = t.toString();
            }
            return result;
        }

        private String name, unitString;

        TemperatureUnit(String name, String unitString) {
            this.name = name;
            this.unitString = unitString;
        }

        public String getUnitString() {
            return unitString;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private Color fillColor;

    private Color fillBackgroundColor = GRAY_COLOR;
    private Color contrastFillColor;
    private TemperatureUnit temperatureUnit;

    private Pipe pipe;

    private Bulb bulb;
    private Label bulbLabel;

    private Label unit;

    private boolean effect3D = true;

    private final static Color RED_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_RED);

    private final static Color GRAY_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_GRAY);

    private final static Color WHITE_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_WHITE);
    private final static Color BLACK_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_BLACK);

    public ThermometerFigure() {

        ((LinearScale) scale).setOrientation(Orientation.VERTICAL);
        scale.setScaleLineVisible(false);
        pipe = new Pipe();
        bulb = new Bulb();
        bulbLabel = new Label();
        unit = new Label();

        setLayoutManager(new ThermoLayout());

        add(scale, ThermoLayout.SCALE);
        add(marker, ThermoLayout.MARKERS);
        add(pipe, ThermoLayout.PIPE);
        add(unit, ThermoLayout.UNIT);
        add(bulb, ThermoLayout.BULB);
        add(bulbLabel, ThermoLayout.BULB_LABEL);
        setFillColor(RED_COLOR);
        setTemperatureUnit(TemperatureUnit.CELSIUS);
    }

    /**
     * @return the fillBackgroundColor
     */
    public Color getFillBackgroundColor() {
        return fillBackgroundColor;
    }

    /**
     * @return the fillColor
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * @return the temperatureUnit
     */
    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    /**
     * @return the effect3D
     */
    public boolean isEffect3D() {
        return effect3D;
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    /**
     * @param effect3D
     *            the effect3D to set
     */
    public void setEffect3D(boolean effect3D) {
        if (this.effect3D == effect3D) {
            return;
        }
        this.effect3D = effect3D;
        repaint();
    }

    /**
     * @param fillBackgroundColor
     *            the fillBackgroundColor to set
     */
    public void setFillBackgroundColor(Color fillBackgroundColor) {
        if (this.fillBackgroundColor != null && this.fillBackgroundColor.equals(fillBackgroundColor)) {
            return;
        }
        this.fillBackgroundColor = fillBackgroundColor;
        repaint();
    }

    /**
     * @param fillColor
     *            the fillColor to set
     */
    public void setFillColor(Color fillColor) {
        if (this.fillColor != null && this.fillColor.equals(fillColor)) {
            return;
        }
        this.fillColor = fillColor;
        var blue = 255 - fillColor.getBlue();
        var green = 255 - fillColor.getGreen();
        var red = fillColor.getRed();
        contrastFillColor = CustomMediaFactory.getInstance().getColor(new RGB(red, green, blue));
        repaint();
    }

    @Override
    public void setForegroundColor(Color fg) {
        super.setForegroundColor(fg);
    }

    public void setShowBulb(boolean showBulb) {
        bulb.setVisible(showBulb);
        revalidate();
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        if (this.temperatureUnit == temperatureUnit) {
            return;
        }
        this.temperatureUnit = temperatureUnit;
        unit.setText(temperatureUnit.getUnitString());
        unit.setVisible(temperatureUnit != TemperatureUnit.NONE);
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);
    }

    class Bulb extends Ellipse {

        public final static int MAX_DIAMETER = 40;
        private final Color EFFECT3D_BULB_COLOR = CustomMediaFactory.getInstance().getColor(new RGB(140, 140, 140));

        public Bulb() {
            setOutline(true);
        }

        @Override
        protected void fillShape(Graphics graphics) {
            graphics.setAntialias(SWT.ON);
            var support3D = false;
            if (effect3D) {
                support3D = GraphicsUtil.testPatternSupported(graphics);
            }

            if (effect3D && support3D) {
                graphics.setBackgroundColor(fillColor);
                super.fillShape(graphics);
                // int l = (int) ((bounds.width - lineWidth)*0.293/2);
                Pattern backPattern = null;

                backPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), bounds.x + lineWidth,
                        bounds.y + lineWidth, bounds.x + bounds.width - lineWidth, bounds.y + bounds.height - lineWidth,
                        WHITE_COLOR, 255, fillColor, 0);
                graphics.setBackgroundPattern(backPattern);
                super.fillShape(graphics);
                backPattern.dispose();

            } else {
                graphics.setBackgroundColor(fillColor);
                super.fillShape(graphics);
            }

            bulbLabel.setFont(getFont());
            bulbLabel.setText(getValueText());
            bulbLabel.setForegroundColor(contrastFillColor);
        }

        @Override
        protected void outlineShape(Graphics graphics) {
            var support3D = false;
            if (effect3D) {
                support3D = GraphicsUtil.testPatternSupported(graphics);
            }

            if (effect3D && support3D) {
                graphics.setForegroundColor(EFFECT3D_BULB_COLOR);
            } else {
                graphics.setForegroundColor(BLACK_COLOR);
            }
            super.outlineShape(graphics);
            // draw a small rectangle to hide the joint

            if (effect3D && support3D) {
                graphics.setBackgroundColor(fillColor);
                graphics.fillRectangle(new Rectangle(pipe.getBounds().x + pipe.getLineWidth(),
                        ((LinearScale) scale).getValuePosition(scale.getRange().getLower(), false),
                        Pipe.PIPE_WIDTH - pipe.getLineWidth() * 2, 2));
                var backPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), pipe.getBounds().x,
                        ((LinearScale) scale).getValuePosition(scale.getRange().getLower(), false),
                        pipe.getBounds().x + Pipe.PIPE_WIDTH,
                        ((LinearScale) scale).getValuePosition(scale.getRange().getLower(), false), WHITE_COLOR, 255,
                        fillColor, 0);
                graphics.setBackgroundPattern(backPattern);
                graphics.fillRectangle(new Rectangle(pipe.getBounds().x + pipe.getLineWidth(),
                        ((LinearScale) scale).getValuePosition(scale.getRange().getLower(), false),
                        Pipe.PIPE_WIDTH - pipe.getLineWidth() * 2, 2));
                backPattern.dispose();

            } else {
                graphics.setBackgroundColor(fillColor);

                graphics.fillRoundRectangle(
                        new Rectangle(pipe.getBounds().x + pipe.getLineWidth(),
                                ((LinearScale) scale).getValuePosition(scale.getRange().getLower(), false),
                                Pipe.PIPE_WIDTH - pipe.getLineWidth() * 2, ((LinearScale) scale).getMargin()),
                        Pipe.FILL_CORNER, Pipe.FILL_CORNER);
            }
        }
    }

    class Pipe extends RoundedRectangle {

        public final static int FILL_CORNER = 3;
        public final static int PIPE_WIDTH = 15;
        private final Color EFFECT3D_PIPE_COLOR = CustomMediaFactory.getInstance().getColor(new RGB(160, 160, 160));

        public Pipe() {
            setOutline(true);
        }

        @Override
        protected void fillShape(Graphics graphics) {
            corner.height = PIPE_WIDTH / 2;
            corner.width = PIPE_WIDTH / 2;
            graphics.setBackgroundColor(fillBackgroundColor);

            var valuePosition = ((LinearScale) scale).getValuePosition(getCoercedValue(), false);
            if (maximum > minimum) {
                if (value > maximum) {
                    valuePosition -= 10;
                } else if (value < minimum) {
                    valuePosition += 10;
                }
            } else {
                if (value > minimum) {
                    valuePosition += 10;
                } else if (value < maximum) {
                    valuePosition -= 10;
                }
            }
            var support3D = false;
            if (effect3D) {
                support3D = GraphicsUtil.testPatternSupported(graphics);
            }

            if (effect3D && support3D) {
                graphics.setForegroundColor(EFFECT3D_PIPE_COLOR);
                // fill back
                super.fillShape(graphics);
                var backPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), bounds.x, bounds.y,
                        bounds.x + bounds.width, bounds.y, WHITE_COLOR, 255, fillBackgroundColor, 0);
                graphics.setBackgroundPattern(backPattern);
                super.fillShape(graphics);
                backPattern.dispose();

                // fill value
                graphics.setBackgroundColor(fillColor);
                graphics.fillRoundRectangle(new Rectangle(bounds.x + lineWidth, valuePosition,
                        bounds.width - 2 * lineWidth, bounds.height - (valuePosition - bounds.y)), FILL_CORNER,
                        FILL_CORNER);
                backPattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), bounds.x, bounds.y,
                        bounds.x + bounds.width, bounds.y, WHITE_COLOR, 255, fillColor, 0);
                graphics.setBackgroundPattern(backPattern);
                graphics.fillRoundRectangle(new Rectangle(bounds.x + lineWidth, valuePosition,
                        bounds.width - 2 * lineWidth, bounds.height - (valuePosition - bounds.y)), FILL_CORNER,
                        FILL_CORNER);
                backPattern.dispose();
            } else {
                super.fillShape(graphics);
                graphics.setBackgroundColor(fillColor);
                graphics.fillRoundRectangle(new Rectangle(bounds.x + lineWidth, valuePosition,
                        bounds.width - 2 * lineWidth, bounds.height - (valuePosition - bounds.y)), FILL_CORNER,
                        FILL_CORNER);
            }
        }

        public Dimension getCorner() {
            return corner;
        }

        @Override
        public Dimension getPreferredSize(int wHint, int hHint) {
            return new Dimension(PIPE_WIDTH, hHint + 2 * corner.height);
        }
    }

    static class ThermoLayout extends AbstractLayout {

        /** Used as a constraint for the scale. */
        public static final String SCALE = "scale";
        /** Used as a constraint for the pipe indicator. */
        public static final String PIPE = "pipe";
        /** Used as a constraint for the alarm ticks */
        public static final String MARKERS = "markers";
        /** Used as a constraint for the bulb in the below of pipe. */
        public static final String BULB = "bulb";
        public static final String BULB_LABEL = "bulbLabel";
        /** Used as a constraint for the unit label */
        public static final String UNIT = "unit";
        private LinearScale scale;
        private LinearScaledMarker marker;
        private Pipe pipe;
        private IFigure bulb;
        private IFigure bulbLabel;
        private IFigure unit;

        @Override
        protected Dimension calculatePreferredSize(IFigure container, int w, int h) {
            var insets = container.getInsets();
            var d = new Dimension(64, 4 * 64);
            d.expand(insets.getWidth(), insets.getHeight());
            return d;
        }

        @Override
        public void layout(IFigure container) {
            var area = container.getClientArea();
            Rectangle bulbArea = null;
            if (bulb != null && bulb.isVisible()) {
                var diameter = area.width / 2;
                if (diameter > Bulb.MAX_DIAMETER) {
                    diameter = Bulb.MAX_DIAMETER;
                }
                var x = area.x + area.width / 2 - diameter / 2;
                var spareHeight = (area.height < diameter) ? 0 : (area.height - diameter);
                var y = area.y + spareHeight;
                bulbArea = new Rectangle(x, y, diameter, diameter);
                bulb.setBounds(bulbArea);
                area.height = spareHeight + scale.getMargin();
            }
            var unitSize = new Dimension(0, 0);
            Dimension scaleSize;
            Dimension markerSize;
            Dimension pipeSize;
            if (unit != null && unit.isVisible()) {
                unitSize = unit.getPreferredSize();
                unit.setBounds(new Rectangle(area.x + area.width / 2 - Pipe.PIPE_WIDTH / 2 - unitSize.width, area.y,
                        unitSize.width, unitSize.height));
            }

            if (scale != null) {
                scaleSize = scale.getPreferredSize(-1, area.height - unitSize.height);
                scale.setBounds(new Rectangle(area.x + area.width / 2 - Pipe.PIPE_WIDTH / 2 - scaleSize.width,
                        area.y + unitSize.height, scaleSize.width, scaleSize.height));
            }

            if (marker != null && marker.isVisible()) {
                markerSize = marker.getPreferredSize();
                marker.setBounds(new Rectangle(area.x + area.width / 2 + Pipe.PIPE_WIDTH / 2,
                        marker.getScale().getBounds().y, markerSize.width, markerSize.height));
            }

            if (pipe != null) {
                pipeSize = pipe.getPreferredSize(-1, scale.getTickLength());
                pipe.setBounds(new Rectangle(area.x + area.width / 2 - Pipe.PIPE_WIDTH / 2,
                        scale.getValuePosition(scale.getRange().getUpper(), false) - pipe.getCorner().height,
                        pipeSize.width, pipeSize.height));
            }
            if (bulbArea != null && bulb.isVisible()) {
                var labelSize = bulbLabel.getPreferredSize();
                bulbLabel.setBounds(new Rectangle(bulbArea.x + bulbArea.width / 2 - labelSize.width / 2,
                        bulbArea.y + bulbArea.height / 2 - labelSize.height / 2, labelSize.width, labelSize.height));
            }
        }

        @Override
        public void setConstraint(IFigure child, Object constraint) {
            if (constraint.equals(SCALE)) {
                scale = (LinearScale) child;
            } else if (constraint.equals(MARKERS)) {
                marker = (LinearScaledMarker) child;
            } else if (constraint.equals(PIPE)) {
                pipe = (Pipe) child;
            } else if (constraint.equals(BULB)) {
                bulb = child;
            } else if (constraint.equals(UNIT)) {
                unit = child;
            } else if (constraint.equals(BULB_LABEL)) {
                bulbLabel = child;
            }
        }
    }
}
