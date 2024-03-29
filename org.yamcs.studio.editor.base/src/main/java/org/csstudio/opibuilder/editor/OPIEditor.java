/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.editor;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.actions.ChangeOrderAction;
import org.csstudio.opibuilder.actions.ChangeOrderAction.OrderType;
import org.csstudio.opibuilder.actions.ChangeOrientationAction;
import org.csstudio.opibuilder.actions.ChangeOrientationAction.OrientationType;
import org.csstudio.opibuilder.actions.CopyPropertiesAction;
import org.csstudio.opibuilder.actions.CopyWidgetsAction;
import org.csstudio.opibuilder.actions.CutWidgetsAction;
import org.csstudio.opibuilder.actions.DistributeWidgetsAction;
import org.csstudio.opibuilder.actions.DistributeWidgetsAction.DistributeType;
import org.csstudio.opibuilder.actions.PastePropertiesAction;
import org.csstudio.opibuilder.actions.PasteWidgetsAction;
import org.csstudio.opibuilder.actions.PrintDisplayAction;
import org.csstudio.opibuilder.actions.ReplaceWidgetsAction;
import org.csstudio.opibuilder.actions.RunOPIAction;
import org.csstudio.opibuilder.actions.ShowIndexInTreeViewAction;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.dnd.ProcessVariableNameTransferDropPVTargetListener;
import org.csstudio.opibuilder.dnd.TextTransferDropPVTargetListener;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.WidgetEditPartFactory;
import org.csstudio.opibuilder.editparts.WidgetTreeEditpartFactory;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.model.RulerModel;
import org.csstudio.opibuilder.palette.OPIEditorPaletteFactory;
import org.csstudio.opibuilder.palette.WidgetCreationFactory;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.runmode.PatchedConnectionLayerClippingStrategy;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.AutoexposeHelper;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ViewportAutoexposeHelper;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.CopyTemplateAction;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * The OPI Editor.
 */
public class OPIEditor extends GraphicalEditorWithFlyoutPalette {

    private static final Logger LOGGER = Logger.getLogger(OPIEditor.class.getName());

    /**
     * The file extension for OPI files.
     */
    public static final String OPI_FILE_EXTENSION = "opi";
    public static final String ID = "org.csstudio.opibuilder.OPIEditor";

    private PaletteRoot paletteRoot;

    /** the undoable <code>IPropertySheetPage</code> */
    private PropertySheetPage undoablePropertySheetPage;

    private DisplayModel displayModel;

    private RulerComposite rulerComposite;

    private KeyHandler sharedKeyHandler;

    private OverviewOutlinePage overviewOutlinePage;

    private OutlinePage outlinePage;

    private Clipboard clipboard;

    private SelectionSynchronizer synchronizer;

    public OPIEditor() {
        if (getPalettePreferences().getPaletteState() <= 0) {
            getPalettePreferences().setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
        }
        setEditDomain(new DefaultEditDomain(this));
    }

    @Override
    public void dispose() {
        if (outlinePage != null) {
            outlinePage.dispose();
            outlinePage = null;
        }
        if (overviewOutlinePage != null) {
            overviewOutlinePage.dispose();
            overviewOutlinePage = null;
        }
        if (undoablePropertySheetPage != null) {
            undoablePropertySheetPage.dispose();
            undoablePropertySheetPage = null;
        }
        displayModel = null;
        super.dispose();
    }

    @Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        var viewer = (ScrollingGraphicalViewer) getGraphicalViewer();
        viewer.setEditPartFactory(new WidgetEditPartFactory(ExecutionMode.EDIT_MODE));
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart() {
            @Override
            public Object getAdapter(@SuppressWarnings("rawtypes") final Class key) {
                if (key == AutoexposeHelper.class) {
                    return new ViewportAutoexposeHelper(this);
                }
                return super.getAdapter(key);
            }
        };

        // set clipping strategy for connection layer of connection can be hide
        // when its source or target is not showing.
        var connectionLayer = (ConnectionLayer) root.getLayer(LayerConstants.CONNECTION_LAYER);
        connectionLayer.setClippingStrategy(new PatchedConnectionLayerClippingStrategy(connectionLayer));

