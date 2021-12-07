/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.csstudio.examples.Activator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * The action to install OPI symbol images library.
 */
public class InstallOPIImageLibraryAction extends Action implements IWorkbenchWindowActionDelegate {

    public static final String PROJECT_NAME = "OPI Image Library";
    private static final String SRC_FOLDER_TOCOPY = "./SymbolLibrary/";
    private static final String JOB_NAME = "Import OPI image library";
    private static final String TASK_NAME = "Copying OPI image library";

    @Override
    public void dispose() {
        // NOP
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // NOP
    }

    @Override
    public void run(IAction action) {
        var root = ResourcesPlugin.getWorkspace().getRoot();
        if (root.getProject(PROJECT_NAME).exists()) {
            MessageDialog.openError(null, "Failed",
                    NLS.bind(
                            "There is already a project named \"{0}\"."
                                    + "Please make sure there is no project named {0} in the workspace.",
                            PROJECT_NAME));
            return;
        }
        Job job = new Job(JOB_NAME) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    // copy the sample displays
                    var project = root.getProject(PROJECT_NAME);
                    project.create(new NullProgressMonitor());
                    project.open(new NullProgressMonitor());
                    var bundle = Platform.getBundle(Activator.PLUGIN_ID);
                    var url = FileLocator.find(bundle, new Path(SRC_FOLDER_TOCOPY), null);
                    try {
                        var directory = new File(FileLocator.toFileURL(url).getPath());
                        if (directory.isDirectory()) {
                            var files = directory.listFiles();
                            monitor.beginTask(TASK_NAME, count(files));
                            copy(files, project, monitor);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private int count(File[] files) {
        var result = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                result += count(file.listFiles());
            } else {
                result++;
            }
        }
        return result;
    }

    private void copy(File[] files, IContainer container, IProgressMonitor monitor) {
        try {
            for (File file : files) {
                monitor.subTask("Copying " + file.getName());
                if (file.isDirectory()) {
                    var folder = container.getFolder(new Path(file.getName()));
                    if (!folder.exists()) {
                        folder.create(true, true, null);
                        copy(file.listFiles(), folder, monitor);
                    }
                } else {
                    var pFile = container.getFile(new Path(file.getName()));
                    if (!pFile.exists()) {
                        pFile.create(new FileInputStream(file), true, new NullProgressMonitor());
                    }
                    monitor.internalWorked(1);
                }
            }
        } catch (Exception e) {
            MessageDialog.openError(null, "Error", NLS.bind("Error happened during copy: \n{0}.", e));
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // NOP
    }
}
