/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.utility.singlesource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Persistable editor input based on path
 *
 * <p>
 * Uses an {@link IPath}, which is supported by both RCP and RAP, as the persisted identifier of an editor input.
 */
public class PathEditorInput implements IPathEditorInput, IPersistableElement {
    final private IPath path;

    /**
     * Initialize
     * 
     * @param path
     *            {@link IPath} that identifies this input
     */
    public PathEditorInput(IPath path) {
        this.path = path;
    }

    /**
     * @return Path that identifies the editor input. Maybe workspace file, file system file or URL
     */
    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IPathEditorInput)) {
            return false;
        }
        var other = ((IPathEditorInput) obj).getPath();
        // Try shortcut if it's the same PathEditorInput and thus path,
        // else compare portable representation
        return other == path || other.toPortableString().equals(path.toPortableString());
    }

    @Override
    public boolean exists() {
        // Try workspace file
        var resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (resource != null && resource.isAccessible() && resource instanceof IFile) {
            return true;
        }

        // Try file outside of the workspace
        var file = path.toFile();
        if (file != null) {
            return file.exists();
        }

        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return path.lastSegment();
    }

    @Override
    public IPersistableElement getPersistable() {
        return this;
    }

    @Override
    public String getToolTipText() {
        return path.toOSString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object getAdapter(Class adapter) {
        if (path == null) {
            return null;
        }
        var root = ResourcesPlugin.getWorkspace().getRoot();
        return root.getFile(path);
    }

    @Override
    public void saveState(IMemento memento) {
        memento.putString(PathEditorInputFactory.TAG_PATH, path.toPortableString());
    }

    @Override
    public String getFactoryId() {
        return PathEditorInputFactory.ID;
    }

    /** @return Debug representation */
    @Override
    public String toString() {
        return path.toString();
    }
}
