/********************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others
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
import java.util.List;

import org.csstudio.swt.widgets.datadefinition.IManualValueChangeListener;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.ui.util.CSSSchemeBorder;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Cursor;

/**
 * The figure for array widget.
 */
public class ArrayFigure extends Figure implements Introspectable {

    class ArrayLayout extends XYLayout {

        public ArrayLayout() {

        }

        @Override
        public void layout(IFigure parent) {
            List<?> children = parent.getChildren();
            var numChildren = children.size();
            if (numChildren == 0) {
                return;
            }
            var clientArea = parent.getClientArea();
            var eSize = ((Rectangle) getConstraint((IFigure) children.get(0))).getSize();
            IFigure child;
            for (var i = 0; i < numChildren; i++) {
                child = (IFigure) children.get(i);

                if (horizontal) {
                    child.setBounds(new Rectangle(clientArea.x, clientArea.y, eSize.width, eSize.height));
                    clientArea.x += eSize.width;
                } else {
                    child.setBounds(new Rectangle(clientArea.x, clientArea.y, eSize.width, eSize.height));
                    clientArea.y += eSize.height;
                }
            }
        }
    }

    class ArrayPane extends Figure {
        @Override
        protected void paintClientArea(Graphics graphics) {
            super.paintClientArea(graphics);
            var elementsCount = getChildren().size();
            if (elementsCount <= 0) {
                return;
            }
            var grayElementsCount = getIndex() + elementsCount - getArrayLength();
            if (grayElementsCount > 0) {
                var clientArea = getClientArea();
                graphics.pushState();
                if (useLocalCoordinates()) {
                    graphics.translate(getBounds().x + getInsets().left, getBounds().y + getInsets().top);
                }
                graphics.setAlpha(40);
                graphics.setBackgroundColor(ColorConstants.darkGray);
                var child = (IFigure) getChildren().get(0);
                if (horizontal) {
                    var visibleWidth = (elementsCount - grayElementsCount) * child.getSize().width;
                    graphics.fillRectangle(clientArea.x + visibleWidth, clientArea.y, clientArea.width - visibleWidth,
                            clientArea.height);
                } else {
                    var visibleHeight = (elementsCount - grayElementsCount) * child.getSize().height;
                    graphics.fillRectangle(clientArea.x, clientArea.y + visibleHeight, clientArea.width,
                            clientArea.height - visibleHeight);
                }
                graphics.popState();
                updateElementsEnability();
            }
        }

        @Override
        public void add(IFigure figure, Object constraint, int index) {
            super.add(figure, constraint, index);
            scrollbar.setExtent(getVisibleElementsCount());
            scrollbar.setPageIncrement(getVisibleElementsCount());
            enabilityDirty = true;
            if (!isEnabled()) {
                figure.setEnabled(false);
            }
        }

        @Override
        public void remove(IFigure figure) {
            super.remove(figure);
            scrollbar.setExtent(getVisibleElementsCount());
            scrollbar.setPageIncrement(getVisibleElementsCount());
            enabilityDirty = true;
        }

        @Override
        protected boolean useLocalCoordinates() {
            return true;
        }
    }

    private static final int SCROLLBAR_WIDTH = 16;

    private static final int SPINNER_HEIGHT = 25;

    private boolean showSpinner;

    private boolean showScrollbar;

    private boolean horizontal;

    private ScrollbarFigure scrollbar;

    private SpinnerFigure spinner;

    private Figure pane;

    private int index;

    private int arrayLength;

    private int spinnerWidth = 45;

    private boolean enabilityDirty;
    private ListenerList listeners;

