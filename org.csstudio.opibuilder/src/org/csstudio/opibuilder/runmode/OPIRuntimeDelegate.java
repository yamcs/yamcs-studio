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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.csstudio.opibuilder.actions.PrintDisplayAction;
import org.csstudio.opibuilder.actions.RefreshOPIAction;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.WidgetEditPartFactory;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.Draw2dSingletonUtil;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.UpdateListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * The delegate to run an OPI in an editor or view.
 */
public class OPIRuntimeDelegate implements IAdaptable {

    private DisplayModel displayModel;

    private boolean displayModelFilled;

    private DisplayOpenManager displayOpenManager;

    private PatchedScrollingGraphicalViewer viewer;

    private ActionRegistry actionRegistry;

    private IEditorInput editorInput;

    private IWorkbenchPartSite site;

    /**
     * The workbench part where the OPI is running on.
     */
    private IOPIRuntime opiRuntime;

    private PaintListener errorMessagePaintListener = e -> {
        e.gc.setForeground(CustomMediaFactory.getInstance().getColor(255, 0, 0));
        e.gc.drawString("Failed to load opi " + getEditorInput(), 0, 0);
    };

    private ZoomManager zoomManager;

    public OPIRuntimeDelegate(IOPIRuntime opiRuntime) {
        this.opiRuntime = opiRuntime;
    }

    public void init(IWorkbenchPartSite site, IEditorInput input) throws PartInitException {
        this.site = site;
        setEditorInput(input);
        if (viewer != null) {
            viewer.getControl().removePaintListener(errorMessagePaintListener);
        }

        displayModel = new DisplayModel(getOPIFilePath());
        displayModel.setOpiRuntime(opiRuntime);
        displayModelFilled = false;
        InputStream inputStream = null;
        try {
            if (input instanceof IRunnerInput) {
                var run_input = (IRunnerInput) input;
                inputStream = run_input.getInputStream();
                displayOpenManager = run_input.getDisplayOpenManager();
            } else {
                inputStream = ResourceUtil.getInputStreamFromEditorInput(input);
            }
            if (inputStream != null) {
                MacrosInput macrosInput = null;
                if (input instanceof IRunnerInput) {
                    macrosInput = ((IRunnerInput) input).getMacrosInput();
                }
                XMLUtil.fillDisplayModelFromInputStream(inputStream, displayModel, null, macrosInput);
                displayModelFilled = true;
                if (input instanceof IRunnerInput) {
                    addRunnerInputMacros(input);
                }
            }
        } catch (Exception e) {
            ErrorHandlerUtil.handleError("Failed to open opi file: " + input, e, true, true);
            throw new PartInitException("Failed to run OPI file: " + input, e);
        }

        // if it was an opened editor
        if (viewer != null && displayModelFilled) {
            viewer.setContents(displayModel);
            updateEditorTitle();
            displayModel.setViewer(viewer);
            displayModel.setOpiRuntime(opiRuntime);
        }

        getActionRegistry().registerAction(new RefreshOPIAction(opiRuntime));
        getActionRegistry().registerAction(new PrintDisplayAction(opiRuntime));

        // hide close button
        hideCloseButton(site);
    }

