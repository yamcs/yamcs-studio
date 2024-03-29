/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.csstudio.ui.util.composites.ResourceAndContainerGroup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Standard main page for a wizard that creates a file resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * <p>
 * Subclasses may override
 * <ul>
 * <li><code>getInitialContents</code></li>
 * <li><code>getNewFileLabel</code></li>
 * </ul>
 * Subclasses may extend
 * <ul>
 * <li><code>handleEvent</code></li>
 * </ul>
 * <p>
 * <b>Code is based upon <code>org.eclipse.ui.dialogs.WizardNewFileCreationPage</code> in plugin
 * <code>org.eclipse.ui.ide</code>.</b>
 */

// TODO: Copied from org.csstudio.platform.ui. Review is needed.

public class WizardNewFileCreationPage extends WizardPage implements Listener {
    /**
     * The current resource selection.
     */
    private IStructuredSelection _currentSelection;

    /**
     * Cache of newly-created file.
     */
    private IFile _newFile;

    /**
     * Target container selection group.
     */
    private ResourceAndContainerGroup _resourceGroup;

    /**
     * Initial value store for the file name.
     */
    private String _initialFileName;

    /**
     * Initial value store for the target container.
     */
    private IPath _initialContainerFullPath;

    /**
     * Flag that signals whether the target project can be chosen or not.
     */
    private boolean _canChooseProject;

    /**
     * Creates a new file creation wizard page. If the initial resource selection contains exactly one container
     * resource then it will be used as the default container resource.
     *
     * @param pageName
     *            the name of the page
     * @param selection
     *            the current resource selection
     */
    public WizardNewFileCreationPage(String pageName, IStructuredSelection selection) {
        this(pageName, selection, true);
    }

    /**
     * Creates a new file creation wizard page. If the initial resource selection contains exactly one container
     * resource then it will be used as the default container resource.
     *
     * @param pageName
     *            the name of the page
     * @param selection
     *            the current resource selection
     * @param canChooseProject
     *            flag to allow the selection of the target project
     */
    public WizardNewFileCreationPage(String pageName, IStructuredSelection selection, boolean canChooseProject) {
        super(pageName);
        setPageComplete(false);
        _currentSelection = selection;
        _canChooseProject = canChooseProject;
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        // top level group
        var topLevel = new Composite(parent, SWT.NONE);
        topLevel.setLayout(new GridLayout());
        topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        topLevel.setFont(parent.getFont());

        // resource and container group
        _resourceGroup = new ResourceAndContainerGroup(topLevel, this, getNewFileLabel(), "file", false, 250);

        if (getFileExtension() != null) {
            _resourceGroup.setFileExtension(getFileExtension());
        }

        _resourceGroup.setAllowExistingResources(false);
        initialPopulateContainerNameField();
        if (_initialFileName != null) {
            _resourceGroup.setResource(_initialFileName);
        }

        _resourceGroup.setContainerSelectionGroupEnabled(_canChooseProject);

        validatePage();
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(topLevel);
    }

    /**
     * Creates a file resource given the file handle and contents.
     *
     * @param fileHandle
     *            the file handle to create a file resource with
     * @param contents
     *            the initial contents of the new file resource, or <code>null</code> if none (equivalent to an empty
     *            stream)
     * @exception CoreException
     *                if the operation fails
     */
    protected void createFile(IFile fileHandle, InputStream contents) throws CoreException {
        var inputStream = contents;

        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(new byte[0]);
        }

