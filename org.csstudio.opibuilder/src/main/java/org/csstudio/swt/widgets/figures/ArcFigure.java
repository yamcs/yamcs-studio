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

import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.swt.widgets.introspection.ShapeWidgetIntrospector;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;

/**
 * The arc figure
 */
public class ArcFigure extends Shape implements Introspectable {

    // private boolean cordFill = false;
    private int startAngle = 0;
    private int totalAngle = 90;
    private boolean fill = false;

    /**
     * @return the startAngle
     */
    public int getStartAngle() {
        return startAngle;
    }

    /**
     * @return the totalAngle
     */
    public int getTotalAngle() {
        return totalAngle;
    }

    @Override
    protected void fillShape(Graphics graphics) {
        graphics.fillArc(getClientArea().getCopy().shrink((int) (getLineWidth() * 1.5), (int) (getLineWidth() * 1.5)),
                startAngle, totalAngle);
    }

    @Override
    protected void outlineShape(Graphics graphics) {
        graphics.drawArc(getClientArea().getCopy().shrink(getLineWidth(), getLineWidth()), startAngle, totalAngle);
    }

    public void setStartAngle(int start_angle) {
        if (startAngle == start_angle) {
            return;
        }
        startAngle = start_angle;
        repaint();
    }

    public void setTotalAngle(int total_angle) {
        if (totalAngle == total_angle) {
            return;
        }
        totalAngle = total_angle;
        repaint();
    }

    public boolean isFill() {
        return fill;
    }

    @Override
    public void setFill(boolean b) {
        fill = b;
        super.setFill(b);
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new ShapeWidgetIntrospector().getBeanInfo(this.getClass());
    }
}
