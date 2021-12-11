/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.composites;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.ui.util.ImageUtil;
import org.csstudio.ui.util.ResourceUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Workbench-level composite for choosing a resource.
 *
 * <p>
 * <b>Code is based upon <code>org.eclipse.ui.internal.ide.misc.ContainerSelectionGroup</code> in plugin
 * <code>org.eclipse.ui.ide</code>.</b>
 * </p>
 */
// TODO: Copied from org.csstudio.platform.ui. Review is needed.
public final class ResourceSelectionGroup extends Composite {

    /**
     * This action is for creating a new folder.
     *
     */
    private final class NewFolderAction extends Action {

        /**
         * The Shell.
         */
        private final Shell _shell;

        /**
         * Constructor.
         * 
         * @param shell
         *            The Shell for this Action
         */
        public NewFolderAction(Shell shell) {
            _shell = shell;
            this.setText("Create new folder");
            this.setToolTipText("Creates a new folder");
            this.setImageDescriptor(
                    ImageUtil.getInstance().getImageDescriptor(YamcsPlugin.PLUGIN_ID, "icons/new_folder.png"));
        }

        @Override
        public void run() {
            var resource = ResourcesPlugin.getWorkspace().getRoot().findMember(getFullPath());
            var buffer = new StringBuffer("Please enter the name of the folder.");
            buffer.append(" (");
            buffer.append(resource.getFullPath());
            buffer.append("/..)");
            var inputDialog = new InputDialog(_shell, "Create a new Folder", buffer.toString(), "", null);
            var ret = inputDialog.open();

            if (ret == Window.OK) {
                var folderName = inputDialog.getValue();
                if (folderName != null) {
                    if (resource instanceof IContainer) {
                        if (ResourceUtil.getInstance().createFolder((IContainer) resource,
                                folderName) == ResourceUtil.FOLDEREXISTS) {
                            MessageDialog.openInformation(_shell, "Project already exists.",
                                    "The project already exists in your workspace.");
                        }
                        refreshTree();
                    }
                }
            }
        }
    }

    private final class NewProjectAction extends Action {

        private final Shell _shell;

        public NewProjectAction(Shell shell) {
            _shell = shell;
            this.setText("Create new project");
            this.setToolTipText("Creates a new project");
            this.setImageDescriptor(
                    ImageUtil.getInstance().getImageDescriptor(YamcsPlugin.PLUGIN_ID, "icons/new_project.png"));
        }

        @Override
        public void run() {
            var inputDialog = new InputDialog(_shell, "Create a new Project", "Please enter the name of the project.",
                    "", null);
            var ret = inputDialog.open();

            if (ret == Window.OK) {
                var projectName = inputDialog.getValue();
                if (projectName != null) {
                    if (ResourceUtil.getInstance().createProject(projectName) == ResourceUtil.PROJECTEXISTS) {
                        MessageDialog.openInformation(_shell, "Project already exists.",
                                "The project already exists in your workspace.");
                    }
                    refreshTree();
                }
            }
        }
    }

    /**
     * The listener to notify of events.
     */
    private Listener _listener;

    /**
     * Show all projects by default.
     */
    private boolean _showClosedProjects = true;

    /**
     * Last selection made by user.
     */
    private IResource _selectedResource;

    /**
     * The tree widget.
     */
    private TreeViewer _treeViewer;

    /**
     * The NewFolderAction.
     */
    private Action _newFolderAction;

    /**
     * The NewProjectAction.
     */
    private Action _newProjectAction;

    /**
     * Whether to show the New Folder and New Project actions.
     */
    private boolean _showNewContainerActions;

    /**
     * Sizing constant for the width of the tree.
     */
    private static final int SIZING_SELECTION_PANE_WIDTH = 320;

    /**
     * Sizing constant for the height of the tree.
     */
    private static final int SIZING_SELECTION_PANE_HEIGHT = 300;

    /**
     * Creates a new instance of the widget.
     *
     * @param parent
     *            The parent widget of the group.
     * @param listener
     *            A listener to forward events to. Can be null if no listener is required.
     * @param fileExtensions
     *            File extensions of files to include in the tree view. Pass an empty array or <code>null</code> to show
     *            only container resources.
     * @param showNewContainerActions
     *            Whether to show the New Folder and New Project actions.
     */
    public ResourceSelectionGroup(Composite parent, Listener listener, String[] fileExtensions,
            boolean showNewContainerActions) {
        this(parent, listener, fileExtensions, null, showNewContainerActions);
    }