    public ArrayFigure() {
        listeners = new ListenerList();
        pane = new ArrayPane();
        pane.setOpaque(false);
        pane.setLayoutManager(new ArrayLayout());
        spinner = new SpinnerFigure();
        spinner.setMin(0);
        spinner.setArrowButtonsOnLeft(true);
        spinner.setStepIncrement(1);
        spinner.setBackgroundColor(ColorConstants.white);
        spinner.getLabelFigure().setOpaque(true);
        spinner.setButtonWidth(16);
        spinner.getLabelFigure().setHorizontalAlignment(H_ALIGN.CENTER);
        spinner.getLabelFigure().setVerticalAlignment(V_ALIGN.MIDDLE);
        scrollbar = new ScrollbarFigure();
        scrollbar.setMinimum(0);
        scrollbar.setStepIncrement(1);
        scrollbar.setShowValueTip(false);
        Border loweredBorder = new SchemeBorder(CSSSchemeBorder.SCHEMES.LOWERED);
        pane.setBorder(loweredBorder);
        spinner.setBorder(loweredBorder);
        add(spinner);
        add(pane);
        add(scrollbar);

        setArrayLength(100);
        setHorizontal(false);
        spinner.addManualValueChangeListener(new IManualValueChangeListener() {

            @Override
            public void manualValueChanged(double newValue) {
                setIndex((int) newValue);
                // scrollbar.setValue((int) newValue);
            }
        });

        scrollbar.addManualValueChangeListener(new IManualValueChangeListener() {

            @Override
            public void manualValueChanged(double newValue) {
                setIndex((int) newValue);

                // spinner.setValue((int) newValue);
            }
        });

    }

    public void addIndexChangeListener(IManualValueChangeListener listener) {
        listeners.add(listener);
    }

    protected void fireIndexChanged(int newIndex) {
        for (Object listener : listeners.getListeners()) {
            ((IManualValueChangeListener) listener).manualValueChanged(newIndex);
        }
    }

