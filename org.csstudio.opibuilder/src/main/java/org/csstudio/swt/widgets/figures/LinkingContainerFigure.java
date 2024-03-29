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
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.swt.widgets.Display;

/**
 * The figure of linking container, which can host children widgets from another OPI file.
 */
public class LinkingContainerFigure extends Figure implements Introspectable {

    private ScalableFreeformLayeredPane pane;

    private ScrollPane scrollPane;

    private ZoomManager zoomManager;

    private boolean zoomToFitAll;

    @SuppressWarnings("deprecation")
    public LinkingContainerFigure() {
        scrollPane = new ScrollPane();
        pane = new ScalableFreeformLayeredPane();
        pane.setLayoutManager(new FreeformLayout());
        setLayoutManager(new StackLayout());
        add(scrollPane);
        var viewPort = new FreeformViewport();
        scrollPane.setViewport(viewPort);
        scrollPane.setContents(pane);

        zoomManager = new ZoomManager(pane, viewPort) {
            @Override
            protected double getFitPageZoomLevel() {
                var fitPageZoomLevel = super.getFitPageZoomLevel();
                if (fitPageZoomLevel <= 0) {
                    fitPageZoomLevel = 0.1;
                }
                return fitPageZoomLevel;

            }
        };

        addFigureListener(source -> Display.getDefault().asyncExec(this::updateZoom));

        updateZoom();
    }

    public IFigure getContentPane() {
        return pane;
    }

    public boolean isZoomToFitAll() {
        return zoomToFitAll;
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        Display.getDefault().asyncExec(this::updateZoom);
    }

    public void setZoomToFitAll(boolean zoomToFitAll) {
        this.zoomToFitAll = zoomToFitAll;
        Display.getDefault().asyncExec(this::updateZoom);
    }

    /**
     * Refreshes the zoom.
     */
    public void updateZoom() {

        if (zoomToFitAll) {
            zoomManager.setZoomAsText(Draw2dSingletonUtil.ZoomManager_FIT_ALL);
        } else {
            zoomManager.setZoom(1.0);
        }
    }

    public ZoomManager getZoomManager() {
        return zoomManager;
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    public void setShowScrollBars(boolean showScrollBars) {
        if (showScrollBars) {
            scrollPane.setHorizontalScrollBarVisibility(ScrollPane.AUTOMATIC);
            scrollPane.setVerticalScrollBarVisibility(ScrollPane.AUTOMATIC);
        } else {
            scrollPane.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
            scrollPane.setVerticalScrollBarVisibility(ScrollPane.NEVER);
        }
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }
}
