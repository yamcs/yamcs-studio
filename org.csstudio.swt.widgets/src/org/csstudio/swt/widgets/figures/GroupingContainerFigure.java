/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
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
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.StackLayout;

/**
 * The figure of grouping container, which can host children widgets.
 */
public class GroupingContainerFigure extends Figure implements Introspectable {

    private IFigure pane;

    private boolean transparent = false;

    private ScrollPane scrollPane;

    private boolean showScrollbar = false;

    public GroupingContainerFigure() {
        scrollPane = new ScrollPane() {
            @Override
            public boolean isOpaque() {
                return !transparent;
            }
        };
        pane = new FreeformLayer();
        pane.setLayoutManager(new FreeformLayout());
        setLayoutManager(new StackLayout());
        add(scrollPane);
        scrollPane.setViewport(new FreeformViewport());
        scrollPane.setContents(pane);
        setShowScrollBar(true);
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    public IFigure getContentPane() {
        return pane;
    }

    public boolean isShowScrollBar() {
        return showScrollbar;
    }

    @Override
    public void setOpaque(boolean opaque) {
        transparent = !opaque;
        pane.setOpaque(opaque);
        super.setOpaque(opaque);
    }

    public void setShowScrollBar(boolean show) {
        if (this.showScrollbar == show) {
            return;
        }
        this.showScrollbar = show;
        scrollPane.setScrollBarVisibility(show ? ScrollPane.AUTOMATIC : ScrollPane.NEVER);
    }

}
