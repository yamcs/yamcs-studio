/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_BACKGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_FOREGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_VISIBLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.widgets.model.GroupingContainerModel.PROP_FORWARD_COLORS;
import static org.csstudio.opibuilder.widgets.model.GroupingContainerModel.PROP_LOCK_CHILDREN;
import static org.csstudio.opibuilder.widgets.model.GroupingContainerModel.PROP_SHOW_SCROLLBAR;
import static org.csstudio.opibuilder.widgets.model.GroupingContainerModel.PROP_TRANSPARENT;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.csstudio.opibuilder.widgets.model.TabModel;
import org.csstudio.swt.widgets.figures.GroupingContainerFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.ui.IActionFilter;

/**
 * The Editpart Controller for a Grouping Container
 */
public class GroupingContainerEditPart extends AbstractContainerEditpart {

    @Override
    protected IFigure doCreateFigure() {
        var f = new GroupingContainerFigure();
        f.setOpaque(!getWidgetModel().isTransparent());
        f.setShowScrollBar(getWidgetModel().isShowScrollbar());
        return f;
    }

    @Override
    public void activate() {
        initFigure(getFigure());
        super.activate();
    }

    @Override
    public GroupingContainerModel getWidgetModel() {
        return (GroupingContainerModel) getModel();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new ContainerHighlightEditPolicy());
    }

    @Override
    public IFigure getContentPane() {
        return ((GroupingContainerFigure) getFigure()).getContentPane();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, figure) -> {
            figure.setOpaque(!(Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, figure) -> {
            for (var child : getWidgetModel().getChildren()) {
                child.setEnabled((Boolean) newValue);
            }
            return true;
        });

        setPropertyChangeHandler(PROP_LOCK_CHILDREN, (oldValue, newValue, figure) -> {
            lockChildren((Boolean) newValue);
            return true;
        });

        lockChildren(getWidgetModel().isLocked());
        if (getWidgetModel().getParent() instanceof TabModel) {
            removeAllPropertyChangeHandlers(PROP_VISIBLE);
            setPropertyChangeHandler(PROP_VISIBLE, (oldValue, newValue, refreshableFigure) -> {
                boolean visible = (Boolean) newValue;
                var figure = getFigure();
                figure.setVisible(visible);
                return true;
            });
        }

        setPropertyChangeHandler(PROP_SHOW_SCROLLBAR, (oldValue, newValue, refreshableFigure) -> {
            ((GroupingContainerFigure) refreshableFigure).setShowScrollBar((Boolean) newValue);
            return true;
        });

        IWidgetPropertyChangeHandler fowardColorHandler = (oldValue, newValue, refreshableFigure) -> {
            if (getWidgetModel().isForwardColors()) {
                forwardColors();
            }
            return true;
        };
        setPropertyChangeHandler(PROP_FORWARD_COLORS, fowardColorHandler);
        setPropertyChangeHandler(PROP_COLOR_BACKGROUND, fowardColorHandler);
        setPropertyChangeHandler(PROP_COLOR_FOREGROUND, fowardColorHandler);

        // use property listener because it doesn't need to be queued in GUIRefreshThread.
        getWidgetModel().getProperty(PROP_WIDTH)
                .addPropertyChangeListener(
                        evt -> resizeChildren((Integer) (evt.getNewValue()), (Integer) (evt.getOldValue()), true));

        getWidgetModel().getProperty(PROP_HEIGHT)
                .addPropertyChangeListener(
                        evt -> resizeChildren((Integer) (evt.getNewValue()), (Integer) (evt.getOldValue()), false));
    }

    private void forwardColors() {
        for (var o : getChildren()) {
            if (o instanceof AbstractBaseEditPart) {
                ((AbstractBaseEditPart) o).setPropertyValue(PROP_COLOR_BACKGROUND,
                        getWidgetModel().getPropertyValue(PROP_COLOR_BACKGROUND));
                ((AbstractBaseEditPart) o).setPropertyValue(PROP_COLOR_FOREGROUND,
                        getWidgetModel().getPropertyValue(PROP_COLOR_FOREGROUND));

            }
        }
    }

    private void lockChildren(boolean lock) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            return;
        }
        for (var o : getChildren()) {
            if (o instanceof AbstractBaseEditPart) {
                ((AbstractBaseEditPart) o).setSelectable(!lock);
            }
        }
    }

    @Override
    protected EditPart createChild(Object model) {
        var result = super.createChild(model);

        // setup selection behavior for the new child
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && result instanceof AbstractBaseEditPart) {
            ((AbstractBaseEditPart) result).setSelectable(!getWidgetModel().isLocked());
        }

        return result;
    }

    private void resizeChildren(int newValue, int oldValue, boolean isWidth) {
        if (!getWidgetModel().isLocked()) {
            return;
        }
        if (getExecutionMode() == ExecutionMode.RUN_MODE
                && getWidgetModel().getRootDisplayModel().getDisplayScaleData().isAutoScaleWidgets()
                && (getWidgetModel().getScaleOptions().isHeightScalable()
                        || getWidgetModel().getScaleOptions().isWidthScalable())) {
            return;
        }
        var ratio = (newValue - oldValue) / (double) oldValue;
        for (var child : getWidgetModel().getChildren()) {
            if (isWidth) {
                child.setX((int) (child.getX() * (1 + ratio)));
                child.setWidth((int) (child.getWidth() * (1 + ratio)));
            } else {
                child.setY((int) (child.getY() * (1 + ratio)));
                child.setHeight((int) (child.getHeight() * (1 + ratio)));
            }
        }
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter == IActionFilter.class) {
            return new BaseEditPartActionFilter() {
                @Override
                public boolean testAttribute(Object target, String name, String value) {
                    if (name.equals("allowAutoSize") && value.equals("TRUE")) {
                        return getExecutionMode() == ExecutionMode.EDIT_MODE;
                    }
                    return super.testAttribute(target, name, value);
                }
            };
        }
        return super.getAdapter(adapter);
    }
}
