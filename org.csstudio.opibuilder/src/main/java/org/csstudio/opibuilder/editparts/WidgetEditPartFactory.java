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

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * The central factory to create editpart for all widgets.
 */
public class WidgetEditPartFactory implements EditPartFactory {

    private ExecutionMode executionMode;

    private IWorkbenchPartSite site;

    public WidgetEditPartFactory(ExecutionMode executionMode, IWorkbenchPartSite site) {
        this(executionMode);
        this.site = site;
    }

    public WidgetEditPartFactory(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        var part = getPartForModel(model);
        if (part != null) {
            part.setModel(model);
            if (part instanceof AbstractBaseEditPart) {
                ((AbstractBaseEditPart) part).setExecutionMode(executionMode);
                ((AbstractBaseEditPart) part).setSite(site);
            } else if (part instanceof WidgetConnectionEditPart) {
                ((WidgetConnectionEditPart) part).setExecutionMode(executionMode);
            }
        }
        return part;
    }

    private EditPart getPartForModel(Object model) {
        if (model instanceof DisplayModel) {
            return new DisplayEditpart();
        }
        if (model instanceof ConnectionModel) {
            return new WidgetConnectionEditPart();
        }
        if (model instanceof AbstractWidgetModel) {
            var editpart = WidgetsService.getInstance().getWidgetDescriptor(((AbstractWidgetModel) model).getTypeID())
                    .getWidgetEditpart();
            return editpart;
        }
        OPIBuilderPlugin.getLogger().log(Level.WARNING, "Cannot create editpart for model object {0}",
                model == null ? "null" : model.getClass().getName());
        return null;
    }
}
