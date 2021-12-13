/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.explorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class OpenActionProvider extends CommonActionProvider {

    private OpenFileAction openFileAction;
    private ICommonViewerWorkbenchSite viewSite;
    private boolean contribute = false;

    @Override
    public void init(ICommonActionExtensionSite aConfig) {
        if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
            openFileAction = new OpenFileAction(viewSite.getPage(), viewSite.getSelectionProvider());
            contribute = true;
        }
    }

    @Override
    public void fillContextMenu(IMenuManager aMenu) {
        if (!contribute || getContext().getSelection().isEmpty()) {
            return;
        }

        if (openFileAction.isEnabled()) {
            aMenu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openFileAction);
        }

        var perspective = viewSite.getPage().getPerspective();
        if (!perspective.getId().equals("org.csstudio.opibuilder.OPIRuntime.perspective")) {
            addOpenWithMenu(aMenu);
        }
    }

    @Override
    public void fillActionBars(IActionBars theActionBars) {
        if (!contribute) {
            return;
        }
        var selection = (IStructuredSelection) getContext().getSelection();
        if (selection.size() == 1 && selection.getFirstElement() instanceof IFile) {
            theActionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openFileAction);
        }
    }

    private void addOpenWithMenu(IMenuManager aMenu) {
        var ss = (IStructuredSelection) getContext().getSelection();

        if (ss == null || ss.size() != 1) {
            return;
        }

        var o = ss.getFirstElement();

        // first try IResource
        IAdaptable openable = Adapters.adapt(o, IResource.class);
        // otherwise try ResourceMapping
        if (openable == null) {
            openable = Adapters.adapt(o, ResourceMapping.class);
        } else if (((IResource) openable).getType() != IResource.FILE) {
            openable = null;
        }

        if (openable != null) {
            IMenuManager submenu = new MenuManager("Open With", ICommonMenuConstants.GROUP_OPEN_WITH);
            submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_TOP));
            submenu.add(new OpenWithMenu(viewSite.getPage(), openable));
            submenu.add(new GroupMarker(ICommonMenuConstants.GROUP_ADDITIONS));
            if (submenu.getItems().length > 2 && submenu.isEnabled()) {
                aMenu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
            }
        }
    }
}