        try {
            var path = fileHandle.getFullPath();
            var root = ResourcesPlugin.getWorkspace().getRoot();
            var numSegments = path.segmentCount();
            if (numSegments > 2 && !root.getFolder(path.removeLastSegments(1)).exists()) {
                // If the direct parent of the path doesn't exist, try to
                // create the
                // necessary directories.
                for (var i = numSegments - 2; i > 0; i--) {
                    var folder = root.getFolder(path.removeLastSegments(i));
                    if (!folder.exists()) {
                        folder.create(false, true, null);
                    }
                }
            }
            fileHandle.create(inputStream, false, null);
        } catch (CoreException e) {
            // If the file already existed locally, just refresh to get contents
            if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED) {
                fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
            } else {
                throw e;
            }
        }
    }

    /**
     * Creates a file resource handle for the file with the given workspace path. This method does not create the file
     * resource; this is the responsibility of <code>createFile</code>.
     *
     * @param filePath
     *            the path of the file resource to create a handle for
     * @return the new file resource handle
     */
    protected final IFile createFileHandle(IPath filePath) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
    }

    /**
     * Creates a new file resource in the selected container and with the selected name. Creates any missing resource
     * containers along the path; does nothing if the container resources already exist.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
     * Finish button implies that all controls on on this page currently contain valid values.
     * </p>
     * <p>
     * Note that this page caches the new file once it has been successfully created; subsequent invocations of this
     * method will answer the same file resource without attempting to create it again.
     * </p>
     * <p>
     * This method should be called within a workspace modify operation since it creates resources.
     * </p>
     *
     * @return the created file resource, or <code>null</code> if the file was not created
     */
    public final IFile createNewFile() {
        if (_newFile != null) {
            return _newFile;
        }

        // create the new file and cache it if successful

        var containerPath = _resourceGroup.getContainerFullPath();
        var newFilePath = containerPath.append(_resourceGroup.getResource());
        var newFileHandle = createFileHandle(newFilePath);
        var initialContents = getInitialContents();

        try {
            createFile(newFileHandle, initialContents);
        } catch (CoreException e) {
            ErrorDialog.openError(getContainer().getShell(), "Creation Problems", null, e.getStatus());

            newFileHandle = null;
        }

        _newFile = newFileHandle;

        return _newFile;
    }

    /**
     * Returns the current full path of the containing resource as entered or selected by the user, or its anticipated
     * initial value.
     *
     * @return the container's full path, anticipated initial value, or <code>null</code> if no path is known
     */
    public final IPath getContainerFullPath() {
        return _resourceGroup.getContainerFullPath();
    }

    /**
     * Returns the current file name as entered by the user, or its anticipated initial value.
     *
     * @return the file name, its anticipated initial value, or <code>null</code> if no file name is known
     */
    public String getFileName() {
        if (_resourceGroup == null) {
            return _initialFileName;
        }

        return _resourceGroup.getResource();
    }

    /**
     * Returns a stream containing the initial contents to be given to new file resource instances. <b>Subclasses</b>
     * may wish to override. This default implementation provides no initial contents.
     *
     * @return initial contents to be given to new file resource instances
     */
    protected InputStream getInitialContents() {
        return null;
    }

    /**
     * Returns the label to display in the file name specification visual component group.
     * <p>
     * Subclasses may reimplement.
     * </p>
     *
     * @return the label to display in the file name specification visual component group
     */
    protected String getNewFileLabel() {
        return "File na&me:";
    }

    /**
     * The <code>WizardNewFileCreationPage</code> implementation of this <code>Listener</code> method handles all events
     * and enablements for controls on this page. Subclasses may extend.
     */
    @Override
    public void handleEvent(Event event) {
        setPageComplete(validatePage());
    }

    /**
     * Sets the initial contents of the container name entry field, based upon either a previously-specified initial
     * value or the ability to determine such a value.
     */
    protected void initialPopulateContainerNameField() {
        if (_initialContainerFullPath != null) {
            _resourceGroup.setContainerFullPath(_initialContainerFullPath);
        } else {
            Iterator<?> it = _currentSelection.iterator();
            if (it.hasNext()) {
                Object object = it.next();
                IResource selectedResource = null;
                if (object instanceof IResource) {
                    selectedResource = (IResource) object;
                } else if (object instanceof IAdaptable) {
                    selectedResource = ((IAdaptable) object).getAdapter(IResource.class);
                }
                if (selectedResource != null) {
                    if (selectedResource.getType() == IResource.FILE) {
                        selectedResource = selectedResource.getParent();
                    }
                    if (selectedResource.isAccessible()) {
                        _resourceGroup.setContainerFullPath(selectedResource.getFullPath());
                    }
                }
            }
        }
    }

    /**
     * Sets the value of this page's container name field, or stores it for future use if this page's controls do not
     * exist yet.
     *
     * @param path
     *            the full path to the container
     */
    public void setContainerFullPath(IPath path) {
        if (_resourceGroup == null) {
            _initialContainerFullPath = path;
        } else {
            _resourceGroup.setContainerFullPath(path);
        }
    }

    /**
     * Sets the value of this page's file name field, or stores it for future use if this page's controls do not exist
     * yet.
     *
     * @param value
     *            new file name
     */
    public void setFileName(String value) {
        if (_resourceGroup == null) {
            _initialFileName = value;
        } else {
            _resourceGroup.setResource(value);
        }
    }

    /**
     * Returns whether this page's controls currently all contain valid values.
     *
     * @return <code>true</code> if all controls are valid, and <code>false</code> if at least one is invalid
     */
    protected final boolean validatePage() {
        var valid = true;

        if (!_resourceGroup.areAllValuesValid()) {
            // if blank name then fail silently
            if (_resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_RESOURCE_EMPTY
                    || _resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_CONTAINER_EMPTY) {
                setMessage(_resourceGroup.getProblemMessage());
                setErrorMessage(null);
            } else {
                setErrorMessage(_resourceGroup.getProblemMessage());
            }
            valid = false;
        }

        if (valid) {
            setMessage(null);
            setErrorMessage(null);
        }
        return valid;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            _resourceGroup.setFocus();
        }
    }

    public String getFileExtension() {
        return null;
    }
}
