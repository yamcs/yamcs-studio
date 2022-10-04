/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.yamcs.client.storage.ObjectId;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class ResourceUtil {

    private static final LoadingCache<String, byte[]> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES).maximumSize(1000).build(new CacheLoader<String, byte[]>() {
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
        var workspace = ResourcesPlugin.getWorkspace();
        var root = workspace.getRoot();
        var resource = root.findMember(path);
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
    public static void pathToInputStreamInJob(String path, AbstractInputStreamRunnable uiTask, String jobName,
            IJobErrorHandler errorHandler) {
        var display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        var job = new Job(jobName) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Connecting to " + path, IProgressMonitor.UNKNOWN);
                try {
                    InputStream inputStream = null;
                    if (!path.contains("://")) {
                        inputStream = workspaceFileToInputStream(Path.fromPortableString(path));
                    }
                    if (inputStream == null) {
                        inputStream = new ByteArrayInputStream(cache.getUnchecked(path));
                    }
                    uiTask.setInputStream(inputStream);
                    display.asyncExec(uiTask);
                } catch (UncheckedExecutionException e) {
                    errorHandler.handleError(e.getCause());
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
        var workspace_file = getIFileFromIPath(path);
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
    private static IFile getIFileFromIPath(IPath path) {
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

    /**
     * Return the {@link InputStream} of the file that is available on the specified path. The caller is responsible for
     * closing inputstream.
     *
     * @param path
     *            Path in local file system, or a URL (http:, https:, ftp:, file:, platform:)
     * @return The corresponding {@link InputStream}. Never <code>null</code>
     * @throws Exception
     */
    public static InputStream pathToInputStream(String path) throws Exception {
        if (path.startsWith("ys://")) {
            var id = ObjectId.parseURL(path);
            var storageClient = YamcsPlugin.getStorageClient();
            if (storageClient == null) {
                throw new IOException("Not connected");
            }
            var obj = storageClient.downloadObject(id).get(5, TimeUnit.SECONDS);
            return new ByteArrayInputStream(obj);
        } else {
            var localFile = new File(path);
            if (localFile.getPath().startsWith("file:")) {
                localFile = new File(localFile.getPath().substring(5));
            }
            String urlString;
            try {
                return new FileInputStream(localFile);
            } catch (Exception ex) {
                urlString = path.toString();
                if (!urlString.contains(":/")) {
                    throw new Exception("Cannot open " + ex.getMessage(), ex);
                }
            }

            // Must be a URL
            // Allow URLs with spaces. Ideally, the URL class would handle this?
            urlString = urlString.replaceAll(" ", "%20");
            var uri = new URI(urlString);
            var url = uri.toURL();
            var connection = url.openConnection();
            connection.setReadTimeout(5000);
            return connection.getInputStream();
        }
    }
}
