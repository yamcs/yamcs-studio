/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OPIShellsChangedHandler extends AbstractHandler {

    public static final String ID = "org.csstudio.opibuilder.opiShellsChanged";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var workbench = PlatformUI.getWorkbench();
        var activePage = workbench.getActiveWorkbenchWindow().getActivePage();

        for (var workbenchWindow : workbench.getWorkbenchWindows()) {
            for (var page : workbenchWindow.getPages()) {
                var view = (OPIShellSummary) page.findView(OPIShellSummary.ID);

                if (page == activePage && (view == null || !page.isPartVisible(view))) {
                    view = showSummaryView(page);
                }

                if (view != null) {
                    view.update();
                }
            }
        }

        return null;
    }

    private OPIShellSummary showSummaryView(IWorkbenchPage page) throws ExecutionException {
        try {
            return (OPIShellSummary) page.showView(OPIShellSummary.ID);
        } catch (PartInitException e) {
            throw new ExecutionException("Failed to display view", e);
        }
    }
}
