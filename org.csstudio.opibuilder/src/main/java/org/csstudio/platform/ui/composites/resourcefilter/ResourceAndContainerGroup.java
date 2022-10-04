/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.platform.ui.composites.resourcefilter;

import org.csstudio.ui.util.composites.ResourceSelectionGroup;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.fieldassist.FieldAssistColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Workbench-level composite for resource and container specification by the user. Services such as field validation are
 * performed by the group. The group can be configured to accept existing resources, or only new resources.
 *
 * <p>
 * <b>Code is based upon <code>org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup</code> in plugin
 * <code>org.eclipse.ui.ide</code>.</b>
 * </p>
 */
// TODO: Copied from org.csstudio.platform.ui.
@SuppressWarnings("deprecation")
public final class ResourceAndContainerGroup implements Listener {
    /**
     * Problem identifier: No problem.
     */
    public static final int PROBLEM_NONE = 0;

    /**
     * Problem identifier: Empty resource.
     */
    public static final int PROBLEM_RESOURCE_EMPTY = 1;

    /**
     * Problem identifier: Resource already exists.
     */
    public static final int PROBLEM_RESOURCE_EXIST = 2;

    /**
     * Problem identifier: Path is invalid.
     */
    public static final int PROBLEM_PATH_INVALID = 4;

    /**
     * Problem identifier: Container is empty.
     */
    public static final int PROBLEM_CONTAINER_EMPTY = 5;

    /**
     * Problem identifier: Project does not exist.
     */
    public static final int PROBLEM_PROJECT_DOES_NOT_EXIST = 6;

    /**
     * Problem identifier: Invalid resource name.
     */
    public static final int PROBLEM_NAME_INVALID = 7;

    /**
     * Problem identifier: Path os occupied.
     */
    public static final int PROBLEM_PATH_OCCUPIED = 8;

    /**
     * The client to notify of changes.
     */
    private Listener _client;

    /**
     * Whether to allow existing resources.
     */
    private boolean _allowExistingResources = false;

    /**
     * Resource type (file, folder, project).
     */
    private String _resourceType = "resource";

    /**
     * Show closed projects in the tree, by default.
     */
    private boolean _showClosedProjects = true;

    /**
     * Problem indicator.
     */
    private String _problemMessage = "";

    /**
     * The default file extension.
     */
    private String _fileExtension = "";

    /**
     * Problem type.
     */
    private int _problemType = PROBLEM_NONE;

    /**
     * The container selection group.
     */
    private ResourceSelectionGroup _containerGroup;

    /**
     * The name of the resource.
     */
    private Text _resourceNameField;

    /**
     * The full path to the resource.
     */
    private Label _fullPathLabel;

    /**
     * Sizing constand for the text field width.
     */
    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    /**
     * Create an instance of the group to allow the user to enter/select a container and specify a resource name.
     *
     * @param parent
     *            composite widget to parent the group
     * @param client
     *            object interested in changes to the group's fields value
     * @param resourceFieldLabel
     *            label to use in front of the resource name field
     * @param resourceType
     *            one word, in lowercase, to describe the resource to the user (file, folder, project) height hint for
     *            the container selection widget group
     */
    public ResourceAndContainerGroup(Composite parent, Listener client, String resourceFieldLabel,
            String resourceType) {
        this(parent, client, resourceFieldLabel, resourceType, true);
    }

    /**
     * Create an instance of the group to allow the user to enter/select a container and specify a resource name.
     *
     * @param parent
     *            composite widget to parent the group
     * @param client
     *            object interested in changes to the group's fields value
     * @param resourceFieldLabel
     *            label to use in front of the resource name field
     * @param resourceType
     *            one word, in lowercase, to describe the resource to the user (file, folder, project)
     * @param showClosedProjects
     *            whether or not to show closed projects height hint for the container selection widget group
     */
    public ResourceAndContainerGroup(Composite parent, Listener client, String resourceFieldLabel, String resourceType,
            boolean showClosedProjects) {
        this(parent, client, resourceFieldLabel, resourceType, showClosedProjects, SWT.DEFAULT);
    }

    /**
     * Create an instance of the group to allow the user to enter/select a container and specify a resource name.
     *
     * @param parent
     *            composite widget to parent the group
     * @param client
     *            object interested in changes to the group's fields value
     * @param resourceFieldLabel
     *            label to use in front of the resource name field
     * @param resourceType
     *            one word, in lowercase, to describe the resource to the user (file, folder, project)
     * @param showClosedProjects
     *            whether or not to show closed projects
     * @param heightHint
     *            height hint for the container selection widget group
     */
    public ResourceAndContainerGroup(Composite parent, Listener client, String resourceFieldLabel, String resourceType,
            boolean showClosedProjects, int heightHint) {
        _resourceType = resourceType;
        _showClosedProjects = showClosedProjects;
        createContents(parent, resourceFieldLabel, heightHint);
        _client = client;
    }

