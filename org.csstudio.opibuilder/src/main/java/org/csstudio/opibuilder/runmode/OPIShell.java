/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.runmode;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.actions.PrintDisplayAction;
import org.csstudio.opibuilder.actions.RefreshOPIAction;
import org.csstudio.opibuilder.datadefinition.NotImplementedException;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.WidgetEditPartFactory;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;

/**
 * An OPIShell is a CS-Studio OPI presented in an SWT shell, which allows more free integration with the host operating
 * system. In most ways it behaves like an OPIView.
 *
 * All OPIShells are maintained in a static set within this class. To maintain a cache of all shells, construction is
 * limited to a static method. The private constructor means that this class cannot be extended.
 *
 * In order for the OPIShell to be integrated with Eclipse functionality, in particular the right-click context menu, it
 * needs to be registered against an existing IViewPart.
 */
public final class OPIShell implements IOPIRuntime {

    // Estimates for the size of a window border, for how much
    // bigger to make a shell than the size of its contents.
    private static final int WINDOW_BORDER_X = 30;
    private static final int WINDOW_BORDER_Y = 30;

    private static Logger log = OPIBuilderPlugin.getLogger();
    public static final String OPI_SHELLS_CHANGED_ID = "org.csstudio.opibuilder.opiShellsChanged";
    // The active OPIshell, or null if no OPIShell is active
    private static OPIShell activeShell = null;
    // Cache of open OPI shells in order of opening.
    private static final Set<OPIShell> openShells = new LinkedHashSet<>();
    // The view against which the context menu is registered.
    private IViewPart view;

    // Variables that do not change for any shell.
    private final Image icon;
    private final Shell shell;
    private final ActionRegistry actionRegistry;
    private final GraphicalViewer viewer;
    // Variables that change if OPI input is changed.
    private DisplayModel displayModel;
    private IPath path;
    // macrosInput should not be null. If there are no macros it should
    // be an empty MacrosInput object.
    private MacrosInput macrosInput;
    // Variable to track if parent view has been lost
    private boolean viewLost;

    // Private constructor means you can't open an OPIShell without adding
    // it to the cache.
    private OPIShell(Display display, IPath path, MacrosInput macrosInput) throws Exception {
        this.path = path;
        this.macrosInput = macrosInput;
        icon = OPIBuilderPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/OPIRunner.png")
                .createImage(display);

        shell = new Shell(display);
        // On macOS multiple windows use the same dock icon, which
        // we'd like to have stay unchanged.
        if (!Platform.getOS().equals(Platform.OS_MACOSX)) {
            shell.setImage(icon);
        }
        displayModel = new DisplayModel(path);
        displayModel.setOpiRuntime(this);
        actionRegistry = new ActionRegistry();

        viewer = new GraphicalViewerImpl();
        viewLost = false;

        viewer.createControl(shell);
        viewer.setEditPartFactory(new WidgetEditPartFactory(ExecutionMode.RUN_MODE));
        viewer.setKeyHandler(new KeyHandler());

        viewer.setRootEditPart(new ScalableRootEditPart() {
            @Override
            public DragTracker getDragTracker(Request req) {
                return new DragEditPartsTracker(this);
            }

            @Override
            public boolean isSelectable() {
                return false;
            }
        });

        var editDomain = new EditDomain() {
            @Override
            public void loadDefaultTool() {
                setActiveTool(new RuntimePatchedSelectionTool());
            }
        };
        editDomain.addViewer(viewer);

        displayModel = createDisplayModel();
        setTitle();

        shell.setLayout(new FillLayout());
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent e) {
            }

            @Override
            public void shellDeiconified(ShellEvent e) {
            }

            @Override
            public void shellDeactivated(ShellEvent e) {
                activeShell = null;
            }

            @Override
            public void shellClosed(ShellEvent e) {
                // Remove this shell from the cache.
                openShells.remove(OPIShell.this);
                sendUpdateCommand();
            }

