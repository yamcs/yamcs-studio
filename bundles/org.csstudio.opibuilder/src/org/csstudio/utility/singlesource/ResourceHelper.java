/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.singlesource;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Helper for accessing resources.
 *
 * <p>
 * This implementation provides the common support. Derived classes can add support that is specific to RCP or RAP.
 *
 * <p>
 * Client code should obtain a {@link ResourceHelper} via the {@link SingleSourcePlugin}
 *
 * @author Kay Kasemir
 * @author Xihui Chen, Abadie Lana, Eric Berryman - ResourceUtil of BOY, contributions to PV Table
 */
public class ResourceHelper {

    /**
     * Check if a path exists
     *
     * <p>
     * Default implementation is limited to local files and URLs.
     *
     * @param path
     *            Path to workspace file, local file, URL
     * @return <code>true</code> if the path points to an existing item
     */
    public boolean exists(final IPath path) {
        // Try workspace file
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (resource != null &&
                resource.isAccessible() &&
                resource instanceof IFile)
            return true;

        // Try file outside of the workspace
        File file = path.toFile();
        if (file != null)
            return file.exists();

        // Check URL
        try {
            new URL(path.toString()).openStream().close();
            return true;
        } catch (Exception ex) {
            // Ignore
        }
        return false;
    }

    /**
     * Obtain file for path within workspace
     * 
     * @param path
     *            Path to a resource in the workspace
     * @return IFile for path or <code>null</code>
     */
    static IFile getFileForPath(final IPath path) {
        if (path == null)
            return null;
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        return root.getFile(path);
    }

    /**
     * Adapt a path to some other class
     *
     * @param path
     *            {@link IPath} to adapt
     * @param adapter
     *            Desired class, for example IFile
     * @return Adapted path or <code>null</code>
     */
    @SuppressWarnings("rawtypes")
    public Object adapt(final IPath path, final Class adapter) {
        // For getInputStream() and getOutputStream() to function,
        // path must adapt to IFile.
        if (adapter == IFile.class)
            return getFileForPath(path);
        // By default, don't adapt, but log to aid in future extension of this code.
        final Logger logger = Logger.getLogger(getClass().getName());
        logger.fine("Cannot read adapt path " + path + " to " + adapter.getName());
        return null;
    }
}
