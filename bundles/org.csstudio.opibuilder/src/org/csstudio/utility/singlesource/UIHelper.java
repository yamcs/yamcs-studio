/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.singlesource;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class UIHelper {

    /**
     * @param site
     *            Site on which to enable/disable closing
     * @param enable_close
     *            Enable the close button, allow closing the part?
     */
    public void enableClose(IWorkbenchPartSite site, boolean enable_close) {
        // TODO Improve implementation

        // Configure the E4 model element.
        // Issue 1:
        // When opening the display for the first time,
        // the 'x' in the tab is still displayed.
        // Only on _restart_ of the app will the tab be displayed
        // without the 'x' to close it.
        // Issue 2:
        // Part can still be closed via Ctrl-W (Command-W on OS X)
        // or via menu File/close.
        final MPart part = site.getService(MPart.class);
        part.setCloseable(false);

        // Original RCP code
        // PartPane currentEditorPartPane = ((PartSite) site)
        // .getPane();
        // PartStack stack = currentEditorPartPane.getStack();
        // Control control = stack.getControl();
        // if (control instanceof CTabFolder) {
        // CTabFolder tabFolder = (CTabFolder) control;
        // tabFolder.getSelection().setShowClose(false);
        // }
    }

    /**
     * @param view
     *            View to 'detach'
     */
    public void detachView(IViewPart view) {
        // TODO Use more generic IWorkbenchPart?, getPartSite()?
        // Pre-E4 code:
        // ((WorkbenchPage)page).detachView(page.findViewReference(OPIView.ID, secondID));
        // See http://tomsondev.bestsolution.at/2012/07/13/so-you-used-internal-api/
        final EModelService model = view.getSite().getService(EModelService.class);
        MPartSashContainerElement p = view.getSite().getService(MPart.class);
        // Part may be shared by several perspectives, get the shared instance
        if (p.getCurSharedRef() != null)
            p = p.getCurSharedRef();
        model.detach(p, 100, 100, 600, 800);
    }
}
