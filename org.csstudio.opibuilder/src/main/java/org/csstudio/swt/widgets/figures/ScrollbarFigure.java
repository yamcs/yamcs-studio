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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.widgets.datadefinition.IManualValueChangeListener;
import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.swt.widgets.util.OPITimer;
import org.csstudio.ui.util.CSSButtonBorder;
import org.csstudio.ui.util.CSSSchemeBorder;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.ArrowButton;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.FocusListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Orientable;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.ScrollBarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * ScrollBar Figure which supports double value.
 */
public class ScrollbarFigure extends Figure implements Orientable, Introspectable {

    class ThumbDragger extends MouseMotionListener.Stub implements MouseListener {
        protected Point start;
        protected double dragRange;
        protected double revertValue;
        protected boolean armed;

        public ThumbDragger() {
        }

        @Override
        public void mouseDoubleClicked(MouseEvent me) {
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            if (!armed) {
                return;
            }
            var difference = transposer.t(me.getLocation().getDifference(start));
            var change = (getValueRange() + getExtent()) * difference.height / dragRange;
            manualSetValue(revertValue + change);
            me.consume();
        }

        @Override
        public void mousePressed(MouseEvent me) {
            armed = true;
            start = me.getLocation();
            var area = new Rectangle(transposer.t(getClientArea()));
            var thumbSize = transposer.t(thumb.getSize());
            if (buttonUp != null) {
                area.height -= transposer.t(buttonUp.getSize()).height;
            }
            if (buttonDown != null) {
                area.height -= transposer.t(buttonDown.getSize()).height;
            }
            var sizeDifference = new Dimension(area.width, area.height - thumbSize.height);
            dragRange = sizeDifference.height;
            revertValue = getValue();
            me.consume();
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (!armed) {
                return;
            }
            armed = false;
            me.consume();
        }
    }

    private static final String DEFAULT_ENGINEERING_FORMAT = "0.##E0";

    /** the default label format */
    private static final String DEFAULT_DECIMAL_FORMAT = "############.##";

    /**
     * the digits limit to be displayed in engineering format
     */
    private static final int ENGINEERING_LIMIT = 2;

