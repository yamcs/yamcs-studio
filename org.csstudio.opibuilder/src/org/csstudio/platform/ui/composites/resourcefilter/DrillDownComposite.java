/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.platform.ui.composites.resourcefilter;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.DrillDownAdapter;

/**
 * Class <code>DrillDownComposite</code> implements a simple web style navigation metaphor. Home, back, and "drill into"
 * buttons are added to a tree viewer for easier navigation.
 *
 * <p>
 * <b>Code is based upon <code>org.eclipse.ui.part.DrillDownComposite</code> in plugin
 * <code>org.eclipse.ui.workbench</code>.</b>
 * </p>
 */
// TODO: Copied from org.csstudio.platform.ui.
public class DrillDownComposite extends Composite {

    /**
     * The ToolBarManager of this DrillDownComposite.
     */
    private ToolBarManager _toolBarMgr;

    /**
     * The Treeviewer of this DrillDownComposite.
     */
    private TreeViewer _fChildTree;

    /**
     * The DrillDownAdapter for the Tree.
     */
    private DrillDownAdapter _adapter;

    public DrillDownComposite(Composite parent, int style) {
        super(parent, style);
        createNavigationButtons();
    }

    /**
     * Creates the navigation buttons for this viewer.
     */
    private void createNavigationButtons() {
        GridData gid;
        GridLayout layout;

        // Define layout.
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);

        // Create a toolbar.
        _toolBarMgr = new ToolBarManager(SWT.FLAT);
        var toolBar = _toolBarMgr.createControl(this);
        gid = new GridData();
        gid.horizontalAlignment = GridData.FILL;
        gid.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(gid);
    }

    /**
     * Sets the child viewer. This method should only be called once, after the viewer has been created.
     *
     * @param aViewer
     *            the new child viewer
     */
    public void setChildTree(TreeViewer aViewer) {
        // Save viewer.
        _fChildTree = aViewer;

        // Create adapter.
        _adapter = new DrillDownAdapter(_fChildTree);
        _adapter.addNavigationActions(_toolBarMgr);
        _toolBarMgr.update(true);

        // Set tree layout.
        _fChildTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout();
    }

    /**
     * Delivers the ToolBarManager of this DrillDownComposite.
     *
     * @return ToolBarManager The ToolbarManager of this DrillDownComposite
     */
    public final ToolBarManager getToolBarManager() {
        return _toolBarMgr;
    }

}
