/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figureparts;

import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.visualization.xygraph.linearscale.AbstractScale.LabelSide;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScale;
import org.eclipse.nebula.visualization.xygraph.linearscale.LinearScale.Orientation;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * The color map figure which can be used as the ramp of intensity graph.
 */
public class ColorMapRamp extends Figure {

    private double min, max;

    private double[] mapData;

    private ColorMap colorMap;
    private LinearScale scale;
    private ColorMapFigure colorMapFigure;
    private final static int RAMP_WIDTH = 25;

    public ColorMapRamp() {
        mapData = new double[256];
        min = 0;
        max = 1;
        updateMapData();
        colorMap = new ColorMap(PredefinedColorMap.GrayScale, true, true);

        scale = new LinearScale();
        scale.setOrientation(Orientation.VERTICAL);
        scale.setScaleLineVisible(false);
        scale.setTickLableSide(LabelSide.Secondary);
        scale.setMinorTicksVisible(false);
        scale.setRange(min, max);
        scale.setMajorTickMarkStepHint(50);
        colorMapFigure = new ColorMapFigure();
        add(colorMapFigure);
        add(scale);
    }

    private void updateMapData() {
        for (var j = 0; j < 256; j++) {
            mapData[j] = max - j * (max - min) / 255.0;
        }
    }

    @Override
    protected void layout() {
        var clientArea = getClientArea();
        var scaleSize = scale.getPreferredSize(clientArea.width, clientArea.height);
        scale.setBounds(new Rectangle(clientArea.x + clientArea.width - scaleSize.width, clientArea.y, scaleSize.width,
                clientArea.height));

        colorMapFigure.setBounds(new Rectangle(clientArea.x, scale.getValuePosition(max, false),
                clientArea.width - scaleSize.width, scale.getTickLength()));
        super.layout();
    }

    @Override
    public Dimension getPreferredSize(int hint, int hint2) {
        var result = super.getPreferredSize(hint, hint2);
        result.width = RAMP_WIDTH + scale.getPreferredSize(hint, hint2).width;
        return result;
    }

    /**
     * @param min
     *            the min to set
     */
    public void setMin(double min) {
        this.min = min;
        scale.setRange(min, max);
        updateMapData();
    }

    /**
     * @param max
     *            the max to set
     */
    public void setMax(double max) {
        this.max = max;
        scale.setRange(min, max);
        updateMapData();
    }

    /**
     * @param colorMap
     *            the colorMap to set
     */
    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        scale.setFont(f);
    }

    class ColorMapFigure extends Figure {

        @Override
        protected void paintClientArea(Graphics graphics) {
            super.paintClientArea(graphics);
            var clientArea = getClientArea();
            var image = new Image(Display.getCurrent(), colorMap.drawImage(mapData, 1, 256, max, min));
            graphics.drawImage(image, new Rectangle(image.getBounds()), clientArea);
            image.dispose();
        }
    }
}
