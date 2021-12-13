/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_BACKGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_FOREGROUND;
import static org.csstudio.opibuilder.model.DisplayModel.PROP_GRID_SPACE;
import static org.csstudio.opibuilder.model.DisplayModel.PROP_SHOW_EDIT_RANGE;
import static org.csstudio.opibuilder.model.DisplayModel.PROP_SHOW_GRID;
import static org.csstudio.opibuilder.model.DisplayModel.PROP_SHOW_RULER;
import static org.csstudio.opibuilder.model.DisplayModel.PROP_SNAP_GEOMETRY;
import static org.eclipse.gef.SnapToGeometry.PROPERTY_SNAP_ENABLED;
import static org.eclipse.gef.SnapToGrid.PROPERTY_GRID_ENABLED;
import static org.eclipse.gef.SnapToGrid.PROPERTY_GRID_SPACING;
import static org.eclipse.gef.SnapToGrid.PROPERTY_GRID_VISIBLE;
import static org.eclipse.gef.rulers.RulerProvider.PROPERTY_RULER_VISIBILITY;

import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Canvas;

/**
 * The editpart for the root display.
 */
public class DisplayEditpart extends AbstractContainerEditpart {

    private ControlListener zoomListener, scaleListener;

    private org.eclipse.swt.graphics.Point originSize, oldSize;

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();

