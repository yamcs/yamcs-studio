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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_NAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;

import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.actions.ShowIndexInTreeViewAction;
import org.csstudio.opibuilder.editpolicies.WidgetComponentEditPolicy;
import org.csstudio.opibuilder.editpolicies.WidgetTreeEditPolicy;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;

/**
 * Basic tree editpart for all widgets.
 */
public class WidgetTreeEditpart extends AbstractTreeEditPart {

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();

        installEditPolicy(EditPolicy.COMPONENT_ROLE, new WidgetComponentEditPolicy());
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new WidgetTreeEditPolicy());
    }

    @Override
    public void activate() {
        super.activate();
        PropertyChangeListener visualListener = evt -> refreshVisuals();
        var nameProperty = getWidgetModel().getProperty(PROP_NAME);
        if (nameProperty != null) {
            nameProperty.addPropertyChangeListener(visualListener);
        }
        var pvNameProperty = getWidgetModel().getProperty(PROP_PVNAME);
        if (pvNameProperty != null) {
            pvNameProperty.addPropertyChangeListener(visualListener);
        }
    }

    public WidgetTreeEditpart(AbstractWidgetModel model) {
        super(model);
    }

    public AbstractWidgetModel getWidgetModel() {
        return (AbstractWidgetModel) getModel();
    }

    @Override
    protected Image getImage() {
        if (getWidgetModel() instanceof DisplayModel) {
            return super.getImage();
        }
        var typeID = getWidgetModel().getTypeID();
        var widgetDescriptor = WidgetsService.getInstance().getWidgetDescriptor(typeID);
        var image = CustomMediaFactory.getInstance().getImageFromPlugin(widgetDescriptor.getPluginId(),
                widgetDescriptor.getIconPath());
        return image;
    }

    @Override
    protected String getText() {

        var sb = new StringBuilder();
        var obj = getViewer().getProperty(ShowIndexInTreeViewAction.SHOW_INDEX_PROPERTY);
        if (obj != null && obj instanceof Boolean && (Boolean) obj) {
            sb.append(Integer.toString(getWidgetModel().getIndex()));
            sb.append("_");
        }
        sb.append(getWidgetModel().getName());
        if (getWidgetModel() instanceof AbstractPVWidgetModel) {
            var pvWidgetModel = (AbstractPVWidgetModel) getWidgetModel();
            var pvName = pvWidgetModel.getPVName();
            if (pvName != null && !pvName.trim().equals("")) {
                sb.append("(");
                sb.append(pvName);
                sb.append(")");
            }
        }
        return sb.toString();
    }

    @Override
    protected void refreshVisuals() {
        if (getWidget() instanceof Tree) {
            return;
        }
        super.refreshVisuals();
    }
}