            @Override
            public void shellActivated(ShellEvent e) {
                // Shell has been activated so check whether
                // we lost the parent view and if so re-register
                if (viewLost) {
                    sendUpdateCommand();
                    viewLost = false;
                }
                activeShell = OPIShell.this;
            }
        });
        shell.addDisposeListener(e -> {
            if (!icon.isDisposed()) {
                icon.dispose();
            }
        });

        /*
         * Don't open the Shell here, as it causes SWT to think the window is on top when it really isn't.
         * Wait until the window is open, then call shell.setFocus() in the activated listener.
         *
         * Make some attempt at sizing the shell, sometimes a shell is not given focus and the shellActivated
         * listener callback doesn't resize the window. It's better to have something a little too large as the
         * default. Related to Eclipse bug 96700.
         */
        shell.setSize(displayModel.getSize().width + WINDOW_BORDER_X, displayModel.getSize().height + WINDOW_BORDER_Y);
        if (!displayModel.getLocation().equals(DisplayModel.NULL_LOCATION)) {
            shell.setLocation(displayModel.getLocation().getSWTPoint());
        }
        shell.setVisible(true);

        // Resize shell correctly after opening.
        UIBundlingThread.getInstance().addRunnable(this::resizeToContents);
    }

    /**
     * In order for the right-click menu to work, this shell must be registered with a view. Register the context menu
     * against the view. Make the view the default.
     *
     * @param view
     */
    public void registerWithView(IViewPart view) {
        this.view = view;
        var refreshAction = new RefreshOPIAction(this);
        actionRegistry.registerAction(refreshAction);
        viewer.getKeyHandler().put(KeyStroke.getPressed(SWT.F5, 0), refreshAction);
        actionRegistry.registerAction(new PrintDisplayAction(this));
        var contextMenuProvider = new OPIRunnerContextMenuProvider(viewer, this);
        getSite().registerContextMenu(contextMenuProvider, viewer);
        viewer.setContextMenu(contextMenuProvider);
    }

    /**
     * Register that the parent view has been disposed so need to re-register this shell with a new view if available,
     * otherwise the context menu will fail
     */
    public void notifyParentViewClosed() {
        viewLost = true;
    }

    public MacrosInput getMacrosInput() {
        return macrosInput;
    }

    public IPath getPath() {
        return path;
    }

    public void raiseToTop() {
        if (shell.getMinimized()) {
            shell.setMinimized(false);
        }
        shell.forceFocus();
        shell.forceActive();
        shell.setFocus();
        shell.setActive();
    }

    @Override
    public boolean equals(Object o) {
        var equal = false;
        if (o instanceof OPIShell) {
            var opiShell = (OPIShell) o;
            equal = opiShell.getMacrosInput().equals(getMacrosInput());
            equal &= opiShell.getPath().equals(path);
        }
        return equal;
    }

    public void close() {
        shell.close();
        dispose();
    }

    private DisplayModel createDisplayModel() throws Exception {
        displayModel = new DisplayModel(path);
        XMLUtil.fillDisplayModelFromInputStream(ResourceUtil.pathToInputStream(path), displayModel, null, macrosInput);
        if (macrosInput != null) {
            macrosInput = macrosInput.getCopy();
            macrosInput.getMacrosMap().putAll(displayModel.getMacrosInput().getMacrosMap());
            displayModel.setPropertyValue(AbstractContainerModel.PROP_MACROS, macrosInput);
        }
        viewer.setContents(displayModel);
        displayModel.setViewer(viewer);
        displayModel.setOpiRuntime(this);
        return displayModel;
    }

    private void setTitle() {
        if (displayModel.getName() != null && displayModel.getName().trim().length() > 0) {
            shell.setText(displayModel.getName());
        } else { // If the name doesn't exist, use the OPI path
            shell.setText(path.toString());
        }
    }

    private void resizeToContents() {
        var frameX = shell.getSize().x - shell.getClientArea().width;
        var frameY = shell.getSize().y - shell.getClientArea().height;
        shell.setSize(displayModel.getSize().width + frameX, displayModel.getSize().height + frameY);
    }

    /*************************************************************
     * Static helper methods to manage open shells.
     *************************************************************/

    /**
     * This is the only way to create an OPIShell. Logs an error and cleans up if path is null.
     */
    public static void openOPIShell(IPath path, MacrosInput macrosInput) {
        if (macrosInput == null) {
            macrosInput = new MacrosInput(new LinkedHashMap<String, String>(), true);
        }
        var alreadyOpen = false;
        for (var opiShell : openShells) {
            if (opiShell.getPath().equals(path) && opiShell.getMacrosInput().equals(macrosInput)) {
                opiShell.raiseToTop();
                alreadyOpen = true;
            }
        }
        if (!alreadyOpen) {
            OPIShell os = null;
            try {
                os = new OPIShell(Display.getCurrent(), path, macrosInput);
                openShells.add(os);
                sendUpdateCommand();
            } catch (Exception e) {
                if (os != null) {
                    os.dispose();
                }
                log.log(Level.WARNING, "Failed to create new OPIShell.", e);
            }
        }
    }

    /**
     * Close all open OPIShells. Use getAllShells() for a copy of the set, to avoid removing items during iteration.
     */
    public static void closeAll() {
        for (var s : getAllShells()) {
            s.close();
        }
    }

    /**
     * Show all open OPIShells.
     */
    public static void showAll() {
        for (var s : getAllShells()) {
            s.raiseToTop();
        }
    }

    /**
     * Search the cache of open OPIShells to find a match for the input Shell object.
     *
     * Return associated OPIShell or Null if none found
     */
    public static OPIShell getOPIShellForShell(Shell target) {
        OPIShell foundShell = null;
        if (target != null) {
            for (var os : openShells) {
                if (os.shell == target) {
                    foundShell = os;
                    break;
                }
            }
        }
        return foundShell;
    }

    /**
     * Return a copy of the set of open shells. Returning the same instance may lead to problems when closing shells.
     *
     * @return a copy of the set of open shells.
     */
    public static Set<OPIShell> getAllShells() {
        return new LinkedHashSet<>(openShells);
    }

    /**
     * Return the active shell, which may be null
     *
     * @return the active OPIShell
     */
    public static OPIShell getActiveShell() {
        return activeShell;
    }

    /**
     * Alert whoever is listening that a new OPIShell has been created.
     */
    private static void sendUpdateCommand() {
        IServiceLocator serviceLocator = PlatformUI.getWorkbench();
        var commandService = serviceLocator.getService(ICommandService.class);
        try {
            var command = commandService.getCommand(OPI_SHELLS_CHANGED_ID);
            command.executeWithChecks(new ExecutionEvent());
        } catch (ExecutionException | NotHandledException | NotEnabledException | NotDefinedException e) {
            log.log(Level.WARNING, "Failed to send OPI shells changed command", e);
        }
    }

    /********************************************
     * Partial implementation of IOPIRuntime
     ********************************************/
    @Override
    public void addPropertyListener(IPropertyListener listener) {
        throw new NotImplementedException();
    }

    @Override
    public void createPartControl(Composite parent) {
        throw new NotImplementedException();
    }

    @Override
    public void dispose() {
        shell.dispose();
        actionRegistry.dispose();
    }

    @Override
    public IWorkbenchPartSite getSite() {
        if (view != null) {
            return view.getSite();
        } else {
            return null;
        }
    }

    @Override
    public String getTitle() {
        return shell.getText();
    }

    @Override
    public Image getTitleImage() {
        throw new NotImplementedException();
    }

    @Override
    public String getTitleToolTip() {
        return shell.getToolTipText();
    }

    @Override
    public void removePropertyListener(IPropertyListener listener) {
        throw new NotImplementedException();
    }

    @Override
    public void setFocus() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == ActionRegistry.class) {
            return adapter.cast(actionRegistry);
        }
        if (adapter == GraphicalViewer.class) {
            return adapter.cast(viewer);
        }
        if (adapter == CommandStack.class) {
            return adapter.cast(viewer.getEditDomain().getCommandStack());
        }
        return null;
    }

    @Override
    public void setWorkbenchPartName(String name) {
        throw new NotImplementedException();
    }

    /**
     * Render a new OPI in the same shell.
     */
    @Override
    public void setOPIInput(IEditorInput input) throws PartInitException {
        try {
            // The old OPIShell needs to be removed from the cache and the new one
            // added afterwards, because they don't evaluate as equal.
            openShells.remove(this);
            if (input instanceof IFileEditorInput) {
                path = ((IFileEditorInput) input).getFile().getFullPath();
            } else if (input instanceof RunnerInput) {
                path = ((RunnerInput) input).getPath();
                macrosInput = ((RunnerInput) input).getMacrosInput();
            }
            displayModel = createDisplayModel();
            setTitle();
            resizeToContents();
            openShells.add(this);
            sendUpdateCommand();
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to replace OPIShell contents.", e);
        }
    }

    @Override
    public IEditorInput getOPIInput() {
        var file = ResourcesPlugin.getWorkspace().getRoot().getFile(displayModel.getOpiFilePath());
        return new FileEditorInput(file);
    }

    @Override
    public DisplayModel getDisplayModel() {
        return displayModel;
    }

    public boolean isDisposed() {
        return ((shell == null) || (shell.isDisposed()));
    }
}
