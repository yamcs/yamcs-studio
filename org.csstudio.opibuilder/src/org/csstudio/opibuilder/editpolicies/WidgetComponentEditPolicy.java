/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editpolicies;

import org.csstudio.opibuilder.commands.WidgetDeleteCommand;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * Component Editpolicy for widgets.
 */
public class WidgetComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest deleteRequest) {
        var containerModel = getHost().getParent().getModel();
        var widget = getHost().getModel();

        if (containerModel instanceof AbstractContainerModel && widget instanceof AbstractWidgetModel) {
            return new WidgetDeleteCommand((AbstractContainerModel) containerModel, (AbstractWidgetModel) widget);
        }
        return super.createDeleteCommand(deleteRequest);
    }
}
