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
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.graphics.Font;

/**
 * A text figure which is able to wrap text automatically and show scrollbar if necessary. *
 */
public class WrappableTextFigure extends TextFigure {

    /** The inner TextFlow **/
    private TextFlow textFlow;
    private FlowPage flowPage;
    private Viewport viewport;
    private ScrollPane scrollPane;
    private boolean transparent;
    private boolean oldSelectable = true;

    public WrappableTextFigure() {
        this(false);
    }

    /**
     * Creates a text figure with a flowPage containing a TextFlow with the style WORD_WRAP_SOFT.
     *
     * @param borderSize
     *            the size of the MarginBorder
     */
    public WrappableTextFigure(boolean runMode) {
        super(runMode);
        flowPage = new FlowPage();
        textFlow = new TextFlow("") {
            @Override
            public void setFont(Font f) {
                super.setFont(f);
                revalidateBidi(this);
                repaint();
            }
        };
        textFlow.setLayoutManager(new ParagraphTextLayout(textFlow, ParagraphTextLayout.WORD_WRAP_SOFT));
        flowPage.add(textFlow);

        scrollPane = new ScrollPane() {
            @Override
            public boolean isOpaque() {
                return !transparent;
            }
        };
        viewport = new Viewport();
        viewport.setContentsTracksWidth(true);
        scrollPane.setViewport(viewport);
        scrollPane.setContents(flowPage);
        scrollPane.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
        add(scrollPane);
    }

    @Override
    public void setHorizontalAlignment(H_ALIGN hAlignment) {
        super.setHorizontalAlignment(hAlignment);
        if (hAlignment == H_ALIGN.CENTER) {
            flowPage.setHorizontalAligment(PositionConstants.CENTER);
            super.setHorizontalAlignment(H_ALIGN.LEFT);
        } else if (hAlignment == H_ALIGN.LEFT) {
            flowPage.setHorizontalAligment(PositionConstants.LEFT);
        } else if (hAlignment == H_ALIGN.RIGHT) {
            flowPage.setHorizontalAligment(PositionConstants.RIGHT);
        }
    }

    @Override
    public void setSelectable(boolean selectable) {
        super.setSelectable(selectable);
        oldSelectable = isSelectable();
    }

    @Override
    public boolean containsPoint(int x, int y) {
        if (scrollPane.getHorizontalScrollBar().isVisible() || scrollPane.getVerticalScrollBar().isVisible()) {
            oldSelectable = selectable;
            selectable = true;
        } else {
            selectable = oldSelectable;
        }
        return super.containsPoint(x, y);
    }

    @Override
    public Dimension getAutoSizeDimension() {
        return flowPage.getPreferredSize().getCopy().expand(getInsets().getWidth(), getInsets().getHeight());
    }

    /**
     * Returns the text inside the TextFlow.
     *
     * @return the text flow inside the text.
     */
    @Override
    public String getText() {
        return textFlow.getText();
    }

    @Override
    protected void layout() {
        var clientArea = getClientArea();
        var preferedSize = scrollPane.getPreferredSize();
        var x = clientArea.x;
        if (clientArea.width > preferedSize.width) {
            switch (horizontalAlignment) {
            case CENTER:
                x = clientArea.x + (clientArea.width - preferedSize.width) / 2;
                break;
            case RIGHT:
                x = clientArea.x + clientArea.width - preferedSize.width;
                break;
            case LEFT:
            default:
                x = clientArea.x;
                break;
            }
        } else {
            preferedSize = scrollPane.getPreferredSize(clientArea.width, -1);
        }
        var y = clientArea.y;
        if (clientArea.height > preferedSize.height) {
            switch (verticalAlignment) {
            case MIDDLE:
                y = clientArea.y + (clientArea.height - preferedSize.height) / 2;
                break;
            case BOTTOM:
                y = clientArea.y + clientArea.height - preferedSize.height;
                break;
            case TOP:
            default:
                y = clientArea.y;
                break;
            }
        }
        var textBounds = new Rectangle(x, y, clientArea.width - (x - clientArea.x),
                clientArea.height - (y - clientArea.y));
        // textFlow.setMinimumSize(textBounds.getSize());
        // textFlow.setPreferredSize(textBounds.getSize());

        scrollPane.setBounds(textBounds);
    }

    @Override
    public void setFont(Font f) {
        flowPage.setFont(f);
        textFlow.setFont(f);
        super.setFont(f);
        revalidate();
    }

    @Override
    public void setOpaque(boolean opaque) {
        transparent = !opaque;
        scrollPane.setOpaque(opaque);
        textFlow.setOpaque(opaque);
        super.setOpaque(opaque);
        repaint();
    }

    public void setWrapWords(boolean wrapWords) {
        if (wrapWords) {
            viewport.setContentsTracksWidth(true);
            scrollPane.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
        } else {
            viewport.setContentsTracksWidth(false);
            scrollPane.setHorizontalScrollBarVisibility(ScrollPane.AUTOMATIC);
        }
        viewport.revalidate();
        revalidate();
    }

    public void setShowScrollbar(boolean showScrollbar) {
        scrollPane.setScrollBarVisibility(showScrollbar ? ScrollPane.AUTOMATIC : ScrollPane.NEVER);
        revalidate();
    }

    /**
     * Sets the text of the TextFlow to the given value.
     *
     * @param newText
     *            the new text value.
     */
    @Override
    public void setText(String newText) {
        textFlow.setText(newText);
        revalidate();
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }
}
