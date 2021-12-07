/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.Cursors;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Utility functions for resources.
 */
public class ResourceUtil {

    private static final Logger LOGGER = Logger.getLogger(ResourceUtil.class.getName());

    private static final String CURSOR_PATH = "icons/copy.gif";
    private static Cursor copyPvCursor;

    /**
     * Returns the cursor used during pv copy action.
     *
     * @return the cursor
     */
    public static Cursor getCopyPvCursor() {
        if (copyPvCursor == null) {
            var bundle = Platform.getBundle(OPIBuilderPlugin.PLUGIN_ID);
            IPath path = new Path(CURSOR_PATH);
            var url = FileLocator.find(bundle, path, null);
            try {
                var inputStream = url.openConnection().getInputStream();
                copyPvCursor = new Cursor(Display.getCurrent(), new ImageData(inputStream), 0, 0);
            } catch (IOException e) {
                copyPvCursor = Cursors.HELP;
            }
        }
        return copyPvCursor;
    }

    /**
     * Returns the absolute file represented by the <code>path</code> if such file exists. If it does not exist null is
     * returned.
     *
     * @param path
     *            the path for which the file is requested
     * @return the absolute file
     * @throws Exception
     *             in case of an error
     */
    public static File getFile(IPath path) throws Exception {
        var workspace_file = getIFileFromIPath(path);
        // Valid file should either open, or give meaningful exception
        if (workspace_file != null && workspace_file.exists()) {
            return workspace_file.getLocation().toFile().getAbsoluteFile();
        }

        // Not a workspace file. Try local file system
        var local_file = path.toFile();
        // Path URL for "file:..." so that it opens as FileInputStream
        if (local_file.getPath().startsWith("file:")) {
            local_file = new File(local_file.getPath().substring(5));
        }

        return local_file.exists() ? local_file.getAbsoluteFile() : null;
    }

