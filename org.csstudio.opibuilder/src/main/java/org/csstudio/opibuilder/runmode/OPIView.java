/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.DisplayModel;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.ViewPart;

/**
 * RCP 'View' for display runtime
 *
 * <p>
 * Similar to an RCP editor it is associated to an 'input', but provides only a view to that *.opi, executing its
 * content.
 *
 * <p>
 * Being a 'View' allows save/restore within a 'Perspective'.
 *
 * <p>
 * RCP distinguishes instances via their secondary view ID. Each instances has a memento for storing arbitrary data,
 * which we use for the 'input' (*.opi path and macros).
 *
 * <p>
 * Secondary view IDs must be unique, they can't simply increment from "1" each time CSS is started because then a view
 * with a previously used secondary ID will show the old content.
 *
 * <p>
 * RCP only triggers <code>saveState</code> for views that are currently visible, typically on exit. This view will
 * write the memento info directly to the underlying E4 model whenever the input changes.
 */
public class OPIView extends ViewPart implements IOPIRuntime {
    /**
     * View ID registered in plugin.xml for use as a 'default' view.
     *
     * <p>
     * For views to be displayed in designated OPIRunnerPerspective.Position, that Position.name() is added to the basic
     * ID
     */
    public static final String ID = "org.csstudio.opibuilder.opiView";

    /** Debug option, see .options file at plugin root */
    public static final boolean debug = "true"
            .equalsIgnoreCase(Platform.getDebugOption(OPIBuilderPlugin.PLUGIN_ID + "/views"));

    /** Memento tags */
    private static final String TAG_INPUT = "input", TAG_FACTORY_ID = "factory_id", TAG_MEMENTO = "memento";

    protected OPIRuntimeDelegate opiRuntimeDelegate;

    private IViewSite site;
    private IEditorInput input;

    private OPIRuntimeToolBarDelegate opiRuntimeToolBarDelegate;

    private static boolean openFromPerspective = false;

    public OPIView() {
        opiRuntimeDelegate = new OPIRuntimeDelegate(this);
    }