    private final static Color GRAY_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_GRAY);
    private final static Color LABEL_COLOR = CustomMediaFactory.getInstance().getColor(new RGB(255, 255, 150));
    private boolean horizontal;
    private boolean showValueTip = true;
    private Label label;

    private OPITimer labelTimer;

    private Runnable timerTask;

    private double value = 50;

    private double minimum = 0;

    private double maximum = 100;
    private double extent = 30;

    private double stepIncrement = 1;

    private double pageIncrement = 10;
    private List<IManualValueChangeListener> listeners;
    private IFigure thumb;

    private Clickable pageUp, pageDown;

    private Clickable buttonUp, buttonDown;

    /**
     * Transposes from vertical to horizontal if needed.
     */
    protected final Transposer transposer = new Transposer();

    /**
     * Listens to mouse events on the scrollbar to take care of scrolling.
     */
    protected ThumbDragger thumbDragger = new ThumbDragger();

    private boolean valueIncreased;

    private static final Color COLOR_TRACK = FigureUtilities.mixColors(ColorConstants.white, ColorConstants.button);

    private String formatPattern;

    private DecimalFormat decimalFormat;

    public ScrollbarFigure() {
        decimalFormat = new DecimalFormat(DEFAULT_DECIMAL_FORMAT);
        listeners = new ArrayList<>();

        initializeListeners();
        initializeParts();
    }

    public void addManualValueChangeListener(IManualValueChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Creates the default 'Down' ArrowButton for the ScrollBar.
     */
    protected Clickable createDefaultDownButton() {
        Button buttonDown = new ArrowButton();
        buttonDown.setBorder(new ButtonBorder(CSSButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
        return buttonDown;
    }

    /**
     * Creates the Scrollbar's "thumb", the draggable Figure that indicates the Scrollbar's position.
     */
    protected IFigure createDefaultThumb() {
        var thumb = new Panel();
        thumb.setMinimumSize(new Dimension(6, 6));
        thumb.setBackgroundColor(ColorConstants.button);

        thumb.setBorder(new SchemeBorder(CSSSchemeBorder.SCHEMES.BUTTON_CONTRAST));
        return thumb;
    }

    /**
     * Creates the default 'Up' ArrowButton for the ScrollBar.
     */
    protected Clickable createDefaultUpButton() {
        Button buttonUp = new ArrowButton();
        buttonUp.setBorder(new ButtonBorder(CSSButtonBorder.SCHEMES.BUTTON_SCROLLBAR));
        return buttonUp;
    }

    /**
     * Creates the pagedown Figure for the Scrollbar.
     */
    protected Clickable createPageDown() {
        return createPageUp();
    }

    /**
     * Creates the pageup Figure for the Scrollbar.
     */
    protected Clickable createPageUp() {
        var clickable = new Clickable();
        clickable.setOpaque(true);
        clickable.setBackgroundColor(COLOR_TRACK);
        clickable.addChangeListener(evt -> {
            if (clickable.getModel().isArmed()) {
                clickable.setBackgroundColor(ColorConstants.black);
            } else {
                clickable.setBackgroundColor(COLOR_TRACK);
            }
        });
        return clickable;
    }

    private void fireManualValueChange(double value) {
        for (var listener : listeners) {
            listener.manualValueChanged(value);
        }
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    public double getCoercedValue() {
        return value < minimum ? minimum : (value > maximum ? maximum : value);
    }

    public double getExtent() {
        return extent;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getPageIncrement() {
        return pageIncrement;
    }

    public double getStepIncrement() {
        return stepIncrement;
    }

    public double getValue() {
        return value;
    }

    /**
     * Returns the size of the range of allowable values.
     */
    protected double getValueRange() {
        return getMaximum() - getExtent() - getMinimum();
    }

    private void hookFocusListener(Clickable up) {
        up.addActionListener(event -> {
            if (!hasFocus()) {
                requestFocus();
            }
        });
    }

    private void initializeListeners() {
        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if ((ke.keycode == SWT.ARROW_UP && !isHorizontal())
                        || (ke.keycode == SWT.ARROW_LEFT && isHorizontal())) {
                    stepUp();
                } else if ((ke.keycode == SWT.ARROW_DOWN && !isHorizontal())
                        || (ke.keycode == SWT.ARROW_RIGHT && isHorizontal())) {
                    stepDown();
                } else if ((ke.keycode == SWT.PAGE_DOWN && !isHorizontal())
                        || (ke.keycode == SWT.PAGE_UP && isHorizontal())) {
                    pageDown();
                } else if ((ke.keycode == SWT.PAGE_UP && !isHorizontal())
                        || (ke.keycode == SWT.PAGE_DOWN && isHorizontal())) {
                    pageUp();
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent fe) {
                repaint();
            }
        });
    }

    /**
     * Initilization of the ScrollBar. Sets the Scrollbar to have a ScrollBarLayout with vertical orientation. Creates
     * the Figures that make up the components of the ScrollBar.
     */
    protected void initializeParts() {
        setLayoutManager(new ScrollBarFigureLayout(transposer));
        setUpClickable(createDefaultUpButton());
        setDownClickable(createDefaultDownButton());
        setPageUp(createPageUp());
        setPageDown(createPageDown());
        setThumb(createDefaultThumb());
        label = new Label();
        label.setBackgroundColor(LABEL_COLOR);
        label.setBorder(new LineBorder(GRAY_COLOR));
        label.setVisible(false);
        add(label, "Label");
    }

    private void initLabelTimer() {
        if (labelTimer == null) {
            var display = Display.getCurrent();
            labelTimer = new OPITimer();
            timerTask = () -> display.asyncExec(() -> label.setVisible(false));
        }
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isShowValueTip() {
        return showValueTip;
    }

    @Override
    protected void layout() {
        super.layout();
        if (label.isVisible()) {
            var thumbBounds = thumb.getBounds();
            var size = label.getPreferredSize();
            if (isHorizontal()) {
                label.setBounds(new Rectangle(thumbBounds.x + (valueIncreased ? -size.width : thumbBounds.width),
                        thumbBounds.y, size.width, size.height));
            } else {
                label.setBounds(new Rectangle(thumbBounds.x,
                        thumbBounds.y + (valueIncreased ? -size.height : thumbBounds.height), size.width, size.height));
            }
        }
    }

    /**
     * Set Value from manual control of the widget. Value will be coerced in range.
     */
    public void manualSetValue(double value) {
        value = Math.max(getMinimum(), Math.min(getMaximum(), value));
        if (this.value == value) {
            return;
        }
        if (showValueTip) {
            valueIncreased = value > this.value;
            label.setText("" + decimalFormat.format(value));
            label.setVisible(true);
            initLabelTimer();
            if (!labelTimer.isDue()) {
                labelTimer.reset();
            } else {
                labelTimer.start(timerTask, 1000);
            }
        }
        setValue(value);
        fireManualValueChange(getValue());
    }

    public void pageDown() {
        manualSetValue(getValue() + pageIncrement);
    }

    public void pageUp() {
        manualSetValue(getValue() - pageIncrement);
    }

    @Override
    protected void paintClientArea(Graphics graphics) {
        super.paintClientArea(graphics);
        if (hasFocus()) {
            graphics.setForegroundColor(ColorConstants.black);
            graphics.setBackgroundColor(ColorConstants.white);

            var area = getClientArea();
            graphics.drawFocus(area.x, area.y, area.width - 1, area.height - 1);
        }
    }

    public void removeManualValueChangeListener(IManualValueChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void revalidate() {
        // Override default revalidate to prevent going up the parent chain. Reason for this
        // is that preferred size never changes unless orientation changes.
        invalidate();
        getUpdateManager().addInvalidFigure(this);
    }

    @Override
    public void setDirection(int direction) {
    }

    /**
     * Sets the Clickable that represents the down arrow of the Scrollbar to <i>down</i>.
     */
    public void setDownClickable(Clickable down) {
        hookFocusListener(down);
        if (buttonDown != null) {
            remove(buttonDown);
        }
        buttonDown = down;
        if (buttonDown != null) {
            if (buttonDown instanceof Orientable) {
                ((Orientable) buttonDown).setDirection(isHorizontal() ? Orientable.EAST : Orientable.SOUTH);
            }
            buttonDown.setFiringMethod(Clickable.REPEAT_FIRING);
            buttonDown.addActionListener(e -> stepDown());
            add(buttonDown, ScrollBarLayout.DOWN_ARROW);
        }
    }

    @Override
    public void setEnabled(boolean value) {
        if (isEnabled() == value) {
            return;
        }
        super.setEnabled(value);
        setChildrenEnabled(value);
        if (thumb != null) {
            thumb.setVisible(value);
            revalidate();
        }
    }

    public void setExtent(double extent) {
        if (this.extent == extent) {
            return;
        }
        this.extent = extent;
        revalidate();
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
        decimalFormat = new DecimalFormat(formatPattern);
    }

    /**
     * Sets the orientation of the ScrollBar. If <code>true</code>, the Scrollbar will have a horizontal orientation. If
     * <code>false</code>, the scrollBar will have a vertical orientation.
     */
    public void setHorizontal(boolean value) {
        setOrientation(value ? HORIZONTAL : VERTICAL);
    }

    public void setMaximum(double maximum) {
        if (this.maximum == maximum) {
            return;
        }
        this.maximum = maximum;
        updateFormat();
        revalidate();
    }

    public void setMinimum(double minimum) {
        if (this.minimum == minimum) {
            return;
        }
        this.minimum = minimum;
        updateFormat();
        revalidate();
    }

    @Override
    public void setOrientation(int value) {
        if ((value == HORIZONTAL) == isHorizontal()) {
            return;
        }
        horizontal = value == HORIZONTAL;
        transposer.setEnabled(horizontal);

        setChildrenOrientation(value);
        super.revalidate();
    }

    /**
     * Sets the pagedown button to the passed Clickable. The pagedown button is the figure between the down arrow button
     * and the ScrollBar's thumb figure.
     */
    public void setPageDown(Clickable down) {
        hookFocusListener(down);

        if (pageDown != null) {
            remove(pageDown);
        }
        pageDown = down;
        if (pageDown != null) {
            pageDown.setFiringMethod(Clickable.REPEAT_FIRING);
            pageDown.addActionListener(event -> pageDown());
            add(down, ScrollBarLayout.PAGE_DOWN);
        }
    }

    public void setPageIncrement(double pageIncrement) {
        this.pageIncrement = pageIncrement;
    }

    /**
     * Sets the pageup button to the passed Clickable. The pageup button is the rectangular figure between the down
     * arrow button and the ScrollBar's thumb figure.
     */
    public void setPageUp(Clickable up) {
        hookFocusListener(up);

        if (pageUp != null) {
            remove(pageUp);
        }
        pageUp = up;
        if (pageUp != null) {
            pageUp.setFiringMethod(Clickable.REPEAT_FIRING);
            pageUp.addActionListener(event -> pageUp());
            add(pageUp, ScrollBarLayout.PAGE_UP);
        }
    }

    /**
     * @param showValueTip
     *            the showValueTip to set
     */
    public void setShowValueTip(boolean showValueTip) {
        this.showValueTip = showValueTip;
    }

    /**
     * @param stepIncrement
     *            the stepIncrement to set
     */
    public void setStepIncrement(double stepIncrement) {
        this.stepIncrement = stepIncrement;
    }

    /**
     * Sets the ScrollBar's thumb to the passed Figure. The thumb is the draggable component of the ScrollBar that
     * indicates the ScrollBar's position.
     */
    public void setThumb(IFigure figure) {
        if (figure != null) {
            figure.addMouseListener(new MouseListener.Stub() {
                @Override
                public void mousePressed(MouseEvent me) {
                    if (!hasFocus()) {
                        requestFocus();
                    }
                }
            });
        }
        if (thumb != null) {
            thumb.removeMouseListener(thumbDragger);
            thumb.removeMouseMotionListener(thumbDragger);
            remove(thumb);
        }
        thumb = figure;
        if (thumb != null) {
            thumb.addMouseListener(thumbDragger);
            thumb.addMouseMotionListener(thumbDragger);
            add(thumb, ScrollBarLayout.THUMB);
        }
    }

    /**
     * Sets the Clickable that represents the up arrow of the Scrollbar to <i>up</i>.
     */
    public void setUpClickable(Clickable up) {
        hookFocusListener(up);

        if (buttonUp != null) {
            remove(buttonUp);
        }
        buttonUp = up;
        if (up != null) {
            if (up instanceof Orientable) {
                ((Orientable) up).setDirection(isHorizontal() ? Orientable.WEST : Orientable.NORTH);
            }
            buttonUp.setFiringMethod(Clickable.REPEAT_FIRING);
            buttonUp.addActionListener(e -> stepUp());
            add(buttonUp, ScrollBarLayout.UP_ARROW);
        }
    }

    public void setValue(double value) {
        if (this.value == value) {
            return;
        }
        this.value = value;
        revalidate();
        repaint();
    }

    public void stepDown() {
        manualSetValue(getValue() + stepIncrement);
    }

    public void stepUp() {
        manualSetValue(getValue() - stepIncrement);
    }

    private void updateFormat() {
        if (formatPattern != null) {
            return;
        }
        String tempPattern;
        if ((maximum != 0 && Math.abs(Math.log10(Math.abs(maximum))) >= ENGINEERING_LIMIT)
                || (minimum != 0 && Math.abs(Math.log10(Math.abs(minimum))) >= ENGINEERING_LIMIT)) {
            tempPattern = DEFAULT_ENGINEERING_FORMAT;
        } else {
            tempPattern = DEFAULT_DECIMAL_FORMAT;
        }
        decimalFormat = new DecimalFormat(tempPattern);
    }

    @Override
    public void setFocusTraversable(boolean focusTraversable) {
        super.setFocusTraversable(focusTraversable);
        buttonUp.setFocusTraversable(focusTraversable);
        buttonDown.setFocusTraversable(focusTraversable);
        pageUp.setFocusTraversable(focusTraversable);
        pageDown.setFocusTraversable(focusTraversable);
    }

    @Override
    public void setRequestFocusEnabled(boolean requestFocusEnabled) {
        super.setRequestFocusEnabled(requestFocusEnabled);
        buttonUp.setRequestFocusEnabled(requestFocusEnabled);
        buttonDown.setRequestFocusEnabled(requestFocusEnabled);
        pageUp.setRequestFocusEnabled(requestFocusEnabled);
        pageDown.setRequestFocusEnabled(requestFocusEnabled);
    }
}
