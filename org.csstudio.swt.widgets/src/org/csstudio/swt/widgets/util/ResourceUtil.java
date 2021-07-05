/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.swt.widgets.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;

/**
 * Utility functions for resources.
 * 
 * @author Xihui Chen
 *
 */
public class ResourceUtil {

    private static final LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder()
            .recordStats()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build(new CacheLoader<String, byte[]>() {
                @Override
                public byte[] load(String file) throws IOException, Exception {
                    return ByteStreams.toByteArray(pathToInputStream(file));
                }
            });

    /**
     * Convert workspace path to OS system path.
     * 
     * @param path
     *            the workspace path
     * @return the corresponding system path. null if it is not exist.
     */
    public static IPath workspacePathToSysPath(IPath path) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IResource resource = root.findMember(path);
        if (resource != null) {
            return resource.getLocation(); // existing resource
        } else {
            return root.getFile(path).getLocation(); // for not existing resource
        }
    }

    /**
     * Get inputstream from path. Run in a Job. The uiTask is responsible for closing the inputstream
     * 
     * @param path
     *            the path to load
     * @param uiTask
     *            the task to be executed in UI thread after path is loaded.
     * @param jobName
     *            name of the job
     * @param errorHandler
     *            the handler to handle IO exception.
     */
    public static void pathToInputStreamInJob(final IPath path,
            final AbstractInputStreamRunnable uiTask, final String jobName,
            final IJobErrorHandler errorHandler) {
        final Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        Job job = new Job(jobName) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Connecting to " + path, IProgressMonitor.UNKNOWN);
                try {
                    InputStream inputStream = workspaceFileToInputStream(path);
                    if (inputStream == null) {
                        inputStream = new ByteArrayInputStream(cache.getUnchecked(path.toPortableString()));
                        // System.out.println("hit: "+cache.stats().hitCount()+
                        // ", miss: "+cache.stats().missCount()+
                        // ", load time: "+cache.stats().totalLoadTime()+
                        // ", entries: "+cache.asMap().size());
                    }
                    uiTask.setInputStream(inputStream);
                    display.asyncExec(uiTask);
                } catch (Exception e) {
                    errorHandler.handleError(e);
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private static InputStream workspaceFileToInputStream(IPath path) {
        // Try workspace location
        final IFile workspace_file = getIFileFromIPath(path);
        // Valid file should either open, or give meaningful exception
        if (workspace_file != null && workspace_file.exists()) {
            try {
                return workspace_file.getContents();
            } catch (CoreException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get the IFile from IPath.
     * 
     * @param path
     *            Path to file in workspace
     * @return the IFile. <code>null</code> if no IFile on the path, file does not exist, internal error.
     */
    private static IFile getIFileFromIPath(final IPath path) {
        try {
            final IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(
                    path, false);
            if (r != null && r instanceof IFile) {
                final IFile file = (IFile) r;
                if (file.exists()) {
                    return file;
                }
            }
        } catch (Exception ex) {
            // Ignored
        }
        return null;
    }

    /**
     * Return the {@link InputStream} of the file that is available on the specified path. The caller is responsible for
     * closing inputstream.
     *
     * @param path
     *            Path in local file system, or a URL (http:, https:, ftp:, file:, platform:)
     * @return The corresponding {@link InputStream}. Never <code>null</code>
     * @throws Exception
     */
    public static InputStream pathToInputStream(final String path) throws Exception {
        // Not a workspace file. Try local file system
        File local_file = new File(path);
        // Path URL for "file:..." so that it opens as FileInputStream
        if (local_file.getPath().startsWith("file:")) {
            local_file = new File(local_file.getPath().substring(5));
        }
        String urlString;
        try {
            return new FileInputStream(local_file);
        } catch (Exception ex) {
            // Could not open as local file.
            // Does it look like a URL?
            // TODO:
            // Eclipse Path collapses "//" into "/", revert that: Is this true?
            // Need test on Mac.
            urlString = path.toString();
            // if(!urlString.startsWith("platform") && !urlString.contains("://"))
            // urlString = urlString.replaceFirst(":/", "://");
            // Does it now look like a URL? If not, report the original local
            // file problem
            if (!isURL(urlString)) {
                throw new Exception("Cannot open " + ex.getMessage(), ex);
            }
        }

        // Must be a URL
        // Allow URLs with spaces. Ideally, the URL class would handle this?
        urlString = urlString.replaceAll(" ", "%20");
        URI uri = new URI(urlString);
        final URL url = uri.toURL();
        return openURLStream(url);
    }

    private static InputStream openURLStream(final URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(5000);
        return connection.getInputStream();
    }

    /**
     * Check if a URL is actually a URL
     * 
     * @param url
     *            Possible URL
     * @return <code>true</code> if considered a URL
     */
    public static boolean isURL(final String url) {
        return url.contains(":/");
    }

    // /**
    // * Return the {@link InputStream} of the file that is available on the
    // * specified path.
    // *
    // * @param path
    // * The {@link IPath} to the file
    // *
    // * @return The corresponding {@link InputStream} or null
    // * @throws Exception
    // */
    // public static InputStream pathToInputStream(final IPath path) throws Exception{
    // InputStream result = null;
    //
    // IResource r = null;
    // try {
    // // try workspace
    // r = ResourcesPlugin.getWorkspace().getRoot().findMember(
    // path, false);
    // if (r!= null && r instanceof IFile) {
    // result = ((IFile) r).getContents();
    // return result;
    // }else
    // throw new Exception();
    // } catch (Exception e) {
    // // try from local file system
    // try {
    // result = new FileInputStream(path.toFile());
    // if(result != null)
    // return result;
    // else
    // throw new Exception();
    // } catch (Exception e1) {
    // try {
    // //try from URL
    // String urlString = path.toString();
    // if(!urlString.contains("://"))
    // urlString = urlString.replaceFirst(":/", "://");
    // URL url = new URL(urlString);
    // result = url.openStream();
    // return result;
    // } catch (Exception e2) {
    // throw new Exception("This exception includes three sub-exceptions:\n"+
    // e+ "\n" + e1 + "\n" + e2);
    // }
    // }
    // }
    // }
}
