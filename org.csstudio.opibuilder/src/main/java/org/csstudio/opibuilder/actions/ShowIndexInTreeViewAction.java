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

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The action to show/hide the index in tree view.
 */
public class ShowIndexInTreeViewAction extends Action {

    public static final String ID = "org.csstudio.opibuilder.actions.showIndexInTreeView";
    private EditPartViewer editPartViewer;

    public static final String SHOW_INDEX_PROPERTY = "show_index_property";

    private boolean showIndex = false;

    private ImageDescriptor showIndexImage = CustomMediaFactory.getInstance()
            .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/show_index.png");
    private ImageDescriptor hideIndexImage = CustomMediaFactory.getInstance()
            .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/hide_index.png");

    public ShowIndexInTreeViewAction(EditPartViewer editPartViewer) {
        setText("Show Index");
        setId(ID);
        setImageDescriptor(showIndexImage);
        this.editPartViewer = editPartViewer;
    }

    @Override
    public void run() {
        showIndex = !showIndex;
        editPartViewer.setProperty(SHOW_INDEX_PROPERTY, showIndex);
        editPartViewer.getRootEditPart().getContents().refresh();
        if (showIndex) {
            setText("Hide Index");
            setImageDescriptor(hideIndexImage);
        } else {
            setText("Show Index");
            setImageDescriptor(showIndexImage);
        }
    }
}
