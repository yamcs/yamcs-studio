/*******************************************************************************
 * Copyright (c) 2025 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.figures;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * Figure for the combo widget.
 */
public class ComboFigure extends Figure implements ITextFigure {

    private static final Image downArrow = CustomMediaFactory.getInstance().getImageFromPlugin(
            OPIBuilderPlugin.PLUGIN_ID,
            "icons/downArrow.png");

    private Label label;
    private ImageFigure icon;

    public ComboFigure() {
        super();

        setLayoutManager(new CustomComboLayout());

        label = new Label();
        label.setTextAlignment(PositionConstants.LEFT);

        icon = new ImageFigure(downArrow);

        add(label);
        add(icon);

        setOpaque(true);
    }

    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
        invalidateTree();
    }

    @Override
    public String getText() {
        return label.getText();
    }

    public void setText(String s) {
        label.setText(s);
        invalidateTree();
    }

    private static class CustomComboLayout extends AbstractLayout {
        @Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            var textFigure = (IFigure) container.getChildren().get(0);
            var iconFigure = (IFigure) container.getChildren().get(1);
            var textSize = textFigure.getPreferredSize(wHint, hHint);
            var iconSize = iconFigure.getPreferredSize(wHint, hHint);
            // Sum widths and use maximum height
            return new Dimension(textSize.width + iconSize.width,
                    Math.max(textSize.height, iconSize.height));
        }

        @Override
        public void layout(IFigure container) {
            var bounds = container.getClientArea();
            var textFigure = (IFigure) container.getChildren().get(0);
            var iconFigure = (IFigure) container.getChildren().get(1);

            // Place text at the left
            var textSize = textFigure.getPreferredSize();
            textFigure.setBounds(new Rectangle(
                    bounds.x + 5,
                    bounds.y + (bounds.height - textSize.height) / 2,
                    textSize.width,
                    textSize.height));

            // Place icon at the far right
            var iconSize = iconFigure.getPreferredSize();
            iconFigure.setBounds(new Rectangle(
                    bounds.x + bounds.width - iconSize.width,
                    bounds.y + (bounds.height - iconSize.height) / 2,
                    iconSize.width,
                    iconSize.height));
        }
    }
}