        // disallows the removal of this edit part from its parent
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
    }

    @Override
    public void activate() {
        super.activate();
        initProperties();

        if (getExecutionMode() == ExecutionMode.RUN_MODE
                && getWidgetModel().getDisplayScaleData().isAutoScaleWidgets()) {
            originSize = new org.eclipse.swt.graphics.Point(getWidgetModel().getWidth(), getWidgetModel().getHeight());
            oldSize = originSize;
            scaleListener = new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    if (getViewer() == null || getViewer().getControl().isDisposed()) {
                        return;
                    }
                    var size = getViewer().getControl().getSize();
                    if (size.equals(oldSize)) {
                        return;
                    }
                    var widthRatio = size.x / (double) originSize.x;
                    var heightRatio = size.y / (double) originSize.y;
                    oldSize = size;
                    getWidgetModel().scale(widthRatio, heightRatio);
                    // oldSize = size;
                }
            };
            UIBundlingThread.getInstance().addRunnable(() -> scaleListener.controlResized(null));
            getViewer().getControl().addControlListener(scaleListener);
        }

        if (getExecutionMode() == ExecutionMode.RUN_MODE && getWidgetModel().isAutoZoomToFitAll()) {
            originSize = new org.eclipse.swt.graphics.Point(getWidgetModel().getWidth(), getWidgetModel().getHeight());
            oldSize = originSize;
            zoomListener = new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    if (!isActive() || getViewer() == null || getViewer().getControl().isDisposed()) {
                        return;
                    }
                    var size = ((Canvas) getViewer().getControl()).getSize();
                    if (size.equals(oldSize)) {
                        return;
                    }
                    if (size.x * size.y > 0) {
                        var displayModel = (DisplayModel) getModel();
                        getZoomManager().getScalableFigure().setPreferredSize(displayModel.getSize());
                        getZoomManager().setZoomAsText(Draw2dSingletonUtil.ZoomManager_FIT_ALL);
                    }
                    oldSize = size;
                }
            };
            UIBundlingThread.getInstance().addRunnable(() -> zoomListener.controlResized(null));
            getViewer().getControl().addControlListener(zoomListener);
        }
        UIBundlingThread.getInstance().addRunnable(() -> {
            if (getViewer() != null && getViewer().getControl() != null && !getViewer().getControl().isDisposed()) {
                getViewer().getControl().forceFocus();
            }
        });
    }

    @Override
    public void deactivate() {
        var control = getViewer().getControl();
        if (getViewer() != null && !control.isDisposed()) {
            // This needs to be executed in UI Thread
            var zoomManager = getZoomManager();
            control.getDisplay().asyncExec(() -> {
                if (zoomListener != null && !control.isDisposed()) {
                    // recover zoom
                    zoomManager.setZoom(1.0);
                    control.removeControlListener(zoomListener);
                }

                if (scaleListener != null && !control.isDisposed()) {
                    control.removeControlListener(scaleListener);
                }
            });
        }
        super.deactivate();
    }

    @Override
    public DisplayModel getWidgetModel() {
        return (DisplayModel) super.getWidgetModel();
    }

    private void initProperties() {
        for (var prop_id : getWidgetModel().getAllPropertyIDs()) {
            getWidgetModel().getProperty(prop_id).firePropertyChange(null, getWidgetModel().getPropertyValue(prop_id));
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_COLOR_BACKGROUND, (oldValue, newValue, figure) -> {
            figure.setBackgroundColor(((OPIColor) newValue).getSWTColor());
            getViewer().getControl()
                    .setBackground(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            return false;
        });

        // grid
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            setPropertyChangeHandler(PROP_COLOR_FOREGROUND, (oldValue, newValue, figure) -> {
                ((ScalableFreeformRootEditPart) getViewer().getRootEditPart()).getLayer(LayerConstants.GRID_LAYER)
                        .setForegroundColor(
                                CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
                return false;
            });

            setPropertyChangeHandler(PROP_GRID_SPACE, (oldValue, newValue, figure) -> {
                getViewer().setProperty(PROPERTY_GRID_SPACING,
                        new Dimension((Integer) newValue, (Integer) newValue));
                return false;
            });

            setPropertyChangeHandler(PROP_SHOW_GRID, (oldValue, newValue, figure) -> {
                getViewer().setProperty(PROPERTY_GRID_VISIBLE, newValue);
                getViewer().setProperty(PROPERTY_GRID_ENABLED, newValue);
                return false;
            });

            setPropertyChangeHandler(PROP_SHOW_RULER, (oldValue, newValue, figure) -> {
                getViewer().setProperty(PROPERTY_RULER_VISIBILITY, newValue);
                return false;
            });

            setPropertyChangeHandler(PROP_SNAP_GEOMETRY, (oldValue, newValue, figure) -> {
                getViewer().setProperty(PROPERTY_SNAP_ENABLED, newValue);
                return false;
            });

            setPropertyChangeHandler(PROP_SHOW_EDIT_RANGE, (oldValue, newValue, figure) -> {
                figure.repaint();
                return true;
            });
        }
    }

    @Override
    protected IFigure doCreateFigure() {
        var f = new FreeformLayer() {
            @Override
            protected void paintFigure(Graphics graphics) {
                super.paintFigure(graphics);
                if (getExecutionMode() == ExecutionMode.EDIT_MODE && ((DisplayModel) getModel()).isShowEditRange()) {
                    graphics.pushState();
                    graphics.setLineStyle(SWT.LINE_DASH);
                    graphics.setForegroundColor(ColorConstants.black);
                    graphics.drawRectangle(new Rectangle(new Point(0, 0), getWidgetModel().getSize()));
                    graphics.popState();
                }
            }
        };
        // f.setBorder(new MarginBorder(3));
        f.setLayoutManager(new FreeformLayout());

        return f;
    }

    @Override
    protected synchronized void doRefreshVisuals(IFigure refreshableFigure) {
        super.doRefreshVisuals(refreshableFigure);
        figure.repaint();
    }

    @Override
    public EditPart getWidget(String name) throws Exception {
        try {
            return super.getWidget(name);
        } catch (Exception e) {
            // search from connection widgets
            for (var conn : getWidgetModel().getConnectionList()) {
                if (conn.getName().equals(name)) {
                    return (EditPart) getViewer().getEditPartRegistry().get(conn);
                }
            }
            throw e;
        }
    }

    private ZoomManager getZoomManager() {
        var editpart = getRoot();
        if (editpart instanceof ScalableFreeformRootEditPart) {
            return ((ScalableFreeformRootEditPart) editpart).getZoomManager();
        } else if (editpart instanceof ScalableRootEditPart) {
            return ((ScalableRootEditPart) editpart).getZoomManager();
        }
        return null;
    }
}
