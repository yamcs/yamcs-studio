/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.singlesource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;

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
     * Create {@link IPath} for string
     * 
     * @param path
     *            Path to workspace file, file system file or URL
     * @return {@link IPath}
     */
    public IPath newPath(final String path) {
        try {
            if (isURL(path))
                return new URLPath(path);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Cannot handle URL path " + path, ex);
        }
        return new Path(path);
    }

    /**
     * Obtain path for editor input
     * 
     * @param input
     *            IEditorInput
     * @return IPath or <code>null</code>
     */
    public IPath getPath(final IEditorInput input) {
        final IFile ws_file = (IFile) input.getAdapter(IFile.class);
        if (ws_file != null)
            return ws_file.getFullPath();
        if (input instanceof IPathEditorInput)
            return ((IPathEditorInput) input).getPath();
        return null;
    }

    /**
     * Check if a path is actually a URL (http://, ftp://, ..)
     * 
     * @param url
     *            Possible URL
     * @return <code>true</code> if considered a URL, <code>false</code> for file path
     */
    private boolean isURL(final String path) {
        try {
            new URL(path);
        } catch (Exception ex) { // Not a valid URL
            return false;
        }
        return true;
    }

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
        final File file = getFilesystemFile(path);
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

    /**
     * Obtain input stream for path
     *
     * <p>
     * Depending on the implementation, the path may be
     * <ul>
     * <li>Workspace location
     * <li>Local file
     * <li>URL
     * </ul>
     *
     * <p>
     * Default implementation is limited to local files.
     *
     * @param path
     *            IPath
     * @return {@link InputStream}
     * @throws Exception
     *             on error
     */
    public InputStream getInputStream(final IPath path) throws Exception {
        // Try workspace file
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IResource ws_file = root.findMember(path);
        if (ws_file instanceof IFile)
            return ((IFile) ws_file).getContents(true);

        // Try file outside of the workspace
        final File file = getFilesystemFile(path);
        if (file != null)
            return new FileInputStream(file);

        // Try URL
        return new URL(path.toString()).openStream();
    }

    /**
     * Obtain input stream for editor input
     *
     * <p>
     * Depending on the implementation, the path may be
     * <ul>
     * <li>Workspace location
     * <li>Local file
     * <li>TODO URL
     * </ul>
     *
     * <p>
     * Default implementation is limited to local files.
     *
     * @param input
     *            IEditorInput
     * @return {@link InputStream} or <code>null</code> if input cannot be resolved
     * @throws Exception
     *             on error
     */
    public InputStream getInputStream(final IEditorInput input) throws Exception {
        // Try workspace file
        final IFile ws_file = (IFile) input.getAdapter(IFile.class);
        if (ws_file != null && ws_file.exists())
            return ws_file.getContents(true);

        // Try file outside of the workspace
        final File file = getFilesystemFile(input);
        if (file != null)
            return new FileInputStream(file);

        // Try URL
        if (input instanceof PathEditorInput) {
            final IPath path = ((PathEditorInput) input).getPath();
            return getInputStream(path);
        }

        // Didn't find anything. Log adapters to aid in future extension of this code.
        final Logger logger = Logger.getLogger(getClass().getName());
        logger.fine("Cannot read from " + input.getClass().getName());
        for (String adapt : Platform.getAdapterManager().computeAdapterTypes(input.getClass()))
            logger.finer("Would adapt to " + adapt);

        return null;
    }

    /**
     * Check if editor input is writeable
     *
     * <p>
     * Default implementation is limited to local files.
     *
     * @param input
     *            {@link IEditorInput}
     * @return <code>true</code> if input can be written
     */
    public boolean isWritable(final IEditorInput input) {
        // Try workspace file
        final IFile ws_file = (IFile) input.getAdapter(IFile.class);
        if (ws_file != null)
            return !ws_file.isReadOnly();

        // Fall back to non-workspace implementation
        // Try file outside of the workspace
        final File file = getFilesystemFile(input);
        if (file != null) { // File is either writable, or doesn't exist, yet, so we assume it could be written
            return file.canWrite() || !file.exists();
        }

        return false;
    }

    /**
     * Obtain output stream for editor input
     *
     * <p>
     * Depending on the implementation, the path may be
     * <ul>
     * <li>Workspace location
     * <li>Local file
     * </ul>
     *
     * <p>
     * Default implementation is limited to local files.
     *
     * @param input
     *            {@link IEditorInput}
     * @return {@link OutputStream} or <code>null</code> if input cannot be resolved
     * @throws Exception
     *             on error
     */
    public OutputStream getOutputStream(final IEditorInput input) throws Exception {
        // Try workspace file
        final IFile ws_file = (IFile) input.getAdapter(IFile.class);
        // Fall back to non-workspace implementation
        if (ws_file == null) {
            // Try file outside of the workspace
            final File file = getFilesystemFile(input);
            if (file != null)
                return new FileOutputStream(file);

            // Didn't find anything. Log adapters to aid in future extension of this code.
            final Logger logger = Logger.getLogger(getClass().getName());
            logger.fine("Cannot write to " + input.getClass().getName());
            for (String adapt : Platform.getAdapterManager().computeAdapterTypes(input.getClass()))
                logger.finer("Would adapt to " + adapt);

            return null;
        }

        // Have workspace file
        // Check write access.
        if (ws_file.isReadOnly())
            throw new Exception("File " + ws_file.getName() + " is read-only");
        // isReadOnly() only tests the file itself,
        // but parent directory can still prohibit writing.
        // --> Test by actually writing
        final InputStream dummy_data = new ByteArrayInputStream(new byte[0]);
        if (ws_file.exists())
            ws_file.setContents(dummy_data, IResource.FORCE, new NullProgressMonitor());
        else
            ws_file.create(dummy_data, IResource.FORCE, new NullProgressMonitor());

        // Caller of this method receives an output stream,
        // but IFile doesn't offer an output stream API.
        // -> Create Pipe
        // Caller of this method will write to pipe output
        final PipedOutputStream pipeout = new PipedOutputStream();

        // Data written to pipe output is read from pipe input, passed to IFile
        final PipedInputStream pipein = new PipedInputStream(pipeout);

        // To avoid deadlock, create thread that handles the IFile
        final Job writer = Job.create("Write " + input.getName(),
                (final IProgressMonitor monitor) -> {
                    try {
                        ws_file.setContents(pipein, IResource.FORCE, monitor);
                    } catch (Exception ex) { // Cannot directly notify the code which uses `pipeout`,
                                             // but closing pipes so writing code gets Exception when
                                             // it tries to write more.
                        try {
                            pipeout.close();
                            pipein.close();
                        } catch (Throwable ignored) {
                        }
                        // Notify user
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error writing " + input.getName(),
                                ex);
                        Display.getDefault().asyncExec(() -> {
                            ExceptionDetailsErrorDialog.openError(null, "Error Writing File", ex);
                        });
                    }
                    return Status.OK_STATUS;
                });
        writer.schedule();

        return pipeout;
    }

    /**
     * Attempt to locate file system file, outside of the workspace
     * 
     * @param input
     *            {@link IEditorInput}
     * @return {@link File} or <code>null</code>
     */
    private File getFilesystemFile(final IEditorInput input) {
        final IPathEditorInput patheditor = (IPathEditorInput) input.getAdapter(IPathEditorInput.class);
        if (patheditor != null)
            return getFilesystemFile(patheditor.getPath());
        return null;
    }

    /**
     * Determine file system file for path
     * 
     * @param path
     *            {@link IPath}
     * @return {@link File}
     */
    private File getFilesystemFile(final IPath path) {
        return path.toFile();
    }
}
