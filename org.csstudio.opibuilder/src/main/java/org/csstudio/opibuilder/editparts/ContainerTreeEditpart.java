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

import java.beans.PropertyChangeListener;
import java.util.List;

import org.csstudio.opibuilder.editpolicies.WidgetContainerEditPolicy;
import org.csstudio.opibuilder.editpolicies.WidgetTreeContainerEditPolicy;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

/**
 * Tree Editpart for container widgets.
 */
public class ContainerTreeEditpart extends WidgetTreeEditpart {

    private PropertyChangeListener childrenPropertyChangeListener;

    public ContainerTreeEditpart(AbstractContainerModel model) {
        super(model);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getWidgetModel().isChildrenOperationAllowable()) {
            installEditPolicy(EditPolicy.CONTAINER_ROLE, new WidgetContainerEditPolicy());
            installEditPolicy(EditPolicy.TREE_CONTAINER_ROLE, new WidgetTreeContainerEditPolicy());
        }
        // If this editpart is the contents of the viewer, then it is not deletable!
        if (getParent() instanceof RootEditPart) {
            installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        }
    }

    @Override
    public void activate() {
        super.activate();

        childrenPropertyChangeListener = evt -> {
            if (evt.getOldValue() instanceof Integer) {
                addChild(createChild(evt.getNewValue()), ((Integer) evt.getOldValue()).intValue());
            } else if (evt.getOldValue() instanceof AbstractWidgetModel) {
                var child = (EditPart) getViewer().getEditPartRegistry().get(evt.getOldValue());
                if (child != null) {
                    removeChild(child);
                }
            } else {
                refreshChildren();
            }
            refreshVisuals();
        };
        getWidgetModel().getChildrenProperty().addPropertyChangeListener(childrenPropertyChangeListener);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        getWidgetModel().getChildrenProperty().removePropertyChangeListener(childrenPropertyChangeListener);
    }

    @Override
    public AbstractContainerModel getWidgetModel() {
        return (AbstractContainerModel) getModel();
    }

    @Override
    protected List<AbstractWidgetModel> getModelChildren() {
        return getWidgetModel().getChildren();
    }

    @Override
    protected void refreshVisuals() {
        super.refreshVisuals();
        for (var child : getChildren()) {
            if (child instanceof WidgetTreeEditpart) {
                ((WidgetTreeEditpart) child).refreshVisuals();
            }
        }
    }
}
