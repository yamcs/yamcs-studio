/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.platform.ui.composites.resourcefilter;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides workspace filtering resources as content for a tree viewer.
 */

final class WorkspaceResourceContentProvider implements ITreeContentProvider {
    /**
     * Flag that signals if closed projects should be included as well.
     */
    private boolean showClosedProjects = true;

    /**
     * File extensions of files to include in the result lists.
     */
    private String[] filters;

    /**
     * Creates a new <code>WorkspaceResourcesContentProvider</code>.
     *
     * @param filters
     */
    public WorkspaceResourceContentProvider(String[] filters) {
        if (filters != null) {
            this.filters = new String[filters.length];
            System.arraycopy(filters, 0, this.filters, 0, filters.length);
        } else {
            this.filters = new String[0];
        }
    }

    /**
     * The visual part that is using this content provider is about to be disposed. Deallocate all allocated SWT
     * resources.
     */
    @Override
    public void dispose() {
    }

    @Override
    public Object[] getChildren(Object element) {
        if (element instanceof IWorkspace) {
            // check if closed projects should be shown
            var allProjects = ((IWorkspace) element).getRoot().getProjects();
            if (showClosedProjects) {
                return allProjects;
            }

            var accessibleProjects = new ArrayList<IProject>();
            for (var i = 0; i < allProjects.length; i++) {
                if (allProjects[i].isOpen()) {
                    accessibleProjects.add(allProjects[i]);
                }
            }
            return accessibleProjects.toArray();
        } else if (element instanceof IContainer) {
            var container = (IContainer) element;
            if (container.isAccessible()) {
                try {
                    var children = new ArrayList<IResource>();
                    var members = container.members();
                    for (var member : members) {
                        if (includeResource(member)) {
                            children.add(member);
                        }
                    }
                    return children.toArray();
                } catch (CoreException e) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", e);
                }
            }
        }
        return new Object[0];
    }

    /**
     * Returns whether the given resource should be included in the contents this content provider returns.
     *
     * @param resource
     *            the resource.
     * @return <code>true</code> if the resource should be included, <code>false</code> otherwise.
     */
    private boolean includeResource(IResource resource) {
        if (resource.getType() != IResource.FILE || filters == null) {
            // non-files are always included
            return true;
        } else {
            for (var filter : filters) {
                if (resource.getName().contains(filter)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Object[] getElements(Object element) {
        return getChildren(element);
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IResource) {
            return ((IResource) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /**
     * Specify whether or not to show closed projects in the tree viewer. Default is to show closed projects.
     *
     * @param show
     *            boolean if false, do not show closed projects in the tree
     */
    public void showClosedProjects(boolean show) {
        showClosedProjects = show;
    }
}