    /** @return Unique secondary view ID for this instance of CSS */
    public static String createSecondaryID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void dispose() {
        if (opiRuntimeDelegate != null) {
            opiRuntimeDelegate.dispose();
            opiRuntimeDelegate = null;
        }
        if (opiRuntimeToolBarDelegate != null) {
            opiRuntimeToolBarDelegate.dispose();
            opiRuntimeToolBarDelegate = null;
        }
        super.dispose();
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.site = site;

        if (debug) {
            System.out.println(site.getId() + ":" + site.getSecondaryId() + " opened "
                    + (memento == null ? ", no memento" : "with memento"));
        }

        if (memento == null) {
            memento = findMementoFromPlaceholder();
        }
        if (memento == null) {
            return;
        }
        // Load previously displayed input from memento
        var factoryID = memento.getString(TAG_FACTORY_ID);
        if (factoryID == null) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, toString() + " has memento with empty factory ID");
            return;
        }
        var inputMem = memento.getChild(TAG_INPUT);
        var factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
        if (factory == null) {
            throw new PartInitException(
                    NLS.bind("Cannot instantiate input element factory {0} for OPIView", factoryID));
        }

        var element = factory.createElement(inputMem);
        if (!(element instanceof IEditorInput)) {
            throw new PartInitException("Instead of IEditorInput, " + factoryID + " returned " + element);
        }

        // Set input, but don't persist to memento because we just read it from memento
        setOPIInput((IEditorInput) element, false);
    }

    /**
     * Retrieve memento persisted in MPlaceholder if present.
     *
     * @return memento persisted in the placeholder.
     */
    private IMemento findMementoFromPlaceholder() {
        IMemento memento = null;
        var placeholder = findPlaceholder();
        if (placeholder != null) {
            if (placeholder.getPersistedState().containsKey(TAG_MEMENTO)) {
                var mementoString = placeholder.getPersistedState().get(TAG_MEMENTO);
                memento = loadMemento(mementoString);
            }
        }
        return memento;
    }

    /**
     * Create memento from string.
     *
     * @param mementoString
     * @return
     */
    private IMemento loadMemento(String mementoString) {
        var reader = new StringReader(mementoString);
        try {
            return XMLMemento.createReadRoot(reader);
        } catch (WorkbenchException e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to load memento", e);
            return null;
        }
    }

    /**
     * Find the MPlaceholder corresponding to this MPart in the MPerspective. This may have persisted information
     * relevant to loading this OPIView.
     *
     * @return corresponding placeholder
     */
    private MPlaceholder findPlaceholder() {
        // do not remove casting - RAP 3.0 still needs it
        var localContext = getViewSite().getService(IEclipseContext.class);
        var part = localContext.get(MPart.class);
        var service = PlatformUI.getWorkbench().getService(EModelService.class);
        var globalContext = PlatformUI.getWorkbench().getService(IEclipseContext.class);
        var app = globalContext.get(MApplication.class);
        var phs = service.findElements(app, null, MPlaceholder.class, null);
        for (var ph : phs) {
            if (ph.getRef() == part) {
                return ph;
            }
        }
        return null;
    }

    /**
     * @param input
     *            Display file that this view should execute
     * @param persist
     *            Persist the input to memento?
     */
    public void setOPIInput(IEditorInput input, boolean persist) throws PartInitException {
        if (debug) {
            var view = getViewSite();
            System.out.println(view.getId() + ":" + view.getSecondaryId() + " displays " + input.getName());
        }
        this.input = input;
        setTitleToolTip(input.getToolTipText());
        opiRuntimeDelegate.init(site, input);
        if (opiRuntimeToolBarDelegate != null) {
            opiRuntimeToolBarDelegate.setActiveOPIRuntime(this);
        }

        if (persist) {
            persist();
        }
    }

    /**
     * @param input
     *            Display file that this view should execute
     */
    @Override
    public void setOPIInput(IEditorInput input) throws PartInitException {
        // Persist _now_.
        // Framework only saves memento on exit.
        // If view happens to be hidden on exit, its input won't be saved,
        // so when it's later restored, you get an empty view.
        setOPIInput(input, true);
    }

    @Override
    public void createPartControl(Composite parent) {
        opiRuntimeDelegate.createGUI(parent);
        createToolbarButtons();
    }

    private Rectangle getBounds() {
        Rectangle bounds;
        if (opiRuntimeDelegate.getDisplayModel() != null) {
            bounds = opiRuntimeDelegate.getDisplayModel().getBounds();
        } else {
            bounds = new Rectangle(0, 0, 800, 600);
        }
        return bounds;
    }

    /**
     * Position the view according to location and size from the model.
     *
     * If the model location has negative values or is (0, 0), position within the parent window.
     */
    public void positionFromModel() {
        Composite parent = getSite().getShell();
        var bounds = getBounds();
        // Resize to that of model from OPI
        parent.getShell().setSize(bounds.width + 45, bounds.height + 65);
        // If OPI model specifies a location, honour it. Otherwise
        // place within parent window.
        if (bounds.x >= 0 && bounds.y > 1) {
            parent.getShell().setLocation(bounds.x, bounds.y);
        } else {
            var winSize = getSite().getWorkbenchWindow().getShell().getBounds();
            parent.getShell().setLocation(winSize.x + winSize.width / 5 + (int) (Math.random() * 100),
                    winSize.y + winSize.height / 8 + (int) (Math.random() * 100));
        }
    }

    public void createToolbarButtons() {
        opiRuntimeToolBarDelegate = new OPIRuntimeToolBarDelegate();
        var bars = getViewSite().getActionBars();
        opiRuntimeToolBarDelegate.init(bars, getSite().getPage());
        opiRuntimeToolBarDelegate.contributeToToolBar(bars.getToolBarManager());
        opiRuntimeToolBarDelegate.setActiveOPIRuntime(this);
    }

    /**
     * Persist the view's input "on demand".
     *
     * <p>
     * To allow saving the memento at any time.
     *
     * <p>
     * Memento is saved in the .metadata/.plugins/org.eclipse.e4.workbench/workbench.xmi inside a "persistedState"
     * element of the E4 model element.
     *
     * <p>
     * This method places it in the model just as the framework does by calling saveState() on shutdown, but allows
     * saving the state at any time.
     */
    private void persist() {

        // Obtain E4 model element for E3 view,
        // based on http://www.vogella.com/tutorials/EclipsePlugIn/article.html#eclipsecontext
        var context = getViewSite().getService(IEclipseContext.class);
        var model = context.get(MPart.class);

        // Based on org.eclipse.ui.internal.ViewReference#persist():
        //
        // XML version of memento is written to E4 model.
        // If compatibility layer changes its memento persistence,
        // this will break...
        var root = XMLMemento.createWriteRoot("view");
        saveState(root);
        var writer = new StringWriter();
        try {
            root.save(writer);
            model.getPersistedState().put(TAG_MEMENTO, writer.toString());
        } catch (Exception ex) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, toString() + " failed to persist input", ex);
        }
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        if (input == null) {
            return;
        }
        var persistable = input.getPersistable();
        if (persistable != null) {
            /*
             * Store IPersistable of the IEditorInput in a separate section since it could potentially use a tag already
             * used in the parent memento and thus overwrite data.
             */
            var persistableMemento = memento.createChild(TAG_INPUT);
            persistable.saveState(persistableMemento);
            memento.putString(TAG_FACTORY_ID, persistable.getFactoryId());
            // save the name and tooltip separately so they can be restored
            // without having to instantiate the input, which can activate
            // plugins
            // memento.putString(IWorkbenchConstants.TAG_NAME, input.getName());
            // memento.putString(IWorkbenchConstants.TAG_TOOLTIP,
            // input.getToolTipText());
            if (debug) {
                System.out.println(this + " saved to memento");
            }
        }
    }

    @Override
    public void setFocus() {
        // NOP
    }

    @Override
    public void setWorkbenchPartName(String name) {
        setPartName(name);
        setTitleToolTip(getOPIInput().getToolTipText());
    }

    // In debug mode, include view ID in tool tip
    @Override
    protected void setTitleToolTip(String tool_tip) {
        if (debug) {
            var view = getViewSite();
            tool_tip = view.getId() + ":" + view.getSecondaryId() + " - " + tool_tip;
        }
        super.setTitleToolTip(tool_tip);
    }

    public OPIRuntimeDelegate getOPIRuntimeDelegate() {
        return opiRuntimeDelegate;
    }

    @Override
    public IEditorInput getOPIInput() {
        return getOPIRuntimeDelegate().getEditorInput();
    }

    @Override
    public DisplayModel getDisplayModel() {
        return getOPIRuntimeDelegate().getDisplayModel();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (opiRuntimeDelegate == null) {
            return super.getAdapter(adapter);
        }
        var obj = opiRuntimeDelegate.getAdapter(adapter);
        if (obj != null) {
            return obj;
        } else {
            return super.getAdapter(adapter);
        }
    }

    public static boolean isOpenFromPerspective() {
        return openFromPerspective;
    }

    public static void setOpenFromPerspective(boolean openFromPerspective) {
        OPIView.openFromPerspective = openFromPerspective;
    }

    /** @return Debug info for view, shows ID and input */
    @Override
    public String toString() {
        return getViewSite().getId() + ":" + getViewSite().getSecondaryId() + ", " + input;
    }
}