    /**
     * Returns a boolean indicating whether all controls in this group contain valid values.
     *
     * @return boolean
     */
    public boolean areAllValuesValid() {
        return _problemType == PROBLEM_NONE;
    }

    /**
     * Creates this object's visual components.
     *
     * @param parent
     *            org.eclipse.swt.widgets.Composite
     * @param heightHint
     *            height hint for the container selection widget group
     * @param resourceLabelString
     *            resource label text.
     */
    private void createContents(Composite parent, String resourceLabelString, int heightHint) {

        // Font font = parent.getFont();
        // server name group
        var composite = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // composite.setFont(font);

        // container group
        if (heightHint == SWT.DEFAULT) {
            _containerGroup = new ResourceSelectionGroup(composite, this, null, null, _showClosedProjects);
        } else {
            _containerGroup = new ResourceSelectionGroup(composite, this, null, null, _showClosedProjects, true,
                    heightHint, SIZING_TEXT_FIELD_WIDTH);
        }

        // resource name group
        var nameGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        nameGroup.setLayout(layout);
        nameGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        // nameGroup.setFont(font);

        var label = new Label(nameGroup, SWT.NONE);
        label.setText(resourceLabelString);
        // label.setFont(font);

        // resource name entry field
        _resourceNameField = new Text(nameGroup, SWT.BORDER);
        _resourceNameField.addListener(SWT.Modify, this);
        var data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        _resourceNameField.setLayoutData(data);
        _resourceNameField.setBackground(FieldAssistColors.getRequiredFieldBackgroundColor(_resourceNameField));

        // full path
        label = new Label(nameGroup, SWT.NONE);
        label.setText("Full path:");
        _fullPathLabel = new Label(nameGroup, SWT.NONE | SWT.WRAP);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        _fullPathLabel.setLayoutData(data);
        refreshFullPath();
        validateControls();
    }

    /**
     * Returns the path of the currently selected container or null if no container has been selected. Note that the
     * container may not exist yet if the user entered a new container name in the field.
     *
     * @return The full path of the selected container.
     */
    public IPath getContainerFullPath() {
        return _containerGroup.getFullPath();
    }

    /**
     * Returns an error message indicating the current problem with the value of a control in the group, or an empty
     * message if all controls in the group contain valid values.
     *
     * @return java.lang.String
     */
    public String getProblemMessage() {
        return _problemMessage;
    }

    /**
     * Returns the type of problem with the value of a control in the group.
     *
     * @return one of the PROBLEM_* constants
     */
    public int getProblemType() {
        return _problemType;
    }

    /**
     * Returns a string that is the path of the currently selected container. Returns an empty string if no container
     * has been selected.
     *
     * @return The entered resource name.
     */
    public String getResource() {
        return getResourceNameWithExtension();
    }

    /**
     * Handles events for all controls in the group.
     *
     * @param e
     *            org.eclipse.swt.widgets.Event
     */
    @Override
    public void handleEvent(Event e) {
        validateControls();
        refreshFullPath();
        if (_client != null) {
            _client.handleEvent(e);
        }
    }

    /**
     * Sets the flag indicating whether existing resources are permitted.
     *
     * @param value
     *            Flag that signals of it is allows to enter the names of already existing resources.
     */
    public void setAllowExistingResources(boolean value) {
        _allowExistingResources = value;
    }

    /**
     * Sets the value of this page's container.
     *
     * @param path
     *            Full path to the container.
     */
    public void setContainerFullPath(IPath path) {
        var initial = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (initial != null) {
            if (!(initial instanceof IContainer)) {
                initial = initial.getParent();
            }
            _containerGroup.setSelectedResource(initial);
        }
        validateControls();
    }

    /**
     * Gives focus to the resource name field and selects its contents.
     */
    public void setFocus() {
        // select the whole resource name.
        _resourceNameField.setSelection(0, _resourceNameField.getText().length());
        _resourceNameField.setFocus();
    }

    /**
     * Sets the value of this page's resource name.
     *
     * @param value
     *            new value
     */
    public void setResource(String value) {
        _resourceNameField.setText(value);
        validateControls();
    }

