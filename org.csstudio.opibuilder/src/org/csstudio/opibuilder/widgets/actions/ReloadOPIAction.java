/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.widgets.editparts.LinkingContainerEditpart;
import org.csstudio.opibuilder.widgets.model.LinkingContainerModel;
import org.eclipse.jface.action.IAction;

/**
 * Reload OPI to linking container.
 */
public class ReloadOPIAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        var property = getSelectedContianerWidget().getWidgetModel().getProperty(LinkingContainerModel.PROP_OPI_FILE);
        property.setPropertyValue(property.getPropertyValue(), true);
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final LinkingContainerEditpart getSelectedContianerWidget() {
        return (LinkingContainerEditpart) selection.getFirstElement();
    }
}
