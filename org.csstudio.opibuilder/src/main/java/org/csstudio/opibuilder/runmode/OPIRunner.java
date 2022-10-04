/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.model.DisplayModel;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * The editor for running of OPI.
 */
// DO NOT REMOVE. RAP NEEDS THIS!
public class OPIRunner extends EditorPart implements IOPIRuntime {

    public static final String ID = "org.csstudio.opibuilder.OPIRunner";

    private OPIRuntimeDelegate opiRuntimeDelegate;

    public OPIRunner() {
        opiRuntimeDelegate = new OPIRuntimeDelegate(this);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void dispose() {
        if (opiRuntimeDelegate != null) {
            opiRuntimeDelegate.dispose();
            opiRuntimeDelegate = null;
        }
        super.dispose();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);

        setInput(input);
        opiRuntimeDelegate.init(site, input);
    }

    @Override
    public void setOPIInput(IEditorInput input) throws PartInitException {
        init(getEditorSite(), input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        opiRuntimeDelegate.createGUI(parent);
        // if this is the first OPI in this window, resize the window to match the OPI size.
        Display.getCurrent().asyncExec(() -> {
            if (getSite().getWorkbenchWindow().getActivePage().getEditorReferences().length == 1
                    && getSite().getWorkbenchWindow().getActivePage().getViewReferences().length == 0) {
                int trimWidth = 45, trimHeight = 165;
                Rectangle bounds;
                if (opiRuntimeDelegate.getDisplayModel() != null) {
                    bounds = opiRuntimeDelegate.getDisplayModel().getBounds();
                } else {
                    bounds = new Rectangle(-1, -1, 800, 600);
                }
                if (bounds.x >= 0 && bounds.y >= 0) {
                    parent.getShell().setLocation(bounds.x, bounds.y);
                }
                parent.getShell().setSize(bounds.width + trimWidth, bounds.height + trimHeight);
            }
        });
    }

    @Override
    public void setFocus() {
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (opiRuntimeDelegate != null) {
            var obj = opiRuntimeDelegate.getAdapter(adapter);
            if (obj != null) {
                return obj;
            }
        }
        return super.getAdapter(adapter);
    }

    @Override
    public void setWorkbenchPartName(String name) {
        setPartName(name);
        setTitleToolTip(getEditorInput().getToolTipText());
    }

    public OPIRuntimeDelegate getOPIRuntimeDelegate() {
        return opiRuntimeDelegate;
    }

    @Override
    public IEditorInput getOPIInput() {
        return getOPIRuntimeDelegate().getEditorInput();
    }

    @Override
    public DisplayModel getDisplayModel() {
        return getOPIRuntimeDelegate().getDisplayModel();
    }
}
