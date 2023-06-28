/*******************************************************************************
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ACTIONS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_COLOR;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_BACKGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_COLOR_FOREGROUND;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_FONT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_NAME;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_SRC_CONNECTIONS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_TGT_CONNECTIONS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_TOOLTIP;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_VISIBLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_XPOS;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_YPOS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.FixedPositionAnchor.AnchorPosition;
import org.csstudio.opibuilder.editpolicies.WidgetComponentEditPolicy;
import org.csstudio.opibuilder.editpolicies.WidgetNodeEditPolicy;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.properties.WidgetPropertyChangeListener;
import org.csstudio.opibuilder.script.ScriptData;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.util.BOYPVFactory;
import org.csstudio.opibuilder.util.OPIBuilderMacroUtil;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.TooltipLabel;
import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LabeledBorder;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.UIJob;
import org.yamcs.studio.data.IPV;

public abstract class AbstractBaseEditPart extends AbstractGraphicalEditPart implements NodeEditPart {

    public class BaseEditPartActionFilter implements IActionFilter {
        @Override
        public boolean testAttribute(Object target, String name, String value) {
            if (name.equals("executionMode") && value.equals("EDIT_MODE")
                    && getExecutionMode() == ExecutionMode.EDIT_MODE) {
                return true;
            }
            if (name.equals("executionMode") && value.equals("RUN_MODE")
                    && getExecutionMode() == ExecutionMode.RUN_MODE) {
                return true;
            }
            if (name.equals("hasPVs") && value.equals("true")) {
                return (getAllPVs() != null && getAllPVs().size() > 0);
            }
            return false;
        }
    }

    private boolean isSelectable = true;

    protected Map<String, WidgetPropertyChangeListener> propertyListenerMap;

    private ExecutionMode executionMode;

    private IWorkbenchPartSite site;

    private TooltipLabel tooltipLabel;

    private Map<String, Object> externalObjectsMap;

    private Runnable displayDisposeListener;

    private Map<String, IPV> pvMap = new HashMap<>();

    private ConnectionHandler connectionHandler;

    private List<ScriptData> scriptDataList;

    protected Map<String, ConnectionAnchor> anchorMap;

    private boolean hasStartedPVs = false;

    public AbstractBaseEditPart() {
        propertyListenerMap = new HashMap<>();
    }

    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
            initFigure(getFigure());

            // add listener to all properties.
            for (var id : getWidgetModel().getAllPropertyIDs()) {
                var property = getWidgetModel().getProperty(id);
                if (property != null) {
                    var listener = new WidgetPropertyChangeListener(this, property);
                    property.addPropertyChangeListener(listener);
                    propertyListenerMap.put(id, listener);

                    property.setExecutionMode(executionMode);
                    property.setWidgetModel(getWidgetModel());
                }

            }
            registerBasePropertyChangeHandlers();
            registerPropertyChangeHandlers();

            if (executionMode == ExecutionMode.RUN_MODE) {
                // hook open display action
                var allPropIds = getWidgetModel().getAllPropertyIDs();
                if (allPropIds.contains(PROP_ACTIONS) && allPropIds.contains(PROP_ENABLED)) {
                    hookMouseClickAction();
                }

                // script and rules execution
                var scriptsInput = getWidgetModel().getScriptsInput();
                scriptDataList = new ArrayList<>(scriptsInput.getScriptList());
                for (var rd : getWidgetModel().getRulesInput().getRuleDataList()) {
                    scriptDataList.add(rd.convertToScriptData());
                }
                for (var scriptData : scriptDataList) {
                    var pvArray = new IPV[scriptData.getPVList().size()];
                    var i = 0;
                    for (var pvTuple : scriptData.getPVList()) {
                        var pvName = pvTuple.pvName;
                        if (pvMap.containsKey(pvName)) {
                            pvArray[i] = pvMap.get(pvName);
                        } else {
                            try {
                                var pv = BOYPVFactory.createPV(pvName, 2);
                                pvMap.put(pvName, pv);
                                addToConnectionHandler(pvName, pv);
                                pvArray[i] = pv;
                            } catch (Exception e) {
                                var message = NLS.bind("Unable to connect to PV: {0}! \n"
                                        + "This may cause error when executing the script.", pvName);
                                OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                                pvArray[i] = null;
                            }
                        }
                        i++;
                    }

                    ScriptService.getInstance().registerScript(scriptData, AbstractBaseEditPart.this, pvArray);

                    UIBundlingThread.getInstance().addRunnable(() -> {
                        if (!isActive()) {
                            // already deactivated
                            return;
                        }
                        hasStartedPVs = true;
                        for (var pv : pvArray) {
                            if (pv != null && !pv.isStarted()) {
                                try {
                                    pv.start();
                                } catch (Exception e) {
                                    OPIBuilderPlugin.getLogger().log(Level.WARNING,
                                            "Unable to start PV " + pv.getName(), e);
                                }
                            }
                        }
                    });
                }
            }
            doActivate();
        }

        // Rap specified code
        displayDisposeListener = this::deactivate;
    }

    /**
     * Subclass should do the activate things in this method. This method is last called from {@link #activate()}. If
     * there is code to be called as the first thing in {@link #activate()}, it can be put in {@link #doCreateFigure()}.
     */
    protected void doActivate() {
    }

    /**
     * Subclass should do the deActivate things in this method. This is the first called in {@link #deactivate()}.
     */
    protected void doDeActivate() {
    }

    protected void addToConnectionHandler(String pvName, IPV pv) {
        if (connectionHandler == null) {
            connectionHandler = createConnectionHandler();
        }
        connectionHandler.addPV(pvName, pv);
    }

    /**
     * Calculate the border for the widget with assume that the widget is connected.
     *
     * @return the border.
     */
    public Border calculateBorder() {
        return BorderFactory.createBorder(getWidgetModel().getBorderStyle(), getWidgetModel().getBorderWidth(),
                getWidgetModel().getBorderColor(), getWidgetModel().getName());
    }

    protected ConnectionHandler createConnectionHandler() {
        return new ConnectionHandler(this);
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new WidgetComponentEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new WidgetNodeEditPolicy());
    }

    @Override
    protected IFigure createFigure() {
        var figure = doCreateFigure();
        return figure;
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            doDeActivate();
            var input = getWidgetModel().getActionsInput();
            for (var a : input.getActionsList()) {
                a.dispose();
            }
            super.deactivate();
            // remove listener from all properties.
            for (var id : getWidgetModel().getAllPropertyIDs()) {
                getWidgetModel().getProperty(id).removeAllPropertyChangeListeners();// removePropertyChangeListener(propertyListenerMap.get(id));
            }
            if (executionMode == ExecutionMode.RUN_MODE) {
                // remove script listeners before stopping PV.
                for (var scriptData : scriptDataList) {
                    ScriptService.getInstance().unRegisterScript(scriptData);
                }
                if (hasStartedPVs) {
                    // this is just a guard statement
                    // if the widget was deactivated before it became fully active (and connected its pv),
                    // we should not attempt to stop those pvs; this can happen with linking container
                    for (var pv : pvMap.values().toArray()) {
                        ((IPV) pv).stop();
                    }
                }
            }
            propertyListenerMap.clear();
            // propertyListenerMap = null;
        }
    }

    /**
     * Create and initialize the widget figure with the property values in model.
     *
     * @return the widget figure
     */
    protected abstract IFigure doCreateFigure();

    /**
     * Resizes the figure. Use {@link AbstractBaseEditPart} to implement more complex refreshing behavior.
     *
     * @param refreshableFigure
     *            the figure
     */
    protected synchronized void doRefreshVisuals(IFigure refreshableFigure) {
        super.refreshVisuals();
        var model = getWidgetModel();
        var parent = (GraphicalEditPart) getParent();
        if (parent != null) {
            parent.setLayoutConstraint(this, refreshableFigure, new Rectangle(model.getLocation(), model.getSize()));
        }
    }

    /**
     * Run a widget action which is attached to the widget.
     *
     * @param index
     *            the index of the action in the actions list.
     */
    public void executeAction(int index) {
        AbstractWidgetAction action;
        try {
            action = getWidgetModel().getActionsInput().getActionsList().get(index);
            if (action != null) {
                action.run();
            } else {
                throw new IndexOutOfBoundsException();
            }
        } catch (IndexOutOfBoundsException e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE,
                    NLS.bind("No action at index {0} is configured for {1}", index, getWidgetModel().getName()));
        }
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key == IActionFilter.class) {
            return new BaseEditPartActionFilter();
        }
        return super.getAdapter(key);
    }

    public String getName() {
        return getWidgetModel().getName();
    }

    /**
     * @return the map with all PVs. PV name is the key. It is not allowed to change the Map by client. null if no PV on
     *         this widget.
     */
    public Map<String, IPV> getAllPVs() {
        if (getConnectionHandler() != null) {
            return getConnectionHandler().getAllPVs();
        }
        return null;
    }

    /**
     * Get PV attached to this widget by pv name. It includes the PVs in Rules and Scripts.
     *
     * @param pvName
     *            name of the PV.
     * @return the PV. null if no such PV exists.
     */
    public IPV getPVByName(String pvName) {
        if (getConnectionHandler() != null) {
            return getConnectionHandler().getAllPVs().get(pvName);
        }
        return null;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public Runnable getDisplayDisposeListener() {
        return displayDisposeListener;
    }

    /**
     * @return the executionMode
     */
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    /**
     *
     * Get the external object by name.
     *
     * @return the external object. null if no such an object was set before.
     * @deprecated Use {@link #getVar(String)} instead.
     */
    @Deprecated
    public synchronized Object getExternalObject(String name) {
        return getVar(name);
    }

    /**
     * Get the value of a variable which is attached to this widget by {@link #setVar(String, Object)}.
     *
     * @param varName
     *            name of the variable
     * @return value of the variable. null if no variable in this name has been attached to this widget.
     */
    public synchronized Object getVar(String varName) {
        if (externalObjectsMap != null) {
            return externalObjectsMap.get(varName);
        }
        return null;
    }

    /**
     * @return the default {@link AbstractWidgetAction} when mouse click this widget.
     */
    public List<AbstractWidgetAction> getHookedActions() {
        var actionsInput = getWidgetModel().getActionsInput();
        if (actionsInput != null && actionsInput.getActionsList().size() > 0
                && (actionsInput.isFirstActionHookedUpToWidget() || actionsInput.isHookUpAllActionsToWidget())) {
            if (actionsInput.isHookUpAllActionsToWidget()) {
                return getWidgetModel().getActionsInput().getActionsList();
            } else {
                return getWidgetModel().getActionsInput().getActionsList().subList(0, 1);
            }
        }
        return null;
    }

    /**
     * Get property value of the widget.
     *
     * @param prop_id
     *            the property id.
     * @return the property value.
     */
    public Object getPropertyValue(String prop_id) {
        return getWidgetModel().getPropertyValue(prop_id);
    }

    /**
     * Get macro value from this widget.
     *
     * @param macroName
     *            the name of the macro.
     * @return the value of the macro.
     */
    public String getMacroValue(String macroName) {
        return OPIBuilderMacroUtil.getWidgetMacroMap(getWidgetModel()).get(macroName);
    }

    public AbstractWidgetModel getWidgetModel() {
        return (AbstractWidgetModel) getModel();
    }

    /**
     * Hook the default AbstractOpenOPIAction with mouse click.
     */
    protected void hookMouseClickAction() {
        var actions = getHookedActions();
        if (getWidgetModel().isEnabled() && actions != null) {
            figure.setCursor(Cursors.HAND);
            figure.addMouseListener(new HookedActionsMouseListener(actions));
        }
    }

    /**
     * initialize the figure
     *
     * @param figure
     */
    protected void initFigure(IFigure figure) {
        if (figure == null) {
            throw new IllegalArgumentException("Editpart does not provide a figure!");
        }
        var allPropIds = getWidgetModel().getAllPropertyIDs();
        if (allPropIds.contains(PROP_COLOR_BACKGROUND)) {
            figure.setBackgroundColor(CustomMediaFactory.getInstance().getColor(getWidgetModel().getBackgroundColor()));
        }

        if (allPropIds.contains(PROP_COLOR_FOREGROUND)) {
            figure.setForegroundColor(CustomMediaFactory.getInstance().getColor(getWidgetModel().getForegroundColor()));
        }

        if (allPropIds.contains(PROP_FONT)) {
            figure.setFont(getWidgetModel().getFont().getSWTFont());
        }

        if (allPropIds.contains(PROP_VISIBLE)) {
            figure.setVisible(getExecutionMode() == ExecutionMode.RUN_MODE ? getWidgetModel().isVisible() : true);
        }

        if (allPropIds.contains(PROP_ENABLED)) {
            figure.setEnabled(getWidgetModel().isEnabled());
        }

        if (allPropIds.contains(PROP_WIDTH) && allPropIds.contains(PROP_HEIGHT)) {
            figure.setSize(getWidgetModel().getSize());
        }

        if (allPropIds.contains(PROP_BORDER_COLOR) && allPropIds.contains(PROP_BORDER_STYLE)
                && allPropIds.contains(PROP_BORDER_WIDTH)) {
            figure.setBorder(BorderFactory.createBorder(getWidgetModel().getBorderStyle(),
                    getWidgetModel().getBorderWidth(), getWidgetModel().getBorderColor(), getWidgetModel().getName()));
        }

        if (allPropIds.contains(PROP_TOOLTIP)) {
            if (!getWidgetModel().getTooltip().equals("")) {
                tooltipLabel = new TooltipLabel(this);
                figure.setToolTip(tooltipLabel);
            }
        }
        // Disable tab traversal
        figure.setFocusTraversable(false);
        figure.setRequestFocusEnabled(false);
    }

    @Override
    public boolean isSelectable() {
        return isSelectable;
    }

    @Override
    protected void refreshVisuals() {
        doRefreshVisuals(getFigure());
    }

    protected void registerBasePropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_XPOS, (oldValue, newValue, figure) -> {
            refreshVisuals();
            return false;
        });
        setPropertyChangeHandler(PROP_YPOS, (oldValue, newValue, figure) -> {
            refreshVisuals();
            return false;
        });
        setPropertyChangeHandler(PROP_WIDTH, (oldValue, newValue, figure) -> {
            refreshVisuals();
            return false;
        });
        setPropertyChangeHandler(PROP_HEIGHT, (oldValue, newValue, figure) -> {
            refreshVisuals();
            return false;
        });

        // add connection should not be ignored by widget listener.
        getWidgetModel().getProperty(PROP_SRC_CONNECTIONS)
                .addPropertyChangeListener(evt -> refreshSourceConnections());

        getWidgetModel().getProperty(PROP_TGT_CONNECTIONS)
                .addPropertyChangeListener(evt -> refreshTargetConnections());

        setPropertyChangeHandler(PROP_COLOR_BACKGROUND, (oldValue, newValue, figure) -> {
            figure.setBackgroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            return true;
        });

        setPropertyChangeHandler(PROP_COLOR_FOREGROUND, (oldValue, newValue, figure) -> {
            figure.setForegroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            return true;
        });

        setPropertyChangeHandler(PROP_FONT, (oldValue, newValue, figure) -> {
            figure.setFont(((OPIFont) newValue).getSWTFont());
            return false;
        });

        setPropertyChangeHandler(PROP_BORDER_STYLE, (oldValue, newValue, figure) -> {
            setFigureBorder(calculateBorder());
            return true;
        });
        setPropertyChangeHandler(PROP_BORDER_COLOR, (oldValue, newValue, figure) -> {
            setFigureBorder(calculateBorder());
            return true;
        });
        setPropertyChangeHandler(PROP_BORDER_WIDTH, (oldValue, newValue, figure) -> {
            setFigureBorder(calculateBorder());
            return true;
        });

        setPropertyChangeHandler(PROP_NAME, (oldValue, newValue, figure) -> {
            if (figure.getBorder() instanceof LabeledBorder) {
                figure.setBorder(
                        BorderFactory.createBorder(getWidgetModel().getBorderStyle(), getWidgetModel().getBorderWidth(),
                                getWidgetModel().getBorderColor(), getWidgetModel().getName()));
            }
            return true;
        });
        setPropertyChangeHandler(PROP_FONT, (oldValue, newValue, figure) -> {
            if (figure.getBorder() instanceof LabeledBorder) {
                figure.setBorder(
                        BorderFactory.createBorder(getWidgetModel().getBorderStyle(), getWidgetModel().getBorderWidth(),
                                getWidgetModel().getBorderColor(), getWidgetModel().getName()));
            }
            return true;
        });

        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, figure) -> {
            figure.setEnabled((Boolean) newValue);
            figure.repaint();
            return true;
        });

        setPropertyChangeHandler(PROP_TOOLTIP, (oldValue, newValue, figure) -> {
            if (newValue.toString().equals("")) {
                figure.setToolTip(null);
            } else {
                if (tooltipLabel == null) {
                    tooltipLabel = new TooltipLabel(AbstractBaseEditPart.this);
                }
                figure.setToolTip(tooltipLabel);
            }
            return false;
        });

        setPropertyChangeHandler(PROP_VISIBLE, (oldValue, newValue, refreshableFigure) -> {
            boolean visible = (Boolean) newValue;
            var figure = getFigure();
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                figure.setVisible(visible);
            } else {
                if (!visible) {
                    figure.setVisible(false);

                    var job = new UIJob("reset") {
                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            figure.setVisible(true);
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule(2000);
                }
            }
            return true;
        });
    }

    /**
     * Register the property change handlers. Subclass should register its property change handlers in this method.
     */
    protected abstract void registerPropertyChangeHandlers();

    /**
     * Remove all the property change handlers on the specified property.
     */
    public void removeAllPropertyChangeHandlers(String propID) {
        var listener = propertyListenerMap.get(propID);
        if (listener != null) {
            listener.removeAllHandlers();
        }
    }

    protected void removeFromConnectionHandler(String pvName) {
        if (connectionHandler != null) {
            connectionHandler.removePV(pvName);
        }
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
        getWidgetModel().setExecutionMode(executionMode);
        /*
         * for(var id : getWidgetModel().getAllPropertyIDs()){ var property =
         * getWidgetModel().getProperty(id); if(property != null){ property.setExecutionMode(executionMode);
         * property.setWidgetModel(getWidgetModel()); }
         *
         * }
         */
    }

    public IWorkbenchPartSite getSite() {
        return site;
    }

    public void setSite(IWorkbenchPartSite site) {
        this.site = site;
    }

    /**
     * Add/modify an external object from javascript.
     *
     * @param name
     *            the name of the object.
     * @param var
     *            the object.
     *
     * @deprecated use {@link #setVar(String, Object)} instead.
     *
     */
    @Deprecated
    public synchronized void setExternalObject(String name, Object var) {
        setVar(name, var);
    }

    /**
     * Set variable value. If the variable does not exist, it will be added to this widget first. *
     *
     * @param varName
     *            name of the variable.
     * @param varValue
     *            value of the variable, which can be any type.
     */
    public synchronized void setVar(String varName, Object varValue) {
        if (externalObjectsMap == null) {
            externalObjectsMap = new HashMap<>();
        }
        externalObjectsMap.put(varName, varValue);
    }

    /**
     * Set border of the figure. If the border has been set for connection or null value indication, the figure's border
     * will not change.
     *
     * @param border
     */
    protected void setFigureBorder(Border border) {
        if (getConnectionHandler() != null
                && (!getConnectionHandler().isConnected() || getConnectionHandler().isHasNullValue())) {
            return;
        }
        getFigure().setBorder(border);
    }

    /**
     * Registers a property change handler for the specified property id.
     *
     * @param propertyId
     *            the property id
     * @param handler
     *            the property change handler
     */
    public void setPropertyChangeHandler(String propertyId, IWidgetPropertyChangeHandler handler) {
        var listener = propertyListenerMap.get(propertyId);
        if (listener != null) {
            listener.addHandler(handler);
        }
    }

    /**
     * Set the property value of the widget. If the new value is same as the old value, it will be ignored.
     *
     * @param prop_id
     *            the property id.
     * @param value
     *            the value.
     */
    public void setPropertyValue(String prop_id, Object value) {
        getWidgetModel().setPropertyValue(prop_id, value);
    }

    /**
     * Set the property value of the widget.
     *
     * @param prop_id
     *            the property id.
     * @param value
     *            the value.
     * @param forceFire
     *            If true, the property will be set again even if the new value is same as old value. If false, only
     *            property value will be set and no listener will be fired.
     */
    public void setPropertyValue(String prop_id, Object value, boolean forceFire) {
        getWidgetModel().setPropertyValue(prop_id, value, forceFire);
    }

    public void setSelectable(boolean isSelectable) {
        this.isSelectable = isSelectable;
    }

    /**
     * Set this widget to be enabled.
     *
     * @param enable
     *            true if the widget should be enabled.
     */
    public void setEnabled(boolean enable) {
        getWidgetModel().setEnabled(enable);
    }

    /**
     * Set this widget's visibility.
     *
     * @param visible
     *            true if the widget should be visible.
     */
    public void setVisible(boolean visible) {
        getWidgetModel().setPropertyValue(PROP_VISIBLE, visible);
    }

    /**
     * Set X position of the widget
     *
     * @param x
     *            x position in pixel which is relative to its parent.
     */
    public void setX(Number x) {
        getWidgetModel().setPropertyValue(PROP_XPOS, x);
    }

    /**
     * Set Y position of the widget
     *
     * @param y
     *            y position in pixel which is relative to its parent.
     */
    public void setY(Number y) {
        getWidgetModel().setPropertyValue(PROP_YPOS, y);
    }

    /**
     * Set widget's width
     *
     * @param width
     *            width in pixel.
     */
    public void setWidth(Number width) {
        getWidgetModel().setPropertyValue(PROP_WIDTH, width);
    }

    /**
     * Set widget's height
     *
     * @param height
     *            height in pixel.
     */
    public void setHeight(Number height) {
        getWidgetModel().setPropertyValue(PROP_HEIGHT, height);
    }

    @Override
    protected List<ConnectionModel> getModelSourceConnections() {
        return getWidgetModel().getSourceConnections();
    }

    @Override
    protected List<ConnectionModel> getModelTargetConnections() {
        return getWidgetModel().getTargetConnections();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        if (anchorMap == null) {
            fillAnchorMap();
        }
        var conn = (ConnectionModel) connection.getModel();
        return anchorMap.get(conn.getSourceTerminal());
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        var p = new Point(((DropRequest) request).getLocation());
        return getClosestAnchorAt(p);
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        if (anchorMap == null) {
            fillAnchorMap();
        }
        var conn = (ConnectionModel) connection.getModel();
        return anchorMap.get(conn.getTargetTerminal());
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        var p = new Point(((DropRequest) request).getLocation());
        return getClosestAnchorAt(p);
    }

    /**
     * Get name of the terminal by anchor
     *
     * @param anchor
     *            the anchor
     * @return terminal name of the anchor. null if no name was found.
     */
    public String getTerminalNameFromAnchor(ConnectionAnchor anchor) {
        if (anchorMap == null) {
            fillAnchorMap();
        }
        for (var entry : anchorMap.entrySet()) {
            if (entry.getValue().equals(anchor)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Fill the anchor map with all predefined anchors.
     */
    protected void fillAnchorMap() {
        anchorMap = new HashMap<>(AnchorPosition.values().length);
        for (var pos : AnchorPosition.values()) {
            anchorMap.put(pos.name(), new FixedPositionAnchor(getFigure(), pos));
        }
    }

    /**
     * Get the anchor map on this widget. Caller should not change the map.
     *
     * @return all the anchors on this widget as in a anchor map. key is the connection terminal name.
     */
    public Map<String, ConnectionAnchor> getAnchorMap() {
        if (anchorMap == null) {
            fillAnchorMap();
        }
        return anchorMap;
    }

    /**
     * Get the closest anchor to point p.
     *
     * @param p
     *            the reference point
     * @return the closest anchor to point p
     */
    protected ConnectionAnchor getClosestAnchorAt(Point p) {
        if (anchorMap == null) {
            fillAnchorMap();
        }
        ConnectionAnchor closest = null;
        double min = Long.MAX_VALUE;
        for (var anchor : anchorMap.values()) {
            var p2 = anchor.getLocation(null);
            var d = p.getDistance(p2);
            if (d < min) {
                min = d;
                closest = anchor;
            }
        }
        return closest;
    }

    @Override
    public String toString() {
        return getWidgetModel().getName();
    }

    /**
     * The value of the widget that is in representing. It is not the value of the attached PV even though they are
     * equals in most cases. The value type is specified by the widget, for example, boolean for boolean widget, double
     * for meter and gauge.
     *
     * @return The value of the widget.
     */
    public Object getValue() {
        return null;
    }

    /**
     * Set the value of the widget. This only takes effect on the visual presentation of the widget and will not write
     * the value to the PV attached to this widget. Since setting value to a widget usually results in figure repaint,
     * this method should be called in UI thread. To call it in non-UI thread, see {@link #setValueInUIThread(Object)}.
     *
     * @param value
     *            the value to be set. It must be the compatible type for the widget. For example, a boolean widget only
     *            accept boolean or number.
     * @throws RuntimeException
     *             if the value is not an acceptable type.
     */
    public void setValue(Object value) {
        throw new RuntimeException("widget.setValue() does not accept " + value.getClass().getSimpleName());
    }

    /**
     * Call {@link #setValue(Object)} in UI Thread. This method can be called in non-UI thread.
     */
    public void setValueInUIThread(Object value) {
        UIBundlingThread.getInstance().addRunnable(getViewer().getControl().getDisplay(), () -> setValue(value));
    }
}
