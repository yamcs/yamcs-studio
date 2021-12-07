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

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * The factory creating tree editpart from model.
 */
public class WidgetTreeEditpartFactory implements EditPartFactory {

    @Override
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof AbstractContainerModel) {
            return new ContainerTreeEditpart((AbstractContainerModel) model);
        }
        if (model instanceof AbstractWidgetModel) {
            return new WidgetTreeEditpart((AbstractWidgetModel) model);
        }
        return null;
    }

}