    /**
     * Return the {@link InputStream} of the file that is available on the specified path.
     *
     * @param path
     *            The {@link IPath} to the file in the workspace, the local file system, or a platform URL
     * @param runInUIJob
     *            true if the task should run in UIJob, which will block UI responsiveness with a progress bar on status
     *            line. Caller must be in UI thread if this is true.
     * @return The corresponding {@link InputStream}. Never <code>null</code>
     * @throws Exception
     */
    public static InputStream pathToInputStream(IPath path) throws Exception {
        // Try workspace location
        var workspaceFile = getIFileFromIPath(path);
        // Valid file should either open, or give meaningful exception
        if (workspaceFile != null && workspaceFile.exists()) {
            return workspaceFile.getContents();
        }

        try {
            // Not a workspace file. Try local file system
            var localFile = path.toFile();
            // Path URL for "file:..." so that it opens as FileInputStream
            if (localFile.getPath().startsWith("file:")) {
                localFile = new File(localFile.getPath().substring(5));
                return new FileInputStream(localFile);
            } else if (localFile.getPath().startsWith("platform:")) {
                var url = new URL(path.toString());
                return url.openConnection().getInputStream();
            } else {
                return new FileInputStream(localFile);
            }
        } catch (Exception ex) {
            throw new Exception("Cannot open " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns a stream which can be used to read this editors input data.
     * 
     * @param editorInput
     *
     * @return a stream which can be used to read this editors input data
     */
    public static InputStream getInputStreamFromEditorInput(IEditorInput editorInput) {
        InputStream result = null;
        if (editorInput instanceof FileEditorInput) {
            try {
                result = ((FileEditorInput) editorInput).getFile().getContents();
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE, "Error while trying to access input stream of an editor.", e);
                e.printStackTrace();
            }
        } else if (editorInput instanceof FileStoreEditorInput) {
            var path = URIUtil.toPath(((FileStoreEditorInput) editorInput).getURI());
            try {
                result = new FileInputStream(path.toFile());
            } catch (FileNotFoundException e) {
                // ignore
            }
        }

        return result;
    }

    /**
     * @param path
     *            the file path
     * @return true if the file path is an existing workspace file.
     */
    public static boolean isExistingWorkspaceFile(IPath path) {
        return getIFileFromIPath(path) != null;
    }

    public static boolean isExistingLocalFile(IPath path) {
        // Not a workspace file. Try local file system
        var local_file = path.toFile();

        // // Path URL for "file:..." so that it opens as FileInputStream
        if (local_file.getPath().startsWith("file:")) {
            local_file = new File(local_file.getPath().substring(5));
        }
        return local_file.exists();
        // try
        // {
        // InputStream inputStream = new FileInputStream(local_file);
        // inputStream.close();
        // }
        // catch (Exception ex)
        // {
        // return false;
        // }
        // return true;

    }

    // TODO Check handling of "absolute" path and search path.
    // Why is AbstractOpenOPIAction resolving a path,
    // and not the OPIRuntimeDelegate itself?
    // Resolve this after settling on use of "Editor" or "View" for runtime,
    // since that will result in less code that needs to be updated.
    /**
     * Build the absolute path from the file path (without the file name part) of the widget model and the relative
     * path.
     * 
     * @param model
     *            the widget model
     * @param relativePath
     *            the relative path
     * @return the absolute path.
     */
    public static IPath buildAbsolutePath(AbstractWidgetModel model, IPath relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.isAbsolute()) {
            return relativePath;
        }
        return model.getRootDisplayModel().getOpiFilePath().removeLastSegments(1).append(relativePath);
    }

    /**
     * Build the relative path from a reference path.
     * 
     * @param refPath
     *            the reference path which does not include the file name.
     * @param fullPath
     *            the absolute full path which includes the file name.
     * @return the relative to path to refPath.
     */
    public static IPath buildRelativePath(IPath refPath, IPath fullPath) {
        if (refPath == null || fullPath == null) {
            throw new NullPointerException();
        }
        return fullPath.makeRelativeTo(refPath);
    }

    /**
     * @return
     * @throws FileNotFoundException
     */
    public static IPath getPathInEditor(IEditorInput input) {
        if (input instanceof FileEditorInput) {
            return ((FileEditorInput) input).getFile().getFullPath();
        } else if (input instanceof IPathEditorInput) {
            return ((IPathEditorInput) input).getPath();
        } else if (input instanceof FileStoreEditorInput) {
            var path = URIUtil.toPath(((FileStoreEditorInput) input).getURI());
            return path;
        }
        return null;
    }

    /**
     * Returns IPath from String.
     */
    public static IPath getPathFromString(String input) {
        if (input == null) {
            return null;
        }
        return new Path(input);
    }

    /**
     * Convert workspace path to OS system path.
     * 
     * @param path
     *            the workspace path
     * @return the corresponding system path. null if it is not exist.
     */
    public static IPath workspacePathToSysPath(IPath path) {
        var workspace = ResourcesPlugin.getWorkspace();
        var root = workspace.getRoot();
        var resource = root.findMember(path);
        if (resource != null) {
            return resource.getLocation(); // existing resource
        } else {
            return root.getFile(path).getLocation(); // for not existing resource
        }
    }

    // TODO Rename to isExistingFile, but that also affects editor, symbol widget, ..
    /**
     * If the file on path is an existing file in workspace, local file system or available URL.
     * 
     * @param absolutePath
     * @param runInUIJob
     *            true if this method should run as an UI Job. If it is true, this method must be called in UI thread.
     * @return
     */
    public static boolean isExsitingFile(IPath absolutePath, boolean runInUIJob) {
        if (isExistingWorkspaceFile(absolutePath)) {
            return true;
        }
        if (isExistingLocalFile(absolutePath)) {
            return true;
        }
        return false;
    }

    public static IEditorInput editorInputFromPath(IPath path) {
        IEditorInput editorInput = null;
        var file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        // Files outside the workspace are handled differently
        // by Eclipse.
        if (!ResourceUtil.isExistingWorkspaceFile(path) && ResourceUtil.isExistingLocalFile(path)) {
            var fileStore = EFS.getLocalFileSystem().getStore(file.getFullPath());
            editorInput = new FileStoreEditorInput(fileStore);
        } else {
            editorInput = new FileEditorInput(file);
        }
        return editorInput;
    }

    /**
     * Get screenshot image from GraphicalViewer
     * 
     * @param viewer
     *            the GraphicalViewer
     * @return the screenshot image
     */
    public static Image getScreenshotImage(GraphicalViewer viewer) {
        var gc = new GC(viewer.getControl());
        var image = new Image(Display.getDefault(), viewer.getControl().getSize().x, viewer.getControl().getSize().y);
        gc.copyArea(image, 0, 0);
        /*
         * This is a workaround for issue 2345 - empty screenshot
         * https://github.com/ControlSystemStudio/cs-studio/issues/2345
         *
         * The workaround is calling gc.copyArea twice.
         */
        gc.copyArea(image, 0, 0);
        gc.dispose();
        return image;
    }

    public static String getScreenshotFile(GraphicalViewer viewer) throws Exception {
        File file;
        // Get name for snapshot file
        file = File.createTempFile("opi", ".png");
        file.deleteOnExit();

        // Create snapshot file
        var loader = new ImageLoader();

        var image = ResourceUtil.getScreenshotImage(viewer);
        loader.data = new ImageData[] { image.getImageData() };
        image.dispose();
        loader.save(file.getAbsolutePath(), SWT.IMAGE_PNG);
        return file.getAbsolutePath();
    }

    /**
     * Get the IFile from IPath.
     * 
     * @param path
     *            Path to file in workspace
     * @return the IFile. <code>null</code> if no IFile on the path, file does not exist, internal error.
     */
    public static IFile getIFileFromIPath(IPath path) {
        try {
            var r = ResourcesPlugin.getWorkspace().getRoot().findMember(path, false);
            if (r != null && r instanceof IFile) {
                var file = (IFile) r;
                if (file.exists()) {
                    return file;
                }
            }
        } catch (Exception ex) {
            // Ignored
        }
        return null;
    }
}