    /**
     * Returns a <code>boolean</code> indicating whether a container name represents a valid container resource in the
     * workbench. An error message is stored for future reference if the name does not represent a valid container.
     *
     * @return <code>boolean</code> indicating validity of the container name
     */
    private boolean validateContainer() {
        var path = _containerGroup.getFullPath();
        if (path == null) {
            _problemType = PROBLEM_CONTAINER_EMPTY;
            _problemMessage = "The folder is empty.";
            return false;
        }
        var workspace = ResourcesPlugin.getWorkspace();
        var projectName = path.segment(0);
        if (projectName == null || !workspace.getRoot().getProject(projectName).exists()) {
            _problemType = PROBLEM_PROJECT_DOES_NOT_EXIST;
            _problemMessage = "The specified project does not exist.";
            return false;
        }
        // path is invalid if any prefix is occupied by a file
        var root = workspace.getRoot();
        while (path.segmentCount() > 1) {
            if (root.getFile(path).exists()) {
                _problemType = PROBLEM_PATH_OCCUPIED;
                _problemMessage = "A file already exists at that location: " + path.makeRelative() + ".";
                return false;
            }
            path = path.removeLastSegments(1);
        }
        return true;
    }

    /**
     * Validates the values for each of the group's controls. If an invalid value is found then a descriptive error
     * message is stored for later reference. Returns a boolean indicating the validity of all of the controls in the
     * group.
     *
     * @return True, if all values are valid.
     */
    private boolean validateControls() {
        // don't attempt to validate controls until they have been created
        if (_containerGroup == null) {
            return false;
        }
        _problemType = PROBLEM_NONE;
        _problemMessage = "";

        if (!validateContainer() || !validateResourceName()) {
            return false;
        }

        var path = _containerGroup.getFullPath().append(getResourceNameWithExtension());
        return validateFullResourcePath(path);
    }

    /**
     * Returns a <code>boolean</code> indicating whether the specified resource path represents a valid new resource in
     * the workbench. An error message is stored for future reference if the path does not represent a valid new
     * resource path.
     *
     * @param resourcePath
     *            the path to validate
     * @return <code>boolean</code> indicating validity of the resource path
     */
    private boolean validateFullResourcePath(IPath resourcePath) {
        var workspace = ResourcesPlugin.getWorkspace();

        var result = workspace.validatePath(resourcePath.toString(), IResource.FOLDER);
        if (!result.isOK()) {
            _problemType = PROBLEM_PATH_INVALID;
            _problemMessage = result.getMessage();
            return false;
        }

        if (!_allowExistingResources && (workspace.getRoot().getFolder(resourcePath).exists()
                || workspace.getRoot().getFile(resourcePath).exists())) {
            _problemType = PROBLEM_RESOURCE_EXIST;
            _problemMessage = "The same name already exists.";
            return false;
        }
        return true;
    }

    /**
     * Returns a <code>boolean</code> indicating whether the resource name rep- resents a valid resource name in the
     * workbench. An error message is stored for future reference if the name does not represent a valid resource name.
     *
     * @return <code>boolean</code> indicating validity of the resource name
     */
    private boolean validateResourceName() {
        var resourceName = getResourceNameWithExtension();

        if (resourceName.equals("")) {
            _problemType = PROBLEM_RESOURCE_EMPTY;
            _problemMessage = "The '" + _resourceType + "' name is empty.";
            return false;
        }

        if (!(new Path("")).isValidPath(resourceName)) {
            _problemType = PROBLEM_NAME_INVALID;
            _problemMessage = "'" + resourceName + "' is not a valid file name.";
            return false;
        }
        return true;
    }

    /**
     * Refreshes the displayed full path of the resource.
     */
    private void refreshFullPath() {
        var buffer = new StringBuffer();
        if (_containerGroup.getFullPath() != null) {
            buffer.append(ResourcesPlugin.getWorkspace().getRoot().getLocation());
            buffer.append(_containerGroup.getFullPath());
            buffer.append("/");
            if (_resourceNameField.getText() != null && _resourceNameField.getText().trim().length() > 0) {
                buffer.append(getResourceNameWithExtension());
            }
        }
        _fullPathLabel.setText(buffer.toString());
        var comp = _fullPathLabel.getParent().getParent();
        if (comp != null) {
            comp.layout();
        }
    }

    /**
     * Return the resource name including its eventually set file extension.
     *
     * @return The resource name including its eventually set file extension.
     */
    private String getResourceNameWithExtension() {
        var result = _resourceNameField.getText();

        if ((_fileExtension != null) && (_fileExtension.length() > 0)) {
            result = result + "." + _fileExtension;
        }

        return result;
    }

    /**
     * Return the file extension.
     *
     * @return The file extension
     */
    public String getFileExtension() {
        return _fileExtension;
    }

    /**
     * Set the file extension.
     *
     * @param fileExtension
     *            The file extension to set
     */
    public void setFileExtension(String fileExtension) {
        _fileExtension = fileExtension;
    }

    /**
     * Set the embedded container selection group to enabled/disabled.
     *
     * @param enabled
     *            true for enabled, false for diabled.
     */
    public void setContainerSelectionGroupEnabled(boolean enabled) {
        if (_containerGroup != null) {
            _containerGroup.setEnabled(enabled);
        }
    }
}