    /**
     * Creates a new instance of the widget.
     *
     * @param parent
     *            The parent widget of the group.
     * @param listener
     *            A listener to forward events to. Can be null if no listener is required.
     * @param fileExtensions
     *            File extensions of files to include in the tree view. Pass an empty array or <code>null</code> to show
     *            only container resources.
     * @param message
     *            The text to present to the user.
     * @param showNewContainerActions
     *            Whether to show the New Folder and New Project actions.
     */
    public ResourceSelectionGroup(Composite parent, Listener listener, String[] fileExtensions,
            String message, boolean showNewContainerActions) {
        this(parent, listener, fileExtensions, message, true, showNewContainerActions);
    }

    /**
     * Creates a new instance of the widget.
     *
     * @param parent
     *            The parent widget of the group.
     * @param listener
     *            A listener to forward events to. Can be null if no listener is required.
     * @param fileExtensions
     *            File extensions of files to include in the tree view. Pass an empty array or <code>null</code> to show
     *            only container resources.
     * @param message
     *            The text to present to the user.
     * @param showClosedProjects
     *            Whether or not to show closed projects.
     * @param showNewContainerActions
     *            Whether to show the New Folder and New Project actions.
     */
    public ResourceSelectionGroup(Composite parent, Listener listener, String[] fileExtensions,
            String message, boolean showClosedProjects, boolean showNewContainerActions) {
        this(parent, listener, fileExtensions, message, showClosedProjects, showNewContainerActions,
                SIZING_SELECTION_PANE_HEIGHT, SIZING_SELECTION_PANE_WIDTH);
    }

    /**
     * Creates a new instance of the widget.
     *
     * @param parent
     *            The parent widget of the group.
     * @param listener
     *            A listener to forward events to. Can be null if no listener is required.
     * @param fileExtensions
     *            File extensions of files to include in the tree view. Pass an empty array or <code>null</code> to show
     *            only container resources.
     * @param message
     *            The text to present to the user.
     * @param showClosedProjects
     *            Whether or not to show closed projects.
     * @param showNewContainerActions
     *            Whether to show the New Folder and New Project actions.
     * @param heightHint
     *            height hint for the drill down composite
     * @param widthHint
     *            width hint for the drill down composite
     */
    public ResourceSelectionGroup(Composite parent, Listener listener, String[] fileExtensions,
            String message, boolean showClosedProjects, boolean showNewContainerActions,
            int heightHint, int widthHint) {
        super(parent, SWT.NONE);
        _listener = listener;
        _showClosedProjects = showClosedProjects;
        _showNewContainerActions = showNewContainerActions;
        if (message != null) {
            createContents(message, fileExtensions, heightHint, widthHint);
        } else {
            createContents("Select the folder:", fileExtensions, heightHint, widthHint);
        }
    }

    /**
     * The container selection has changed in the tree view. Update the container name field value and notify all
     * listeners.
     *
     * @param resource
     *            The container that changed
     */
    public void containerSelectionChanged(IResource resource) {
        _selectedResource = resource;

        // fire an event so the parent can update its controls
        if (_listener != null) {
            var changeEvent = new Event();
            changeEvent.type = SWT.Selection;
            changeEvent.widget = this;
            _listener.handleEvent(changeEvent);
        }
    }

    public void itemDoubleClicked(IResource resource) {
        _selectedResource = resource;

        // fire an event so the parent can update its controls
        if (_listener != null) {
            var changeEvent = new Event();
            changeEvent.type = SWT.MouseDoubleClick;
            changeEvent.widget = this;
            _listener.handleEvent(changeEvent);
        }
    }

    /**
     * Creates the contents of the composite.
     *
     * @param message
     *            The text to present to the user.
     * @param fileExtensions
     *            The file extensions of files to include in the tree.
     * @param heightHint
     *            The height of the tree widget.
     * @param widthHint
     *            The width of the tree widget.
     */
    public void createContents(String message, String[] fileExtensions, int heightHint, int widthHint) {
        var layout = new GridLayout();
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        var label = new Label(this, SWT.WRAP);
        label.setText(message);
        // label.setFont(getFont());

        createTreeViewer(fileExtensions, heightHint);
        // Dialog.applyDialogFont(this);
    }

