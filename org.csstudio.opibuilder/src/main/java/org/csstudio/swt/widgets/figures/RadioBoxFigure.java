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
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * The figure of Radio Box.
 */
public class RadioBoxFigure extends AbstractChoiceFigure {

    public RadioBoxFigure(boolean runMode) {
        super(runMode);
        selectedColor = ColorConstants.black;
    }

    @Override
    protected Toggle createToggle(String text) {
        return new RadioBox(text);
    }

    class RadioBox extends Toggle {

        private RadioFigure radio = null;

        /**
         * Constructs a CheckBox with no text.
         */
        public RadioBox() {
            this("");
        }

        /**
         * Constructs a CheckBox with the passed text in its label.
         */
        public RadioBox(String text) {
            radio = new RadioFigure(text);
            setContents(radio);
            if (runMode) {
                addMouseMotionListener(new MouseMotionListener.Stub() {
                    @Override
                    public void mouseEntered(MouseEvent me) {
                        var backColor = getBackgroundColor();
                        var darkColor = GraphicsUtil.mixColors(backColor.getRGB(), new RGB(94, 151, 230), 0.7);
                        setBackgroundColor(CustomMediaFactory.getInstance().getColor(darkColor));
                    }

                    @Override
                    public void mouseExited(MouseEvent me) {
                        setBackgroundColor(RadioBoxFigure.this.getBackgroundColor());
                    }
                });
            }
        }

        /**
         * Adjusts CheckBox's icon depending on selection status.
         */
        protected void handleSelectionChanged() {
            radio.setSelected(isSelected());
        }

        /**
         * Initializes this Clickable by setting a default model and adding a clickable event handler for that model.
         * Also adds a ChangeListener to update icon when selection status changes.
         */
        @Override
        protected void init() {
            super.init();
            addChangeListener(changeEvent -> {
                if (changeEvent.getPropertyName().equals(ButtonModel.SELECTED_PROPERTY)) {
                    handleSelectionChanged();
                }
            });
        }
    }

    class RadioFigure extends Figure {

        private static final int RADIO_RADIUS = 7;
        private static final int DOT_RADIUS = 2;
        private static final int GAP = 4;

        private boolean selected = false;

        private String text;
        private Boolean support3d;

        public RadioFigure(String text) {
            this.text = text;
            if (runMode) {
                setCursor(Cursors.HAND);
            }
        }

        @Override
        protected void paintClientArea(Graphics graphics) {
            super.paintClientArea(graphics);
            if (support3d == null) {
                support3d = GraphicsUtil.testPatternSupported(graphics);
            }
            graphics.setAntialias(support3d ? SWT.ON : SWT.OFF);
            var clientArea = getClientArea();
            var circle = new Rectangle(clientArea.x, clientArea.getCenter().y - RADIO_RADIUS, 2 * RADIO_RADIUS,
                    2 * RADIO_RADIUS);
            graphics.pushState();
            Pattern pattern = null;
            if (support3d) {
                pattern = GraphicsUtil.createScaledPattern(graphics, Display.getCurrent(), circle.x,
                        circle.y, circle.x + circle.width, circle.y + circle.height, ColorConstants.white,
                        graphics.getBackgroundColor());
                graphics.setBackgroundPattern(pattern);
            }
            graphics.fillArc(circle, 0, 360);
            if (pattern != null) {
                pattern.dispose();
            }
            graphics.setForegroundColor(CustomMediaFactory.getInstance().getColor(120, 120, 120));
            graphics.drawArc(circle, 0, 360);
            if (selected) {
                graphics.setBackgroundColor(selectedColor);
                graphics.fillArc(new Rectangle(circle.getCenter().x - DOT_RADIUS, circle.getCenter().y - DOT_RADIUS,
                        2 * DOT_RADIUS + 1, 2 * DOT_RADIUS + 1), 0, 360);
            }
            graphics.popState();
            var textSize = FigureUtilities.getTextExtents(text, graphics.getFont());
            if (!isEnabled()) {
                graphics.translate(1, 1);
                graphics.setForegroundColor(ColorConstants.buttonLightest);
                graphics.drawText(text, circle.getRight().getTranslated(GAP, -textSize.height / 2));
                graphics.translate(-1, -1);
                graphics.setForegroundColor(ColorConstants.buttonDarker);
            }

            graphics.drawText(text, circle.getRight().getTranslated(GAP, -textSize.height / 2));
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        public void setText(String text) {
            this.text = text;
            repaint();
        }
    }
}
