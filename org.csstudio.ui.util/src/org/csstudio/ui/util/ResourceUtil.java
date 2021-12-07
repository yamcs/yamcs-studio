/********************************************************************************
 * Copyright (c) 2006 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * This class is for creating new IResource-objects.
 */

// TODO: Copied from org.csstudio.platform.ui. Review is needed.
public final class ResourceUtil {

    /**
     * Result identifier: Okay.
     */
    public static final int OK = 0;
    /**
     * Result identifier: An error occured.
     */
    public static final int ERROROCCURED = 1;
    /**
     * Result identifier: Name was NULL.
     */
    public static final int NAMEWASNULL = 2;
    /**
     * Result identifier: Folder exists.
     */
    public static final int FOLDEREXISTS = 3;
    /**
     * Result identifier: Project exists.
     */
    public static final int PROJECTEXISTS = 4;

    /**
     * The instance of this class.
     */
    private static ResourceUtil _instance;

    /**
     * Construktor.
     */
    private ResourceUtil() {
    }

    /**
     * Delivers the instance of this class.
     *
     * @return IResourceUtil
     */
    public static ResourceUtil getInstance() {
        if (_instance == null) {
            _instance = new ResourceUtil();
        }
        return _instance;
    }

    /**
     * Creates a new Folder in the parentContainer.
     * 
     * @param parentContainer
     *            The IContainer, where the new folder is built in
     * @param folderName
     *            The name of the folder
     * @return int The result-status
     */
    public int createFolder(IContainer parentContainer, String folderName) {
        if (folderName != null && folderName.trim().length() > 0) {
            var folder = parentContainer.getFolder(new Path(folderName));
            if (folder.exists()) {
                return FOLDEREXISTS;
            } else {
                try {
                    folder.create(true, true, null);
                } catch (CoreException e) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", e);
                    return ERROROCCURED;
                }
            }
            return OK;
        }
        return NAMEWASNULL;
    }

    /**
     * Creates a new Project.
     * 
     * @param projectName
     *            The name of the project
     * @return int The result-status
     */
    public int createProject(String projectName) {
        if (projectName != null && projectName.trim().length() > 0) {
            var project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project.exists()) {
                return PROJECTEXISTS;
            } else {
                try {
                    project.create(null);
                    project.open(null);
                } catch (CoreException e) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "", e);
                    return ERROROCCURED;
                }
            }
            return OK;
        }
        return NAMEWASNULL;
    }

}