    public void createGUI(Composite parent) {
        viewer = new PatchedScrollingGraphicalViewer();
        if (displayModel != null) {
            displayModel.setOpiRuntime(opiRuntime);
            displayModel.setViewer(viewer);
        }
        ScalableFreeformRootEditPart root = new PatchedScalableFreeformRootEditPart() {
            // In Run mode, clicking the Display or container should de-select
            // all widgets.
            @Override
            public DragTracker getDragTracker(Request req) {
                return new DragEditPartsTracker(this);
            }

            @Override
            public boolean isSelectable() {
                return false;
            }
        };
        // set clipping strategy for connection layer of connection can be hide
        // when its source or target is not showing.
        var connectionLayer = (ConnectionLayer) root.getLayer(LayerConstants.CONNECTION_LAYER);
        connectionLayer.setClippingStrategy(new PatchedConnectionLayerClippingStrategy(connectionLayer));

        viewer.createControl(parent);
        viewer.setRootEditPart(root);
        viewer.setEditPartFactory(new WidgetEditPartFactory(ExecutionMode.RUN_MODE, site));

        // viewer.addDropTargetListener(new
        // ProcessVariableNameTransferDropPVTargetListener(viewer));
        // viewer.addDropTargetListener(new
        // TextTransferDropPVTargetListener(viewer));
        // Add drag listener will make click feel stagnant.
        // viewer.addDragSourceListener(new DragPVSourceListener(viewer));
        // this will make viewer as a selection provider
        EditDomain editDomain = new EditDomain() {
            @Override
            public void loadDefaultTool() {
                setActiveTool(new RuntimePatchedSelectionTool());
            }
        };
        editDomain.addViewer(viewer);

        // connect the CSS menu
        ContextMenuProvider cmProvider = new OPIRunnerContextMenuProvider(viewer, opiRuntime);
        viewer.setContextMenu(cmProvider);

        opiRuntime.getSite().registerContextMenu(cmProvider, viewer);
        if (displayModelFilled) {
            viewer.setContents(displayModel);
            displayModel.setViewer(viewer);
            displayModel.setOpiRuntime(opiRuntime);
            updateEditorTitle();
        }

        zoomManager = root.getZoomManager();

        if (zoomManager != null) {
            List<String> zoomLevels = new ArrayList<>(3);
            zoomLevels.add(Draw2dSingletonUtil.ZoomManager_FIT_ALL);
            zoomLevels.add(Draw2dSingletonUtil.ZoomManager_FIT_WIDTH);
            zoomLevels.add(Draw2dSingletonUtil.ZoomManager_FIT_HEIGHT);
            zoomManager.setZoomLevelContributions(zoomLevels);

            zoomManager.setZoomLevels(createZoomLevels());

            // IAction zoomIn = new ZoomInAction(zoomManager);
            // IAction zoomOut = new ZoomOutAction(zoomManager);
            // getActionRegistry().registerAction(zoomIn);
            // getActionRegistry().registerAction(zoomOut);
        }

        /* scroll-wheel zoom */
        viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);

