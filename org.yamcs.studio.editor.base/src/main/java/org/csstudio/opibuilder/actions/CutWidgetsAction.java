/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.editor.OPIEditor;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Cut widgets to clipboard.
 */
public class CutWidgetsAction extends CopyWidgetsAction {

    private DeleteAction deleteAction;

    public CutWidgetsAction(OPIEditor part, DeleteAction deleteAction) {
        super(part);
        this.deleteAction = deleteAction;
        setText("Cut");
        setActionDefinitionId("org.eclipse.ui.edit.cut");
        setId(ActionFactory.CUT.getId());
        var sharedImages = part.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
    }

    @Override
    public void run() {
        super.run();
        deleteAction.run();
    }
}
