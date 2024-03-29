/********************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figures;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;

/**
 * Lays out the Figures that make up a ScrollBar.
 */
public class ScrollBarFigureLayout extends AbstractLayout {

    /** Used as a constraint for the up arrow figure. */
    public static final String UP_ARROW = "up arrow";
    /** Used as a constraint for the down arrow figure. */
    public static final String DOWN_ARROW = "down arrow";
    /** Used as a constraint for the thumb figure. */
    public static final String THUMB = "thumb";
    /** Used as a constraint for the page up figure. */
    public static final String PAGE_UP = "page_up";
    /** Used as a constraint for the page down figure. */
    public static final String PAGE_DOWN = "page_down";

    IFigure up, down, thumb, pageUp, pageDown;

    /**
     * Transposes values if the ScrollBar is horizontally oriented. When used properly, the layout manager just needs to
     * code for one case: vertical orientation.
     */
    protected final Transposer transposer;

    /**
     * Constructs a ScrollBarLayout. If the given Transposer is enabled, the Scrollbar will be horizontally oriented.
     * Otherwise, the ScrollBar will be vertically oriented.
     */
    public ScrollBarFigureLayout(Transposer t) {
        transposer = t;
    }

    @Override
    public void setConstraint(IFigure figure, Object constraint) {
        if (constraint.equals(UP_ARROW)) {
            up = figure;
        } else if (constraint.equals(DOWN_ARROW)) {
            down = figure;
        } else if (constraint.equals(THUMB)) {
            thumb = figure;
        } else if (constraint.equals(PAGE_UP)) {
            pageUp = figure;
        } else if (constraint.equals(PAGE_DOWN)) {
            pageDown = figure;
        }
    }

    @Override
    protected Dimension calculatePreferredSize(IFigure parent, int w, int h) {
        var insets = transposer.t(parent.getInsets());
        var d = new Dimension(16, 16 * 4);
        d.expand(insets.getWidth(), insets.getHeight());
        return transposer.t(d);
    }

    @Override
    public void layout(IFigure parent) {
        var scrollBar = (ScrollbarFigure) parent;

        var trackBounds = layoutButtons(scrollBar);

        var extent = scrollBar.getExtent();
        var max = scrollBar.getMaximum() + extent;
        var min = scrollBar.getMinimum();
        var totalRange = max - min;
        var valueRange = totalRange - extent;
        if (!scrollBar.isEnabled() || Double.isNaN(min) || Double.isNaN(max)) {
            Rectangle boundsUpper = new Rectangle(trackBounds), boundsLower = new Rectangle(trackBounds);
            boundsUpper.height /= 2;
            boundsLower.y += boundsUpper.height;
            boundsLower.height = trackBounds.height - boundsUpper.height;
            if (pageUp != null) {
                pageUp.setBounds(transposer.t(boundsUpper));
            }
            if (pageDown != null) {
                pageDown.setBounds(transposer.t(boundsLower));
            }
            return;
        }

        if (totalRange == 0) {
            return;
        }
        var thumbHeight = (int) Math.max(thumb == null ? 0 : thumb.getMinimumSize().height,
                trackBounds.height * extent / totalRange);

        if (thumb != null) {
            thumb.setVisible(trackBounds.height > thumbHeight);
        }
        int thumbY;
        if (valueRange <= 0) {
            thumbHeight = 0;
            thumb.setVisible(false);
            thumbY = trackBounds.y;
        } else {
            thumbY = (int) (trackBounds.y
                    + (trackBounds.height - thumbHeight) * (scrollBar.getCoercedValue() - min) / valueRange);
        }

        var thumbBounds = new Rectangle(trackBounds.x, thumbY, trackBounds.width, thumbHeight);

        if (thumb != null && thumb.isVisible()) {
            thumb.setBounds(transposer.t(thumbBounds));
        }

        if (pageUp != null) {
            pageUp.setBounds(transposer
                    .t(new Rectangle(trackBounds.x, trackBounds.y, trackBounds.width, thumbBounds.y - trackBounds.y)));
        }

        if (pageDown != null) {
            pageDown.setBounds(transposer.t(new Rectangle(trackBounds.x, thumbBounds.y + thumbHeight, trackBounds.width,
                    trackBounds.bottom() - thumbBounds.bottom())));
        }
    }

    /**
     * Places the buttons and returns the Rectangle into which the track should be placed. The track consists of the
     * pageup, pagedown, and thumb figures. The Rectangle returned should be transposed correctly, that is, it should be
     * vertically oriented. Users of the rectangle will re-transpose it for horizontal use.
     *
     * @param scrollBar
     *            the scrollbar whose buttons are being layed out
     * @return the Rectangle into which the track should be placed
     */
    protected Rectangle layoutButtons(ScrollbarFigure scrollBar) {
        var bounds = transposer.t(scrollBar.getClientArea());
        var buttonSize = new Dimension(bounds.width, Math.min(bounds.width, bounds.height / 2));

        if (up != null) {
            up.setBounds(transposer.t(new Rectangle(bounds.getTopLeft(), buttonSize)));
        }
        if (down != null) {
            var r = new Rectangle(bounds.x, bounds.bottom() - buttonSize.height, buttonSize.width, buttonSize.height);
            down.setBounds(transposer.t(r));
        }

        var trackBounds = bounds.getShrinked(
                new Insets((up == null) ? 0 : buttonSize.height, 0, (down == null) ? 0 : buttonSize.height, 0));

        return trackBounds;
    }

    @Override
    public void remove(IFigure child) {
        if (child == up) {
            up = null;
        } else if (child == down) {
            down = null;
        } else if (child == thumb) {
            thumb = null;
        } else if (child == pageUp) {
            pageUp = null;
        } else if (child == pageDown) {
            pageDown = null;
        }
    }
}