    /**
     * Returns a new drill down viewer for this dialog.
     *
     * @param fileExtensions
     *            The file extensions of files to include in the tree.
     * @param heightHint
     *            height hint for the drill down composite
     */
    protected void createTreeViewer(String[] fileExtensions, int heightHint) {
        // Create drill down.
        var drillDown = new DrillDownComposite(this, SWT.NONE);
        var spec = new GridData(SWT.FILL, SWT.FILL, true, true);
        spec.widthHint = SIZING_SELECTION_PANE_WIDTH;
        spec.heightHint = heightHint;
        drillDown.setLayoutData(spec);

        // Create tree viewer inside drill down.
        _treeViewer = new TreeViewer(drillDown, SWT.NONE);
        drillDown.setChildTree(_treeViewer);
        var cp = new WorkspaceResourceContentProvider(fileExtensions);
        cp.showClosedProjects(_showClosedProjects);
        _treeViewer.setContentProvider(cp);
        _treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        _treeViewer.setSorter(new ViewerSorter());
        _treeViewer.setUseHashlookup(true);
        _treeViewer.addSelectionChangedListener(event -> {
            var selection = (IStructuredSelection) event.getSelection();
            containerSelectionChanged((IResource) selection.getFirstElement()); // allow null
            if (_newFolderAction != null) {
                _newFolderAction.setEnabled(selection != null);
            }
        });
        _treeViewer.addDoubleClickListener(event -> {
            var selection = event.getSelection();
            if (selection instanceof IStructuredSelection) {
                var item = ((IStructuredSelection) selection).getFirstElement();
                if (item == null) {
                    return;
                }
                if (_treeViewer.getExpandedState(item)) {
                    _treeViewer.collapseToLevel(item, 1);
                } else if (_treeViewer.isExpandable(item)) {
                    _treeViewer.expandToLevel(item, 1);
                } else if (!(item instanceof IContainer)) {
                    itemDoubleClicked((IResource) item);
                }

            }
        });

        // This has to be done after the viewer has been laid out
        _treeViewer.setInput(ResourcesPlugin.getWorkspace());
        this.addNewContainerActions(drillDown.getToolBarManager());
        this.addPopupMenu(_treeViewer);

        this.setDefaultSelection(_treeViewer);
    }

    /**
     * Sets the first Element of the TreeViewer as selected Item.
     * 
     * @param viewer
     *            The TreeViewer, which selection should be set
     */
    private void setDefaultSelection(TreeViewer viewer) {
        var item = viewer.getTree().getItemCount() > 0 ? viewer.getTree().getItem(0) : null;
        if (item != null) {
            viewer.getTree().setSelection(item);
        }
    }

    /**
     * Creates the New Folder and New Project actions and adds them to the given toolbar manager.
     *
     * @param manager
     *            The ToolBarManager, where the Actions are added
     */
    private void addNewContainerActions(ToolBarManager manager) {
        if (_showNewContainerActions) {
            _newFolderAction = new NewFolderAction(this.getShell());
            _newFolderAction.setEnabled(false);
            _newProjectAction = new NewProjectAction(this.getShell());
            manager.add(new Separator());
            manager.add(_newFolderAction);
            manager.add(_newProjectAction);
            manager.update(true);
        }
    }

    /**
     * Adds a PopupMenu to the given TreeViewer.
     * 
     * @param viewer
     *            The TreeViewer, where the PopupMenu is added
     */
    private void addPopupMenu(TreeViewer viewer) {
        var popupMenu = new MenuManager();
        if (_showNewContainerActions) {
            popupMenu.add(_newFolderAction);
            popupMenu.add(_newProjectAction);
        }
        var menu = popupMenu.createContextMenu(viewer.getTree());
        viewer.getTree().setMenu(menu);
    }

    /**
     * Refreshes the Tree in an async-thread.
     */
    private void refreshTree() {
        _treeViewer.getTree().getDisplay().asyncExec(() -> _treeViewer.refresh());
    }

    /**
     * Returns the currently entered container name. Null if the field is empty. Note that the container may not exist
     * yet if the user entered a new container name in the field.
     *
     * @return IPath
     */
    public IPath getFullPath() {
        if (_selectedResource == null) {
            return null;
        }
        return _selectedResource.getFullPath();
    }

    /**
     * Gives focus to one of the widgets in the group, as determined by the group.
     */
    public void setInitialFocus() {
        _treeViewer.getTree().setFocus();
    }

    /**
     * Selects the given resource.
     *
     * @param resource
     *            the resource to select.
     */
    public void setSelectedResource(IResource resource) {
        _selectedResource = resource;

        List<IResource> itemsToExpand = new ArrayList<>();
        var parent = resource.getParent();
        while (parent != null) {
            itemsToExpand.add(0, parent);
            parent = parent.getParent();
        }
        _treeViewer.setExpandedElements(itemsToExpand.toArray());
        _treeViewer.setSelection(new StructuredSelection(resource), true);
    }

    /**
     * Selects the resource with the given path.
     *
     * @param path
     *            The path to a specific resource.
     */
    public void setSelectedResource(IPath path) {
        IContainer workspace = ResourcesPlugin.getWorkspace().getRoot();
        var res = workspace.findMember(path);
        if (res != null) {
            setSelectedResource(res);
        }
    }
}
