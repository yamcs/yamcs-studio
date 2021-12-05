/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.runmode.IOPIRuntime;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Refresh the OPI just like the OPI is reopened.
 */
public class RefreshOPIAction extends Action {

    final private IOPIRuntime opiRuntime;

    public RefreshOPIAction(IOPIRuntime opiRuntime) {
        this.opiRuntime = opiRuntime;
        setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);
        setId(ActionFactory.REFRESH.getId());
        setText("Refresh OPI");
        setImageDescriptor(
                CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(
                        OPIBuilderPlugin.PLUGIN_ID, "icons/refresh.gif"));
    }

    @Override
    public void run() {
        try {
            opiRuntime.setOPIInput(opiRuntime.getOPIInput());
        } catch (PartInitException e) {
            ErrorHandlerUtil.handleError("Failed to refresh OPI", e);
        }
    }

}