    public int getArrayLength() {
        return arrayLength;
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    /**
     * @return the content pane to hold array element widgets.
     */
    public IFigure getContentPane() {
        return pane;
    }

    public int getIndex() {
        return index;
    }

    public SpinnerFigure getSpinner() {
        return spinner;
    }

    public int getSpinnerWidth() {
        return spinnerWidth;
    }

    /**
     * Calculate the number of visible elements that the array pane can hold. This is a round number since the array
     * pane will not fit an integer number of elements. The client should call
     * {@link #calcWidgetSizeForElements(int, Dimension)} to get the proposed size this number of elements.
     *
     * @param elementSize
     *            size of element.
     * @return
     */
    public int calcVisibleElementsCount(Dimension elementSize) {
        var clientArea = pane.getClientArea();
        int r;
        if (horizontal) {
            r = Math.round(clientArea.width / elementSize.width);

        } else {
            r = Math.round(clientArea.height / elementSize.height);
        }
        if (r < 1) {
            r = 1;
        }
        return r;
    }

    /**
     * Calculate the proposed widget size for the proposed visible elements count.
     *
     * @param visibleElementsCount
     *            number of visible elements.
     * @param elementSize
     *            size of element.
     * @return
     */
    public Dimension calcWidgetSizeForElements(int visibleElementsCount, Dimension elementSize) {
        int delta1 = 0, delta2 = 0;
        var clientArea = pane.getClientArea();
        var r = getSize();
        if (horizontal) {
            delta1 = elementSize.width * visibleElementsCount - clientArea.width;
            r.width += delta1;
            delta2 = elementSize.height - clientArea.height;
            r.height += delta2;

        } else {
            delta1 = elementSize.height * visibleElementsCount - clientArea.height;
            r.height += delta1;
            delta2 = elementSize.width - clientArea.width;
            r.width += delta2;
        }
        return r;
    }

    /**
     * @return each element widget's size. If no children, return null.
     */
    public Dimension getElementSize() {
        if (pane.getChildren().isEmpty()) {
            return null;
        }
        return ((IFigure) pane.getChildren().get(0)).getSize();
    }

    public int getVisibleElementsCount() {
        return pane.getChildren().size();
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isShowIndexSpinner() {
        return showSpinner;
    }

    public boolean isShowScrollbar() {
        return showScrollbar;
    }

    /**
     * Calculate the preferred size based current layout and elements' size.
     *
     * @see org.eclipse.draw2d.XYLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure, int, int)
     */
    protected Dimension calculatePreferredSize() {
        var result = new Dimension();
        var elementSize = getElementSize();
        if (elementSize == null) {
            return getSize();
        }
        if (horizontal) {
            result.height = elementSize.height + (scrollbar.isVisible() ? SCROLLBAR_WIDTH : 0)
                    + pane.getInsets().getHeight();
            if (spinner.isVisible() && result.height < SPINNER_HEIGHT) {
                result.height = SPINNER_HEIGHT;
            }
            result.width = elementSize.width * getVisibleElementsCount() + pane.getInsets().getWidth()
                    + (spinner.isVisible() ? spinnerWidth : 0);
        } else {
            result.width = elementSize.width + pane.getInsets().getWidth()
                    + (scrollbar.isVisible() ? SCROLLBAR_WIDTH : 0) + (spinner.isVisible() ? spinnerWidth : 0);
            result.height = elementSize.height * getVisibleElementsCount() + pane.getInsets().getHeight();
        }

        result.width += getInsets().getWidth();
        result.height += getInsets().getHeight();

        return result;
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        setPreferredSize(calculatePreferredSize());
        return super.getPreferredSize(wHint, hHint);
    }

    @Override
    protected void layout() {
        super.layout();
        var clientArea = getClientArea();
        if (spinner.isVisible()) {
            spinner.setBounds(new Rectangle(clientArea.x, clientArea.y, spinnerWidth - 1, SPINNER_HEIGHT));
            clientArea.x += spinnerWidth;
            clientArea.width -= spinnerWidth;
        }
        if (horizontal) {
            if (scrollbar.isVisible()) {
                scrollbar.setBounds(new Rectangle(clientArea.x, clientArea.y + clientArea.height - SCROLLBAR_WIDTH,
                        clientArea.width, SCROLLBAR_WIDTH));
                clientArea.height -= SCROLLBAR_WIDTH;
            }
        } else {
            if (scrollbar.isVisible()) {
                scrollbar.setBounds(new Rectangle(clientArea.x + clientArea.width - SCROLLBAR_WIDTH, clientArea.y,
                        SCROLLBAR_WIDTH, clientArea.height));
                clientArea.width -= SCROLLBAR_WIDTH;
            }
        }
        pane.setBounds(clientArea);
    }

    public void setArrayLength(int arrayLength) {
        this.arrayLength = arrayLength;
        scrollbar.setMaximum(arrayLength - 1);
        spinner.setMax(arrayLength - 1);
        if (arrayLength > 0 && getIndex() >= arrayLength) {
            setIndex(0);
        }
        enabilityDirty = true;
        updateElementsEnability();
    }

    @Override
    public void setCursor(Cursor cursor) {
        pane.setCursor(cursor);
    }

    @Override
    public void setEnabled(boolean value) {
        pane.setEnabled(value);
        enabilityDirty = true;
        updateElementsEnability();

    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        scrollbar.setHorizontal(horizontal);
        revalidate();
    }

    public void setIndex(int index) {
        if (index > getArrayLength() - 1 || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.index = index;
        spinner.setValue(index);
        scrollbar.setValue(index);
        enabilityDirty = true;
        fireIndexChanged(index);
        updateElementsEnability();
        repaint();
    }

    protected void updateElementsEnability() {
        if (!enabilityDirty) {
            return;
        }
        enabilityDirty = false;
        if (!pane.isEnabled()) {
            for (Object child : pane.getChildren()) {
                ((IFigure) child).setEnabled(false);
            }
            return;
        }

        var elementsCount = pane.getChildren().size();
        if (elementsCount <= 0) {
            return;
        }
        var grayElementsCount = this.index + elementsCount - getArrayLength();

        for (var i = 0; i < elementsCount; i++) {
            var child = (IFigure) pane.getChildren().get(i);
            child.setEnabled(i < elementsCount - grayElementsCount);
        }
    }

    public void setShowSpinner(boolean showSpinner) {
        this.showSpinner = showSpinner;
        spinner.setVisible(showSpinner);
        revalidate();
    }

    public void setShowScrollbar(boolean showScrollbar) {
        this.showScrollbar = showScrollbar;
        scrollbar.setVisible(showScrollbar);
        revalidate();
    }

    public void setSpinnerWidth(int spinnerWidth) {
        this.spinnerWidth = spinnerWidth;
        revalidate();
    }

}