        viewer.setRootEditPart(root);
        viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer).setParent(getCommonKeyHandler()));
        ContextMenuProvider cmProvider = new OPIEditorContextMenuProvider(viewer, getActionRegistry());
        viewer.setContextMenu(cmProvider);
        getSite().registerContextMenu(cmProvider, viewer);

        // Grid Action
        IAction action = new ToggleGridAction(getGraphicalViewer()) {
            @Override
            public boolean isChecked() {
                return getDisplayModel().isShowGrid();
            }

            @Override
            public void run() {
                getCommandStack()
                        .execute(new SetWidgetPropertyCommand(displayModel, DisplayModel.PROP_SHOW_GRID, !isChecked()));
            }
        };

        getActionRegistry().registerAction(action);

        // Ruler Action
        configureRuler();
        action = new ToggleRulerVisibilityAction(getGraphicalViewer()) {
            @Override
            public boolean isChecked() {
                return getDisplayModel().isShowRuler();
            }

            @Override
            public void run() {
                getCommandStack().execute(
                        new SetWidgetPropertyCommand(displayModel, DisplayModel.PROP_SHOW_RULER, !isChecked()));
            }
        };
        getActionRegistry().registerAction(action);

        // Snap to Geometry Action
        IAction geometryAction = new ToggleSnapToGeometryAction(getGraphicalViewer()) {
            @Override
            public boolean isChecked() {
                return getDisplayModel().isSnapToGeometry();
            }

            @Override
            public void run() {
                getCommandStack().execute(
                        new SetWidgetPropertyCommand(displayModel, DisplayModel.PROP_SNAP_GEOMETRY, !isChecked()));
            }
        };
        getActionRegistry().registerAction(geometryAction);

        // configure zoom actions
        var zm = root.getZoomManager();
        if (zm != null) {
            List<String> zoomLevels = new ArrayList<>(3);
            zoomLevels.add(ZoomManager.FIT_ALL);
            zoomLevels.add(ZoomManager.FIT_WIDTH);
            zoomLevels.add(ZoomManager.FIT_HEIGHT);
            zm.setZoomLevelContributions(zoomLevels);
            zm.setZoomLevels(createZoomLevels());
            IAction zoomIn = new ZoomInAction(zm);
            IAction zoomOut = new ZoomOutAction(zm);
            getActionRegistry().registerAction(zoomIn);
            getActionRegistry().registerAction(zoomOut);
        }

        /* scroll-wheel zoom */
        getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
                MouseWheelZoomHandler.SINGLETON);

        // status line listener
        getGraphicalViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            private IStatusLineManager statusLine = ((ActionBarContributor) getEditorSite().getActionBarContributor())
                    .getActionBars().getStatusLineManager();

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateStatusLine(statusLine);
            }
        });
    }

    private void updateStatusLine(IStatusLineManager statusLine) {
        List<AbstractBaseEditPart> selectedWidgets = new ArrayList<>();
        for (var editpart : getGraphicalViewer().getSelectedEditParts()) {
            if (editpart instanceof AbstractBaseEditPart && !(editpart instanceof DisplayEditpart)) {
                selectedWidgets.add((AbstractBaseEditPart) editpart);
            }
        }
        if (selectedWidgets.size() == 1) {
            statusLine.setMessage(selectedWidgets.get(0).getWidgetModel().getName() + "("
                    + selectedWidgets.get(0).getWidgetModel().getType() + ")");
        } else if (selectedWidgets.size() >= 1) {
            statusLine.setMessage(selectedWidgets.size() + " widgets were selected");
        } else {
            statusLine.setMessage("No widget was selected");
        }
    }

    /**
     * Configure the properties for the rulers.
     */
    private void configureRuler() {
        // Ruler properties
        RulerProvider hprovider = new OPIEditorRulerProvider(new RulerModel(true));
        RulerProvider vprovider = new OPIEditorRulerProvider(new RulerModel(false));
        getGraphicalViewer().setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, hprovider);
        getGraphicalViewer().setProperty(RulerProvider.PROPERTY_VERTICAL_RULER, vprovider);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createActions() {
        super.createActions();

        getEditorSite().getService(IContextService.class)
                .activateContext("org.csstudio.opibuilder.opiEditor");

        var registry = getActionRegistry();
        IAction action;

        action = new CopyTemplateAction(this);
        registry.registerAction(action);

        action = new MatchWidthAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new MatchHeightAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new DirectEditAction((IWorkbenchPart) this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        var id = ActionFactory.DELETE.getId();
        action = getActionRegistry().getAction(id);
        action.setActionDefinitionId("org.eclipse.ui.edit.delete");

        action = new PasteWidgetsAction(this);
        registry.registerAction(action);

        action = new CopyWidgetsAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new CutWidgetsAction(this, (DeleteAction) registry.getAction(ActionFactory.DELETE.getId()));
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new PrintDisplayAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        id = ActionFactory.SELECT_ALL.getId();
        action = getActionRegistry().getAction(id);
        action.setActionDefinitionId("org.eclipse.ui.edit.selectAll");

        id = ActionFactory.UNDO.getId();
        action = getActionRegistry().getAction(id);
        action.setActionDefinitionId("org.eclipse.ui.edit.undo");

        id = ActionFactory.REDO.getId();
        action = getActionRegistry().getAction(id);
        action.setActionDefinitionId("org.eclipse.ui.edit.redo");

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.LEFT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.RIGHT);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.TOP);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.BOTTOM);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.CENTER);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new AlignmentAction((IWorkbenchPart) this, PositionConstants.MIDDLE);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        for (var dt : DistributeType.values()) {
            action = new DistributeWidgetsAction(this, dt);
            registry.registerAction(action);
            getSelectionActions().add(action.getId());
        }

        for (var orderType : OrderType.values()) {
            action = new ChangeOrderAction(this, orderType);
            registry.registerAction(action);
            getSelectionActions().add(action.getId());
        }

        for (var orientationType : OrientationType.values()) {
            action = new ChangeOrientationAction(this, orientationType);
            registry.registerAction(action);
            getSelectionActions().add(action.getId());
        }

        action = new RunOPIAction();
        registry.registerAction(action);

        var pastePropAction = new PastePropertiesAction(this);
        registry.registerAction(pastePropAction);
        getSelectionActions().add(pastePropAction.getId());

        action = new CopyPropertiesAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new ReplaceWidgetsAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());
    }

    @Override
    protected void createGraphicalViewer(Composite parent) {
        initDisplayModel();

        rulerComposite = new RulerComposite(parent, SWT.NONE);

        GraphicalViewer viewer = new PatchedScrollingGraphicalViewer();
        viewer.createControl(rulerComposite);
        setGraphicalViewer(viewer);
        configureGraphicalViewer();
        hookGraphicalViewer();
        initializeGraphicalViewer();

        rulerComposite.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
    }

    @Override
    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain()) {
            @Override
            protected void configurePaletteViewer(PaletteViewer viewer) {
                super.configurePaletteViewer(viewer);
                // create a drag source listener for this palette viewer
                // together with an appropriate transfer drop target listener, this will enable
                // model element creation by dragging a CombinatedTemplateCreationEntries
                // from the palette into the editor
                // @see ShapesEditor#createTransferDropTargetListener()
                viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
            }
        };
    }

    /**
     * Create a transfer drop target listener. When using a CombinedTemplateCreationEntry tool in the palette, this will
     * enable model element creation by dragging from the palette.
     */
    private TransferDropTargetListener createTransferDropTargetListener() {
        return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
            @Override
            protected CreationFactory getFactory(Object template) {
                return (WidgetCreationFactory) template;
            }
        };
    }

    /**
     * Create a double array that contains the pre-defined zoom levels.
     */
    private double[] createZoomLevels() {
        List<Double> zoomLevelList = new ArrayList<>();

        var level = 0.1;
        while (level < 1.0) {
            zoomLevelList.add(level);
            level = level + 0.05;
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

    @Override
    public void doSave(IProgressMonitor monitor) {
        if (getEditorInput() instanceof FileEditorInput || getEditorInput() instanceof FileStoreEditorInput) {
            performSave();
        } else {
            doSaveAs();
        }
    }

    @Override
    public void doSaveAs() {
        var saveAsDialog = new SaveAsDialog(getEditorSite().getShell());
        if (getEditorInput() instanceof FileEditorInput) {
            saveAsDialog.setOriginalFile(((FileEditorInput) getEditorInput()).getFile());
        } else if (getEditorInput() instanceof FileStoreEditorInput) {
            saveAsDialog.setOriginalName(((FileStoreEditorInput) getEditorInput()).getName());
        }

        var ret = saveAsDialog.open();

        try {
            if (ret == Window.OK) {
                var targetPath = saveAsDialog.getResult();
                var targetFile = ResourcesPlugin.getWorkspace().getRoot().getFile(targetPath);

                if (!targetFile.exists()) {
                    targetFile.create(null, true, null);
                }

                var editorInput = new FileEditorInput(targetFile);

                setInput(editorInput);

                setPartName(targetFile.getName());

                performSave();
            }
        } catch (CoreException e) {
            MessageDialog.openError(getSite().getShell(), "IO Error", e.getMessage());
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "File save error", e);
        }
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
        if (type == IPropertySheetPage.class) {
            return getPropertySheetPage();
        } else if (type == ZoomManager.class) {
            return ((ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
        } else if (type == IContentOutlinePage.class) {
            outlinePage = new OutlinePage(new TreeViewer());
            return outlinePage;
        } else if (type.equals(IGotoMarker.class)) {
            return (IGotoMarker) marker -> {
                try {
                    var wuid = (String) marker.getAttribute(AbstractWidgetModel.PROP_WIDGET_UID);
                    if (wuid == null) {
                        // if wuid is not stored in the marker try to find it based on character
                        var charStart = (Integer) marker.getAttribute(IMarker.CHAR_START);
                        if (charStart == null) {
                            return;
                        }
                        // Get the closest widget to charStart position
                        wuid = XMLUtil.findClosestWidgetUid(getInputStream(), charStart);
                        if (wuid == null) {
                            return;
                        }
                    }
                    var widget = getDisplayModel().getWidgetFromWUID(wuid);
                    if (widget == null) {
                        return;
                    }
                    // Get the widget editPart
                    var obj = getGraphicalViewer().getEditPartRegistry().get(widget);
                    if (obj != null && obj instanceof AbstractBaseEditPart) {
                        EditPart widgetEditPart = (AbstractBaseEditPart) obj;

                        // Reveal the widget
                        getGraphicalViewer().reveal(widgetEditPart);

                        // Find the closest selectable part
                        while (widgetEditPart != null && !widgetEditPart.isSelectable()) {
                            widgetEditPart = widgetEditPart.getParent();
                        }
                        if (widgetEditPart != null) {
                            // Select the widget in OPI
                            var selectionManager = getGraphicalViewer().getSelectionManager();
                            selectionManager.deselectAll();
                            selectionManager.appendSelection(widgetEditPart);
                        }
                    }
                } catch (IOException e1) {
                    MessageDialog.openError(getSite().getShell(), "IO Error", e1.getMessage());
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "File open error", e1);
                } catch (CoreException e2) {
                    MessageDialog.openError(getSite().getShell(), "Core Error", e2.getMessage());
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "File open error", e2);
                }
            };
        }
        return super.getAdapter(type);
    }

    public Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = new Clipboard(getSite().getShell().getDisplay());
        }
        return clipboard;
    }

    /**
     * Returns the KeyHandler with common bindings for both the Outline and Graphical Views. For example, delete is a
     * common action.
     */
    protected KeyHandler getCommonKeyHandler() {
        if (sharedKeyHandler == null) {
            sharedKeyHandler = new KeyHandler();
            sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
                    getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
        }
        return sharedKeyHandler;
    }

    /**
     * Returns the Point, which is the center of the Display.
     *
     * @return Point The Point, which is the center of the Display
     */
    public Point getDisplayCenterPosition() {
        var root = (ScalableFreeformRootEditPart) getGraphicalViewer().getRootEditPart();

        var m = root.getZoomManager();

        var center = m.getViewport().getBounds().getCenter();

        var x = new Rectangle(center.x, center.y, 10, 10);

        x.translate(m.getViewport().getViewLocation());
        m.getScalableFigure().translateFromParent(x);

        var result = x.getLocation();

        return result;
    }

    public DisplayModel getDisplayModel() {
        return displayModel;
    }

    @Override
    protected Control getGraphicalControl() {
        return rulerComposite;
    }

    /**
     * Returns a stream which can be used to read this editors input data.
     *
     * @return a stream which can be used to read this editors input data
     */
    private InputStream getInputStream() {
        InputStream result = null;

        var editorInput = getEditorInput();

        if (editorInput instanceof FileEditorInput) {
            try {
                result = ((FileEditorInput) editorInput).getFile().getContents();
            } catch (CoreException e) {
                LOGGER.log(Level.WARNING, "Error reading file.", e);
            }
        } else if (editorInput instanceof FileStoreEditorInput) {
            var path = URIUtil.toPath(((FileStoreEditorInput) editorInput).getURI());
            try {
                result = new FileInputStream(path.toFile());
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "Error reading file.", e);
            }
        }

        return result;
    }

    /**
     * Returns the overview for the outline view.
     */
    protected OverviewOutlinePage getOverviewOutlinePage() {
        if (null == overviewOutlinePage && null != getGraphicalViewer()) {
            var rootEditPart = getGraphicalViewer().getRootEditPart();
            if (rootEditPart instanceof ScalableFreeformRootEditPart) {
                overviewOutlinePage = new OverviewOutlinePage((ScalableFreeformRootEditPart) rootEditPart);
            }
        }
        return overviewOutlinePage;
    }

    @Override
    protected PaletteRoot getPaletteRoot() {
        if (paletteRoot == null) {
            paletteRoot = OPIEditorPaletteFactory.createPalette();
        }
        return paletteRoot;
    }

    /**
     * Returns the main composite of the editor.
     */
    public Composite getParentComposite() {
        return rulerComposite;
    }

    /**
     * Returns the undoable <code>PropertySheetPage</code> for this editor.
     */
    protected PropertySheetPage getPropertySheetPage() {
        if (undoablePropertySheetPage == null) {
            undoablePropertySheetPage = new PropertySheetPage() {
                @Override
                protected ISaveablePart getSaveablePart() {
                    return null;
                }
            };
            undoablePropertySheetPage.setRootEntry(new UndoablePropertySheetEntry(getCommandStack()));
        }
        return undoablePropertySheetPage;
    }

    // Override this to make unselectable widgets won't be selected.
    @Override
    protected SelectionSynchronizer getSelectionSynchronizer() {
        if (synchronizer == null) {
            synchronizer = new SelectionSynchronizer() {
                @Override
                protected EditPart convert(EditPartViewer viewer, EditPart part) {
                    var editPart = super.convert(viewer, part);
                    if (editPart != null && editPart.isSelectable()) {
                        return editPart;
                    }
                    return null;
                }
            };
        }
        return synchronizer;
    }

    private void initDisplayModel() {

        displayModel = new DisplayModel();
        displayModel.setOpiFilePath(getOPIFilePath());
        try {
            XMLUtil.fillDisplayModelFromInputStream(getInputStream(), displayModel);
        } catch (Exception e) {
            var message = "Error happened when loading the OPI file!";
            ErrorHandlerUtil.handleError(message, e, true, true);
            getEditorSite().getPage().closeEditor(this, false);
        }
    }

    private IPath getOPIFilePath() {
        var editorInput = getEditorInput();
        if (editorInput instanceof FileEditorInput) {
            return ((FileEditorInput) editorInput).getFile().getFullPath();
        } else if (editorInput instanceof FileStoreEditorInput) {
            return URIUtil.toPath(((FileStoreEditorInput) editorInput).getURI());
        }
        return null;
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        var viewer = getGraphicalViewer();

        viewer.setContents(displayModel);
        displayModel.setViewer(viewer);

        viewer.addDropTargetListener(createTransferDropTargetListener());
        viewer.addDropTargetListener(new ProcessVariableNameTransferDropPVTargetListener(viewer));
        viewer.addDropTargetListener(new TextTransferDropPVTargetListener(viewer));
        setPartName(getEditorInput().getName());
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    private void performSave() {

        try {
            var content = XMLUtil.XML_HEADER + XMLUtil.widgetToXMLString(displayModel, true);
            if (getEditorInput() instanceof FileEditorInput) {
                InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
                try {
                    var file = ((FileEditorInput) getEditorInput()).getFile();
                    file.setContents(in, false, false, null);
                    in.close();
                } catch (Exception e) {
                    in.close();
                    processSaveFailedError(e);
                    return;
                }

            } else if (getEditorInput() instanceof FileStoreEditorInput) {
                try {
                    var file = URIUtil.toPath(((FileStoreEditorInput) getEditorInput()).getURI()).toFile();

                    var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                    writer.write(content);
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    processSaveFailedError(e);
                    return;
                }

            }
        } catch (Exception e) {
            processSaveFailedError(e);
            return;
        }

        getCommandStack().markSaveLocation();

        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    private void processSaveFailedError(Exception e) {
        MessageDialog.openError(getSite().getShell(), "Save Failed!",
                e.getMessage() + "\nThe original file might be deleted, moved or renamed. "
                        + "Please use File->Save as... to save this file.");
        OPIBuilderPlugin.getLogger().log(Level.WARNING, "File save failed", e);
    }

    protected FigureCanvas getFigureCanvas() {
        return (FigureCanvas) getGraphicalViewer().getControl();
    }

    /**
     * The outline page provide both tree view and overview.
     */
    class OutlinePage extends ContentOutlinePage implements IAdaptable {

        private PageBook pageBook;
        private Control outline;
        private Canvas overview;
        private IAction showOutlineAction, showOverviewAction;
        static final int ID_OUTLINE = 0;
        static final int ID_OVERVIEW = 1;
        private Thumbnail thumbnail;
        private DisposeListener disposeListener;

        public OutlinePage(EditPartViewer viewer) {
            super(viewer);
        }

        @Override
        public void init(IPageSite pageSite) {
            super.init(pageSite);
            var registry = getActionRegistry();
            var bars = pageSite.getActionBars();
            var id = ActionFactory.UNDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.REDO.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.DELETE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.COPY.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.PASTE.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = ActionFactory.PRINT.getId();
            bars.setGlobalActionHandler(id, registry.getAction(id));

            bars.updateActionBars();
        }

        protected void configureOutlineViewer() {
            getViewer().setEditDomain(getEditDomain());
            getViewer().setEditPartFactory(new WidgetTreeEditpartFactory());
            var showIndexInTreeViewAction = new ShowIndexInTreeViewAction(getViewer());
            ContextMenuProvider provider = new OPIEditorContextMenuProvider(OPIEditor.this.getGraphicalViewer(),
                    getActionRegistry()) {
                @Override
                public void buildContextMenu(IMenuManager menu) {
                    super.buildContextMenu(menu);
                    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, showIndexInTreeViewAction);
                }
            };
            getViewer().setContextMenu(provider);
            getSite().registerContextMenu("org.csstudio.opibuilder.outline.contextmenu", provider, this);
            getSite().setSelectionProvider(getViewer());
            getViewer().setKeyHandler(getCommonKeyHandler());
            getViewer().addDropTargetListener(
                    (TransferDropTargetListener) new TemplateTransferDropTargetListener(getViewer()));

            // status line listener
            addSelectionChangedListener(new ISelectionChangedListener() {
                private IStatusLineManager statusLine = getSite().getActionBars().getStatusLineManager();

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    Display.getCurrent().asyncExec(() -> updateStatusLine(statusLine));
                }
            });

            var tbm = getSite().getActionBars().getToolBarManager();
            showOutlineAction = new Action() {
                @Override
                public void run() {
                    showPage(ID_OUTLINE);
                }
            };
            showOutlineAction.setImageDescriptor(
                    OPIBuilderPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/tree_mode.gif"));
            showOutlineAction.setToolTipText("Show Tree View ");
            tbm.add(showOutlineAction);
            showOverviewAction = new Action() {
                @Override
                public void run() {
                    showPage(ID_OVERVIEW);
                }
            };
            showOverviewAction.setImageDescriptor(
                    OPIBuilderPlugin.imageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/overview.gif"));
            showOverviewAction.setToolTipText("Show Overview");
            tbm.add(showOverviewAction);
            showPage(ID_OUTLINE);
        }

        @Override
        public void createControl(Composite parent) {
            pageBook = new PageBook(parent, SWT.NONE);
            outline = getViewer().createControl(pageBook);
            // a hack to make unselectable widgets in editor unselectable too in the tree viewer.
            var tree = ((Tree) getViewer().getControl());
            tree.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    var selection = getViewer().getSelection();
                    if (selection instanceof IStructuredSelection) {
                        for (var o : ((IStructuredSelection) selection).toArray()) {
                            if (o instanceof EditPart) {
                                var editPart = (EditPart) getGraphicalViewer().getEditPartRegistry()
                                        .get(((EditPart) o).getModel());
                                if (editPart != null && !(editPart.isSelectable())) {
                                    getViewer().deselect((EditPart) o);
                                }
                            }
                        }
                    }
                }
            });

            overview = new Canvas(pageBook, SWT.NONE);
            pageBook.showPage(outline);
            configureOutlineViewer();
            hookOutlineViewer();
            initializeOutlineViewer();
        }

        @Override
        public void dispose() {
            unhookOutlineViewer();
            if (thumbnail != null) {
                thumbnail.deactivate();
                thumbnail = null;
            }
            super.dispose();
            outlinePage = null;
            outlinePage = null;
        }

        @Override
        public <T> T getAdapter(Class<T> type) {
            if (type == ISaveablePart.class) {
                return null; // we should not return the saveable part, because the outline itself is not saveable, the
            } else if (type == ZoomManager.class) {
                return type.cast(getGraphicalViewer().getProperty(ZoomManager.class.toString()));
            } else if (type == CommandStack.class) {
                return type.cast(getCommandStack());
            }
            return type.cast(OPIEditor.this.getAdapter(type));
        }

        @Override
        public Control getControl() {
            return pageBook;
        }

        protected void hookOutlineViewer() {
            getSelectionSynchronizer().addViewer(getViewer());
        }

        protected void initializeOutlineViewer() {
            setContents(getDisplayModel());
        }

        protected void initializeOverview() {
            var lws = new LightweightSystem(overview);
            var rep = getGraphicalViewer().getRootEditPart();
            if (rep instanceof ScalableFreeformRootEditPart) {
                var root = (ScalableFreeformRootEditPart) rep;
                thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
                thumbnail.setBorder(new MarginBorder(3));
                thumbnail.setSource(root.getLayer(LayerConstants.PRINTABLE_LAYERS));
                lws.setContents(thumbnail);
                disposeListener = e -> {
                    if (thumbnail != null) {
                        thumbnail.deactivate();
                        thumbnail = null;
                    }
                };
                getFigureCanvas().addDisposeListener(disposeListener);
            }
        }

        public void setContents(Object contents) {
            getViewer().setContents(contents);
        }

        protected void showPage(int id) {
            if (id == ID_OUTLINE) {
                showOutlineAction.setChecked(true);
                showOverviewAction.setChecked(false);
                pageBook.showPage(outline);
                if (thumbnail != null) {
                    thumbnail.setVisible(false);
                }
            } else if (id == ID_OVERVIEW) {
                if (thumbnail == null) {
                    initializeOverview();
                }
                showOutlineAction.setChecked(false);
                showOverviewAction.setChecked(true);
                pageBook.showPage(overview);
                thumbnail.setVisible(true);
            }
        }

        protected void unhookOutlineViewer() {
            getSelectionSynchronizer().removeViewer(getViewer());
            if (disposeListener != null && getFigureCanvas() != null && !getFigureCanvas().isDisposed()) {
                getFigureCanvas().removeDisposeListener(disposeListener);
            }
        }

        public GraphicalViewer getGraphicalViewer() {
            return OPIEditor.this.getGraphicalViewer();
        }

        /*
         * Override this function, so the selection if actually provided by OPIEditor graphical viewer.
         */
        @Override
        public ISelection getSelection() {
            if (getGraphicalViewer() == null) {
                return StructuredSelection.EMPTY;
            }
            return getGraphicalViewer().getSelection();
        }
    }
}
