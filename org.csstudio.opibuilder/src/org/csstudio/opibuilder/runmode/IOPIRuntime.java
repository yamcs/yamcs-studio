/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.model.DisplayModel;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * The common interface for OPI runtime, which could be an editor or view.
 */
public interface IOPIRuntime extends IWorkbenchPart, IAdaptable {

    /**
     * Set workbench part name. It calls setPartName() from editor or view to make it public visible.
     * 
     * @param name
     */
    public void setWorkbenchPartName(String name);

    /**
     * Set the OPI input. The OPI Runtime will reload OPI from the input.
     * 
     * @param input
     * @throws PartInitException
     */
    public void setOPIInput(IEditorInput input) throws PartInitException;

    /**
     * @return the OPI input of the runtime.
     */
    public IEditorInput getOPIInput();

    /**
     * @return the display model in this runtime.
     */
    public DisplayModel getDisplayModel();
}