        /*
         * When Figure instance which corresponds to RootEditPart is updated, calculate the frame rate and set the
         * measured rate to "frame_rate" property of the corresponding DisplayModel instance.
         *
         * By default, org.eclipse.draw2d.DeferredUpdateManager is used. This update manager queues update requests from
         * figures and others, and it repaints requested figures at once when GUI thread is ready to repaint.
         * notifyPainting() method of UpdateLister is called when it repaints. The frame rate is calculated based on the
         * timing of notifyPainting().
         *
         * Note that the update manager repaints only requested figures. It does not repaint all figures at once. For
         * example, if there are only two widgets in one display, these widgets might be repainted alternately. In that
         * case, the frame rate indicates the inverse of the time between the repainting of one widget and the
         * repainting of the other widget, which is different from our intuition. Thus, you have to be careful about the
         * meaning of "frame rate" calculated by the following code.
         */
        if (displayModelFilled && displayModel.isFreshRateEnabled()) {
            var updateManager = root.getFigure().getUpdateManager();
            updateManager.addUpdateListener(new UpdateListener() {
                private long updateCycle = -1; // in milliseconds
                private Date previousDate = null;

                @Override
                public void notifyPainting(Rectangle damage, @SuppressWarnings("rawtypes") Map dirtyRegions) {
                    var currentDate = new Date();

                    if (previousDate == null) {
                        previousDate = currentDate;
                        return;
                    }

                    synchronized (previousDate) {
                        updateCycle = currentDate.getTime() - previousDate.getTime();
                        displayModel.setFrameRate(1000.0 / updateCycle);
                        previousDate = currentDate;
                    }
                }

                @Override
                public void notifyValidating() {
                    // Do nothing
                }
            });
        }
    }

    private void updateEditorTitle() {
        if (displayModel.getName() != null && displayModel.getName().trim().length() > 0) {
            opiRuntime.setWorkbenchPartName(displayModel.getName());
        } else {
            opiRuntime.setWorkbenchPartName(getEditorInput().getName());
        }
    }

    public IPath getOPIFilePath() {
        var editorInput = getEditorInput();
        return ResourceUtil.getPathInEditor(editorInput);
    }

    private void hideCloseButton(IWorkbenchPartSite site) {
        if (!displayModel.isShowCloseButton()) {
            Display.getCurrent().asyncExec(() -> {
                // TODO Improve implementation

                // Configure the E4 model element.
                // Issue 1:
                // When opening the display for the first time,
                // the 'x' in the tab is still displayed.
                // Only on _restart_ of the app will the tab be displayed
                // without the 'x' to close it.
                // Issue 2:
                // Part can still be closed via Ctrl-W (Command-W on OS X)
                // or via menu File/close.
                var part = site.getService(MPart.class);
                part.setCloseable(false);

                // Original RCP code
                // PartPane currentEditorPartPane = ((PartSite) site)
                // .getPane();
                // PartStack stack = currentEditorPartPane.getStack();
                // Control control = stack.getControl();
                // if (control instanceof CTabFolder) {
                // CTabFolder tabFolder = (CTabFolder) control;
                // tabFolder.getSelection().setShowClose(false);
                // }
            });
        }
    }

    public void setEditorInput(IEditorInput editorInput) {
        this.editorInput = editorInput;
    }

    public IEditorInput getEditorInput() {
        return editorInput;
    }

    public DisplayModel getDisplayModel() {
        return displayModel;
    }

    /**
     * Lazily creates and returns the action registry.
     *
     * @return the action registry
     */
    protected ActionRegistry getActionRegistry() {
        if (actionRegistry == null) {
            actionRegistry = new ActionRegistry();
        }
        return actionRegistry;
    }

    /**
     * Create a double array that contains the pre-defined zoom levels.
     *
     * @return A double array that contains the pre-defined zoom levels.
     */
    private double[] createZoomLevels() {
        List<Double> zoomLevelList = new ArrayList<>();

        var level = 0.1;
        while (level <= 0.9) {
            zoomLevelList.add(level);
            level = level + 0.1;
        }
        zoomLevelList.add(1.0);
        zoomLevelList.add(1.1);
        zoomLevelList.add(1.2);
        zoomLevelList.add(1.3);
        zoomLevelList.add(1.5);
        zoomLevelList.add(2.0);
        zoomLevelList.add(2.5);
        zoomLevelList.add(3.0);
        zoomLevelList.add(3.5);
        zoomLevelList.add(4.0);
        zoomLevelList.add(4.5);
        zoomLevelList.add(5.0);

        var result = new double[zoomLevelList.size()];
        for (var i = 0; i < zoomLevelList.size(); i++) {
            result[i] = zoomLevelList.get(i);
        }

        return result;
    }

    private void addRunnerInputMacros(IEditorInput input) {
        var macrosInput = ((IRunnerInput) input).getMacrosInput();

        if (macrosInput != null) {
            macrosInput = macrosInput.getCopy();
            macrosInput.getMacrosMap().putAll(displayModel.getMacrosInput().getMacrosMap());
            displayModel.setPropertyValue(AbstractContainerModel.PROP_MACROS, macrosInput);
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == DisplayOpenManager.class) {
            if (displayOpenManager == null) {
                displayOpenManager = new DisplayOpenManager(opiRuntime);
            }
            return adapter.cast(displayOpenManager);
        }
        if (adapter == GraphicalViewer.class) {
            return adapter.cast(viewer);
        }
        if (adapter == ActionRegistry.class) {
            return adapter.cast(getActionRegistry());
        }
        if (adapter == CommandStack.class) {
            return adapter.cast(viewer.getEditDomain().getCommandStack());
        }
        if (adapter == ZoomManager.class) {
            return adapter.cast(((ScalableFreeformRootEditPart) viewer.getRootEditPart()).getZoomManager());
        }
        return null;
    }

    /**
     * Dispose of all resources.
     */
    public void dispose() {
        getActionRegistry().dispose();
        if (displayOpenManager != null) {
            displayOpenManager.dispose();
            displayOpenManager = null;
        }
        if (displayModel != null) {
            displayModel.setViewer(null);
            displayModel = null;
        }
        if (viewer != null) {
            viewer.setContents(null);
            viewer = null;
        }
    }
}
