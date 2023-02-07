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

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * RCP View summarising OPI shells. Only one should be open at any one time.
 */
public class OPIShellSummary extends ViewPart {

    public static final String ID = "org.csstudio.opibuilder.opiShellSummary";

    private OPIShellsTableViewer tableViewer;
    private boolean disposed = false;
    private Set<OPIShell> cachedShells;

    @Override
    public void init(final IViewSite site, final IMemento memento) throws PartInitException {
        super.init(site, memento);
        cachedShells = OPIShell.getAllShells();
        for (var shell : cachedShells) {
            shell.registerWithView(this);
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        var tableWrapper = new Composite(parent, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_BOTH));
        var tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);
        tableViewer = new OPIShellsTableViewer(tableWrapper, tcl);

        if (getViewSite() != null) {
            getViewSite().setSelectionProvider(tableViewer);
        }

        // Set initial state
        tableViewer.refresh();
    }

    @Override
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }

    public void update() {
        var updatedShells = OPIShell.getAllShells();

        // Register right-click with any new shells.
        for (var shell : updatedShells) {
            if (!cachedShells.contains(shell)) {
                shell.registerWithView(this);
            }
        }
        cachedShells = updatedShells;
        tableViewer.setInput(new ArrayList<>(cachedShells));
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (OPIShell.getActiveShell() == null) {
            return null;
        }
        return OPIShell.getActiveShell().getAdapter(adapter);
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        // Notify shell that this view has been disposed
        for (var shell : cachedShells) {
            shell.notifyParentViewClosed();
        }
        super.dispose();
        disposed = true;
    }
}
