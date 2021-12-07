/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractContainerEditpart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
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
        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                figure.setOpaque(!(Boolean) newValue);
                return true;
            }
        };

        setPropertyChangeHandler(GroupingContainerModel.PROP_TRANSPARENT, handler);

        handler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                for (AbstractWidgetModel child : getWidgetModel().getChildren()) {
                    child.setEnabled((Boolean) newValue);
                }
                return true;
            }
        };

        setPropertyChangeHandler(AbstractWidgetModel.PROP_ENABLED, handler);

        handler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                lockChildren((Boolean) newValue);
                return true;
            }
        };

        setPropertyChangeHandler(GroupingContainerModel.PROP_LOCK_CHILDREN, handler);

        lockChildren(getWidgetModel().isLocked());
        if (getWidgetModel().getParent() instanceof TabModel) {
            removeAllPropertyChangeHandlers(AbstractWidgetModel.PROP_VISIBLE);
            IWidgetPropertyChangeHandler visibilityHandler = new IWidgetPropertyChangeHandler() {
                @Override
                public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                    boolean visible = (Boolean) newValue;
                    var figure = getFigure();
                    figure.setVisible(visible);
                    return true;
                }
            };
            setPropertyChangeHandler(AbstractWidgetModel.PROP_VISIBLE, visibilityHandler);
        }

        IWidgetPropertyChangeHandler showBarHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                ((GroupingContainerFigure) refreshableFigure).setShowScrollBar((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(GroupingContainerModel.PROP_SHOW_SCROLLBAR, showBarHandler);

        IWidgetPropertyChangeHandler fowardColorHandler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                if (getWidgetModel().isForwardColors()) {
                    forwardColors();
                }
                return true;
            }
        };
        setPropertyChangeHandler(GroupingContainerModel.PROP_FORWARD_COLORS, fowardColorHandler);
        setPropertyChangeHandler(GroupingContainerModel.PROP_COLOR_BACKGROUND, fowardColorHandler);
        setPropertyChangeHandler(GroupingContainerModel.PROP_COLOR_FOREGROUND, fowardColorHandler);

        // use property listener because it doesn't need to be queued in GUIRefreshThread.
        getWidgetModel().getProperty(AbstractWidgetModel.PROP_WIDTH)
                .addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        resizeChildren((Integer) (evt.getNewValue()), (Integer) (evt.getOldValue()), true);

                    }
                });

        getWidgetModel().getProperty(AbstractWidgetModel.PROP_HEIGHT)
                .addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        resizeChildren((Integer) (evt.getNewValue()), (Integer) (evt.getOldValue()), false);
                    }
                });

    }

    private void forwardColors() {
        for (Object o : getChildren()) {
            if (o instanceof AbstractBaseEditPart) {
                ((AbstractBaseEditPart) o).setPropertyValue(AbstractWidgetModel.PROP_COLOR_BACKGROUND,
                        getWidgetModel().getPropertyValue(AbstractWidgetModel.PROP_COLOR_BACKGROUND));
                ((AbstractBaseEditPart) o).setPropertyValue(AbstractWidgetModel.PROP_COLOR_FOREGROUND,
                        getWidgetModel().getPropertyValue(AbstractWidgetModel.PROP_COLOR_FOREGROUND));

            }
        }
    }

    /**
     * @param lock
     *            true if the children should be locked.
     */
    private void lockChildren(boolean lock) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            return;
        }
        for (Object o : getChildren()) {
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
        for (AbstractWidgetModel child : getWidgetModel().getChildren()) {
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
