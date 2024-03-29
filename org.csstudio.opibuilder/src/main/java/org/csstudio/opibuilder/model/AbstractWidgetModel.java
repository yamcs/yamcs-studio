/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.model;

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.csstudio.opibuilder.datadefinition.WidgetScaleData;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.properties.ActionsProperty;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.ComplexDataProperty;
import org.csstudio.opibuilder.properties.FontProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.PVNameProperty;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.RulesProperty;
import org.csstudio.opibuilder.properties.ScriptProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.UnchangableStringProperty;
import org.csstudio.opibuilder.properties.UnsavableListProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.script.RulesInput;
import org.csstudio.opibuilder.script.ScriptsInput;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.util.UpgradeUtil;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgetActions.ActionsInput;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.osgi.framework.Version;

public abstract class AbstractWidgetModel implements IAdaptable, IPropertySource {

    public static final String VERSION = "1.0";

    /**
     * The Name of the widget.
     */
    public static final String PROP_NAME = "name";

    /**
     * Scripts attached to the widget.
     */
    public static final String PROP_SCRIPTS = "scripts";

    /**
     * Rules attached to the widget.
     */
    public static final String PROP_RULES = "rules";

    /**
     * X position of the widget.
     */
    public static final String PROP_XPOS = "x";

    /**
     * Y position of the widget.
     */
    public static final String PROP_YPOS = "y";

    /**
     * Width of the widget.
     */
    public static final String PROP_WIDTH = "width";

    /**
     * Height of the widget.
     */
    public static final String PROP_HEIGHT = "height";

    /**
     * Background color.
     */
    public static final String PROP_COLOR_BACKGROUND = "background_color";

    /**
     * Foreground color.
     */
    public static final String PROP_COLOR_FOREGROUND = "foreground_color";

    /**
     * Foreground color.
     */
    public static final String PROP_FONT = "font";

    /**
     * Visibility of the widget.
     */
    public static final String PROP_VISIBLE = "visible";

    /**
     * Enable status. Only effective for control widgets which will make control widget uncontrollable if this is false.
     */
    public static final String PROP_ENABLED = "enabled";

    /**
     * Actions attached to the widget, which can be accessed on runtime via context menu <code>Actions</code>.
     */
    public static final String PROP_ACTIONS = "actions";

    /**
     * Tooltip of the widget, which will show up when mouse hover on the widget. Macros are allowed and can be updated.
     * The property macro $(pv_value) could be used to show the PV value which has timestamp, value, severity and
     * status.
     */
    public static final String PROP_TOOLTIP = "tooltip";

    /**
     * Color of border.
     */
    public static final String PROP_BORDER_COLOR = "border_color";

    /**
     * Width of border.
     */
    public static final String PROP_BORDER_WIDTH = "border_width";
    /**
     * Style of border.
     */
    public static final String PROP_BORDER_STYLE = "border_style";

    /**
     * The type of the widget. This is the only property that cannot be edited. The name and type of the selected widget
     * will also be displayed on the status bar.
     */
    public static final String PROP_WIDGET_TYPE = "widget_type";

    /**
     * Unique ID of the widget, it should not be changed after generated.
     */
    public static final String PROP_WIDGET_UID = "wuid"; //

    /**
     * Source Connections.
     */
    public static final String PROP_SRC_CONNECTIONS = "src_connections";

    /**
     * Target Connections.
     */
    public static final String PROP_TGT_CONNECTIONS = "tgt_connections";

    /**
     * The options for its scale behavior.
     */
    public static final String PROP_SCALE_OPTIONS = "scale_options";

    private Map<String, AbstractWidgetProperty> propertyMap;

    /**
     * The map contains properties which are allowed to change during running.
     */
    private List<AbstractWidgetProperty> runtimePropertyList;

    private Map<String, IPropertyDescriptor> propertyDescriptors;

    private AbstractContainerModel parent;

    private LinkedHashMap<StringProperty, PVValueProperty> pvMap;

    private ExecutionMode executionMode;

    /** List of outgoing Connections. */
    private List<ConnectionModel> sourceConnections = Collections.emptyList();
    /** List of incoming Connections. */
    private List<ConnectionModel> targetConnections = Collections.emptyList();

    private Dimension originSize;

    private Point originLocation;

    private Version versionOnFile;

    public AbstractWidgetModel() {
        propertyMap = new HashMap<>();
        propertyDescriptors = new HashMap<>();
        pvMap = new LinkedHashMap<>();
        configureBaseProperties();
        configureProperties();
    }

    public void addConnection(ConnectionModel conn) {
        if (conn == null || conn.getSource() == conn.getTarget()) {
            throw new IllegalArgumentException();
        }
        if (sourceConnections == Collections.EMPTY_LIST || targetConnections == Collections.EMPTY_LIST) {
            sourceConnections = new ArrayList<>(1);
            targetConnections = new ArrayList<>(1);
        }
        if (conn.getSource() == this) {
            sourceConnections.add(conn);
            setPropertyValue(PROP_SRC_CONNECTIONS, sourceConnections, true);
        } else if (conn.getTarget() == this) {
            targetConnections.add(conn);
            setPropertyValue(PROP_TGT_CONNECTIONS, targetConnections, true);
        }
    }

    /**
     * Remove an incoming or outgoing connection from this widget.
     *
     * @param conn
     *            a non-null connection instance
     * @throws IllegalArgumentException
     *             if the parameter is null
     */
    void removeConnection(ConnectionModel conn) {
        if (conn == null) {
            throw new IllegalArgumentException();
        }
        if (conn.getSource() == this) {
            sourceConnections.remove(conn);
            setPropertyValue(PROP_SRC_CONNECTIONS, sourceConnections, true);
        } else if (conn.getTarget() == this) {
            targetConnections.remove(conn);
            setPropertyValue(PROP_TGT_CONNECTIONS, targetConnections, true);
        }
    }

    /**
     * Add a property to the widget.
     *
     * @param property
     *            the property to be added.
     */
    public void addProperty(AbstractWidgetProperty property) {
        Assert.isNotNull(property);
        property.setWidgetModel(this);
        propertyMap.put(property.getPropertyID(), property);
        if (property.isVisibleInPropSheet()) {
            propertyDescriptors.put(property.getPropertyID(), property.getPropertyDescriptor());
        }
    }

    /**
     * Add a property to the widget with the option to set it running changeable.
     *
     * @param property
     *            the property to be added.
     * @param runtimeChangeable
     *            true if this property is changeable during running. false otherwise.
     */
    public void addProperty(AbstractWidgetProperty property, boolean runtimeChangeable) {
        addProperty(property);
        if (runtimeChangeable) {
            if (runtimePropertyList == null) {
                runtimePropertyList = new ArrayList<>();
            }
            runtimePropertyList.add(property);
        }
    }

    public List<AbstractWidgetProperty> getRuntimePropertyList() {
        return runtimePropertyList;
    }

    /**
     * Add a PVNameProperty and its value property correspondingly.
     *
     * @param pvNameProperty
     * @param pvValueProperty
     */
    public void addPVProperty(PVNameProperty pvNameProperty, PVValueProperty pvValueProperty) {
        addProperty(pvNameProperty);
        addProperty(pvValueProperty);
        pvMap.put(pvNameProperty, pvValueProperty);
    }

    private void checkPropertyExist(Object propID) {
        if (!propertyMap.containsKey(propID)) {
            throw new NonExistPropertyException(getName(), propID.toString());
        }
    }

    protected void configureBaseProperties() {
        addProperty(new IntegerProperty(PROP_WIDTH, "Width", WidgetPropertyCategory.Position, 100, 1, 10000));
        addProperty(new IntegerProperty(PROP_HEIGHT, "Height", WidgetPropertyCategory.Position, 100, 1, 10000));
        addProperty(new IntegerProperty(PROP_XPOS, "X", WidgetPropertyCategory.Position, 0));
        addProperty(new IntegerProperty(PROP_YPOS, "Y", WidgetPropertyCategory.Position, 0));
        addProperty(new ColorProperty(PROP_COLOR_BACKGROUND, "Background Color", WidgetPropertyCategory.Display,
                new RGB(240, 240, 240)));
        addProperty(new ColorProperty(PROP_COLOR_FOREGROUND, "Foreground Color", WidgetPropertyCategory.Display,
                new RGB(192, 192, 192)));
        addProperty(new FontProperty(PROP_FONT, "Font", WidgetPropertyCategory.Display, MediaService.DEFAULT_FONT));
        addProperty(new ColorProperty(PROP_BORDER_COLOR, "Border Color", WidgetPropertyCategory.Border,
                new RGB(0, 128, 255)));
        addProperty(new ComboProperty(PROP_BORDER_STYLE, "Border Style", WidgetPropertyCategory.Border,
                BorderStyle.stringValues(), 0));
        addProperty(new IntegerProperty(PROP_BORDER_WIDTH, "Border Width", WidgetPropertyCategory.Border, 1, 0, 1000));
        addProperty(new BooleanProperty(PROP_ENABLED, "Enabled", WidgetPropertyCategory.Behavior, true));
        addProperty(new BooleanProperty(PROP_VISIBLE, "Visible", WidgetPropertyCategory.Behavior, true));
        addProperty(new ScriptProperty(PROP_SCRIPTS, "Scripts", WidgetPropertyCategory.Behavior));
        addProperty(new ActionsProperty(PROP_ACTIONS, "Actions", WidgetPropertyCategory.Behavior));
        addProperty(new StringProperty(PROP_TOOLTIP, "Tooltip", WidgetPropertyCategory.Display, "", true));
        addProperty(new RulesProperty(PROP_RULES, "Rules", WidgetPropertyCategory.Behavior));
        addProperty(new ComplexDataProperty(PROP_SCALE_OPTIONS, "Scale Options", WidgetPropertyCategory.Position,
                new WidgetScaleData(this, true, true, false), "Set Scale Options"));
        addProperty(
                new StringProperty(PROP_WIDGET_UID, "Widget UID", WidgetPropertyCategory.Basic, new UID().toString()));
        // update the WUID saved in connections without triggering anything
        getProperty(PROP_WIDGET_UID).addPropertyChangeListener(evt -> {
            for (var connection1 : sourceConnections) {
                connection1.setPropertyValue(ConnectionModel.PROP_SRC_WUID, evt.getNewValue(), false);
            }
            for (var connection2 : targetConnections) {
                connection2.setPropertyValue(ConnectionModel.PROP_TGT_WUID, evt.getNewValue(), false);
            }
        });

        setPropertyVisibleAndSavable(PROP_WIDGET_UID, false, true);

        var descriptor = WidgetsService.getInstance().getWidgetDescriptor(getTypeID());
        String name;
        name = descriptor == null ? getTypeID().substring(getTypeID().lastIndexOf(".") + 1) : descriptor.getName();
        addProperty(new StringProperty(PROP_NAME, "Name", WidgetPropertyCategory.Basic, name));
        addProperty(new UnchangableStringProperty(PROP_WIDGET_TYPE, "Widget Type", WidgetPropertyCategory.Basic, name));

        addProperty(new UnsavableListProperty(PROP_SRC_CONNECTIONS, "Source Connections",
                WidgetPropertyCategory.Display, sourceConnections));
        setPropertyVisible(PROP_SRC_CONNECTIONS, false);

        addProperty(new UnsavableListProperty(PROP_TGT_CONNECTIONS, "Target Connections",
                WidgetPropertyCategory.Display, targetConnections));
        setPropertyVisible(PROP_TGT_CONNECTIONS, false);
    }

    /**
     * Configure the properties of the widget. Subclass should add new properties in this method.
     */
    protected abstract void configureProperties();

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public Set<String> getAllPropertyIDs() {
        return new HashSet<>(propertyMap.keySet());
    }

    public RGB getBackgroundColor() {
        return getRGBFromColorProperty(PROP_COLOR_BACKGROUND);
    }

    public RGB getBorderColor() {
        return getRGBFromColorProperty(PROP_BORDER_COLOR);
    }

    public BorderStyle getBorderStyle() {
        var i = (Integer) getCastedPropertyValue(PROP_BORDER_STYLE);
        return BorderStyle.values()[i];
    }

    public int getBorderWidth() {
        return (Integer) getCastedPropertyValue(PROP_BORDER_WIDTH);
    }

    public RGB getRGBFromColorProperty(String propID) {
        return ((OPIColor) getCastedPropertyValue(propID)).getRGBValue();
    }

    public Color getSWTColorFromColorProperty(String propID) {
        return ((OPIColor) getCastedPropertyValue(propID)).getSWTColor();
    }

    /**
     * Return the casted value of a property of this widget model.
     *
     * @param <TYPE>
     *            The return type of the property value.
     * @param propertyName
     *            The ID of the property.
     * @return The casted value of a property of this widget model.
     */
    @SuppressWarnings("unchecked")
    protected <TYPE> TYPE getCastedPropertyValue(String propertyName) {
        checkPropertyExist(propertyName);
        return (TYPE) getProperty(propertyName).getPropertyValue();
    }

    @Override
    public Object getEditableValue() {
        return this;
    }

    public ActionsInput getActionsInput() {
        return (ActionsInput) getCastedPropertyValue(PROP_ACTIONS);
    }

    public Rectangle getBounds() {
        return new Rectangle(getLocation(), getSize());
    }

    public OPIFont getFont() {
        return (OPIFont) getPropertyValue(PROP_FONT);
    }

    public RGB getForegroundColor() {
        return getRGBFromColorProperty(PROP_COLOR_FOREGROUND);
    }

    public Point getLocation() {
        return new Point(((Integer) getCastedPropertyValue(PROP_XPOS)).intValue(),
                ((Integer) getCastedPropertyValue(PROP_YPOS)).intValue());
    }

    public int getX() {
        return ((Integer) getCastedPropertyValue(PROP_XPOS)).intValue();
    }

    public int getY() {
        return ((Integer) getCastedPropertyValue(PROP_YPOS)).intValue();
    }

    public String getName() {
        return (String) getCastedPropertyValue(PROP_NAME);
    }

    public AbstractWidgetProperty getProperty(String prop_id) {
        if ((prop_id != null && propertyMap.containsKey(prop_id))) {
            return propertyMap.get(prop_id);
        }
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        var propArray = new IPropertyDescriptor[propertyDescriptors.size()];
        var i = 0;
        for (var p : propertyDescriptors.values()) {
            propArray[i++] = p;
        }

        return propArray;
    }

    @Override
    public Object getPropertyValue(Object id) {
        checkPropertyExist(id);
        return propertyMap.get(id).getPropertyValue();
    }

    public Object getRawPropertyValue(Object id) {
        checkPropertyExist(id);
        return propertyMap.get(id).getRawPropertyValue();
    }

    public LinkedHashMap<StringProperty, PVValueProperty> getPVMap() {
        return pvMap;
    }

    public RulesInput getRulesInput() {
        return (RulesInput) getPropertyValue(PROP_RULES);
    }

    public ScriptsInput getScriptsInput() {
        return (ScriptsInput) getCastedPropertyValue(PROP_SCRIPTS);
    }

    public Dimension getSize() {
        return new Dimension(((Integer) getCastedPropertyValue(PROP_WIDTH)).intValue(),
                ((Integer) getCastedPropertyValue(PROP_HEIGHT)).intValue());
    }

    public int getWidth() {
        return ((Integer) getCastedPropertyValue(PROP_WIDTH)).intValue();
    }

    public int getHeight() {
        return ((Integer) getCastedPropertyValue(PROP_HEIGHT)).intValue();
    }

    public String getTooltip() {
        return (String) getCastedPropertyValue(PROP_TOOLTIP);
    }

    public String getRawTooltip() {
        return (String) getRawPropertyValue(PROP_TOOLTIP);
    }

    public String getType() {
        return (String) getCastedPropertyValue(PROP_WIDGET_TYPE);
    }

    /**
     * @return the unique typeID of the model.
     */
    public abstract String getTypeID();

    /**
     * @return version of this widget model.
     */
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    /**
     * @return version read from opi file.
     */
    public Version getVersionOnFile() {
        if (versionOnFile == null) {
            return Version.emptyVersion;
        }
        return versionOnFile;
    }

    public String getWidgetType() {
        return (String) getCastedPropertyValue(PROP_WIDGET_TYPE);
    }

    public Boolean isEnabled() {
        return (Boolean) getCastedPropertyValue(PROP_ENABLED);
    }

    @Override
    public boolean isPropertySet(Object id) {
        return !getProperty((String) id).isDefaultValue();
    }

    public Boolean isVisible() {
        return (Boolean) getCastedPropertyValue(PROP_VISIBLE);
    }

    /**
     * Remove a property from the model.
     *
     * @param prop_id
     */
    public synchronized void removeProperty(String prop_id) {
        if (!propertyMap.containsKey(prop_id)) {
            return;
        }
        var property = propertyMap.get(prop_id);
        property.removeAllPropertyChangeListeners();
        if (property.isVisibleInPropSheet()) {
            propertyDescriptors.remove(prop_id);
        }
        propertyMap.remove(prop_id);
    }

    /**
     * Remove a PV p
     *
     * @param pvNamePropId
     * @param pvValuePropId
     */
    public void removePVProperty(String pvNamePropId, String pvValuePropId) {
        removeProperty(pvNamePropId);
        removeProperty(pvValuePropId);
        pvMap.remove(getProperty(pvNamePropId));
    }

    @Override
    public void resetPropertyValue(Object id) {
        setPropertyValue(id, getProperty((String) id).getDefaultValue());
    }

    /**
     * Scale location and size of the widget. If the widget needs to change its scale behavior, it should override
     * {@link #doScale(double, double)} instead of this method.
     *
     * @param widthRatio
     *            Ratio of width change.
     * @param heightRatio
     *            Ratio of height change.
     */
    public void scale(double widthRatio, double heightRatio) {
        if (originSize == null) {
            originSize = getSize();
        }
        if (originLocation == null) {
            originLocation = getLocation();
        }
        doScale(widthRatio, heightRatio);
        scaleConnections(widthRatio, heightRatio);
    }

    /**
     * The actual code that scaling the widget.
     *
     * @param widthRatio
     * @param heightRatio
     */
    protected void doScale(double widthRatio, double heightRatio) {
        setLocation((int) Math.round(originLocation.x * widthRatio), (int) Math.round(originLocation.y * heightRatio));
        if (getScaleOptions().isHeightScalable() || getScaleOptions().isWidthScalable()) {
            setSize(getScaledSize(widthRatio, heightRatio));
        }
    }

    /**
     * @param widthRatio
     * @param heightRatio
     */
    protected void scaleConnections(double widthRatio, double heightRatio) {
        for (var conn : getSourceConnections()) {
            if (conn.getPoints() != null && conn.getPoints().size() > 0) {
                var pl = conn.getOriginPoints().getCopy();
                for (var i = 0; i < pl.size(); i++) {
                    pl.setPoint(new Point((int) Math.round((pl.getPoint(i).x * widthRatio)),
                            (int) Math.round(pl.getPoint(i).y * heightRatio)), i);
                }
                conn.setPoints(pl);
            }
        }
    }

    /**
     * @return the original size before scaling
     */
    public Dimension getOriginSize() {
        if (originSize == null) {
            originSize = getSize();
        }
        return originSize;
    }

    /**
     * @return the original location before scaling
     */
    public Point getOriginLocation() {
        if (originLocation == null) {
            originLocation = getLocation();
        }
        return originLocation;
    }

    /**
     * Make necessary adjustment for widget compatibility between different versions.
     */
    public void processVersionDifference(Version boyVersionOnFile) {
        // update pv name
        if (UpgradeUtil.VERSION_WITH_PVMANAGER.compareTo(boyVersionOnFile) > 0) {
            if (propertyMap.containsKey(PROP_SCRIPTS)) {
                var scriptsInput = getScriptsInput();
                for (var sd : scriptsInput.getScriptList()) {
                    for (var tuple : sd.getPVList()) {
                        tuple.pvName = UpgradeUtil.convertUtilityPVNameToPM(tuple.pvName);
                    }
                }
                setPropertyValue(PROP_SCRIPTS, scriptsInput);
            }
            if (propertyMap.containsKey(PROP_RULES)) {
                var rulesInput = getRulesInput();
                for (var rd : rulesInput.getRuleDataList()) {
                    for (var tuple : rd.getPVList()) {
                        tuple.pvName = UpgradeUtil.convertUtilityPVNameToPM(tuple.pvName);
                    }
                }
                setPropertyValue(PROP_RULES, rulesInput);
            }
        }
    }

    public WidgetScaleData getScaleOptions() {
        return (WidgetScaleData) getPropertyValue(PROP_SCALE_OPTIONS);
    }

    public void setScaleOptions(boolean isWidthScalable, boolean isHeightScalable, boolean keepWHRatio) {
        setPropertyValue(PROP_SCALE_OPTIONS, new WidgetScaleData(this, isWidthScalable, isHeightScalable, keepWHRatio));
    }

    /**
     * Get the widget size after scaled.
     *
     * @param widthRatio
     *            Ratio of width change.
     * @param heightRatio
     *            Ratio of height change.
     * @return the new size.
     */
    protected Dimension getScaledSize(double widthRatio, double heightRatio) {
        var scaleOptions = getScaleOptions();
        int newW = originSize.width, newH = originSize.height;
        if (scaleOptions.isKeepWHRatio() && scaleOptions.isHeightScalable() && scaleOptions.isWidthScalable()) {
            if (widthRatio <= heightRatio) {
                newW = (int) Math.round(originSize.width * widthRatio);
                newH = originSize.height * newW / originSize.width;
            } else {
                newH = (int) Math.round(originSize.height * heightRatio);
                newW = originSize.width * newH / originSize.height;
            }
        } else {
            if (scaleOptions.isHeightScalable()) {
                newH = (int) Math.round(originSize.height * heightRatio);
            }
            if (scaleOptions.isWidthScalable()) {
                newW = (int) Math.round(originSize.width * widthRatio);
            }
        }
        return new Dimension(newW, newH);
    }

    public void setBackgroundColor(RGB color) {
        setPropertyValue(PROP_COLOR_BACKGROUND, color);
    }

    public void setEnabled(boolean enable) {
        setPropertyValue(PROP_ENABLED, enable);
    }

    public void setBorderColor(RGB color) {
        setPropertyValue(PROP_BORDER_COLOR, color);
    }

    public void setBorderStyle(BorderStyle borderStyle) {
        var i = 0;
        for (var bs : BorderStyle.values()) {
            if (borderStyle == bs) {
                break;
            }
            i++;
        }
        setPropertyValue(PROP_BORDER_STYLE, i);
    }

    public void setBorderWidth(int width) {
        setPropertyValue(PROP_BORDER_WIDTH, width);
    }

    public void setBounds(Rectangle bounds) {
        setLocation(bounds.getLocation());
        setSize(bounds.getSize());
    }

    public void setBounds(int x, int y, int width, int height) {
        setLocation(x, y);
        setSize(width, height);
    }

    public void setForegroundColor(RGB color) {
        setPropertyValue(PROP_COLOR_FOREGROUND, color);
    }

    public void setLocation(int x, int y) {
        setPropertyValue(PROP_XPOS, x);
        setPropertyValue(PROP_YPOS, y);
    }

    public void setLocation(Point point) {
        setLocation(point.x, point.y);
    }

    public void setName(String name) {
        setPropertyValue(PROP_NAME, name);
    }

    public void setPropertyDescription(String prop_id, String description) {
        if (getProperty(prop_id) == null) {
            return;
        }
        getProperty(prop_id).setDescription(description);
        if (propertyDescriptors.containsKey(prop_id)) {
            propertyDescriptors.put(prop_id, getProperty(prop_id).getPropertyDescriptor());
        }
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        checkPropertyExist(id);
        propertyMap.get(id).setPropertyValue(value);
    }

    public void setPropertyValue(Object id, Object value, boolean forceFire) {
        checkPropertyExist(id);
        propertyMap.get(id).setPropertyValue(value, forceFire);
    }

    /**
     * Set if property should be visible in property sheet. Note: this method will also make the invisible property not
     * savable to xml file. If the invisible property still needs to be saved, please use
     * {@link #setPropertyVisibleAndSavable(String, boolean, boolean)}.
     *
     * @param prop_id
     *            id of the property.
     * @param visible
     *            true if visible in
     */
    public void setPropertyVisible(String prop_id, boolean visible) {
        if (visible) {
            setPropertyVisibleAndSavable(prop_id, visible, true);
        } else {
            setPropertyVisibleAndSavable(prop_id, visible, false);
        }
    }

    /**
     * Set if property should be visible in property sheet and if savable to xml file.
     *
     * @param prop_id
     *            id of the property
     * @param visible
     *            true if visible in property sheet.
     * @param isSavable
     *            true if this property should be saved to xml file.
     */
    public void setPropertyVisibleAndSavable(String prop_id, boolean visible, boolean isSavable) {
        checkPropertyExist(prop_id);
        var property = propertyMap.get(prop_id);
        if (property.setVisibleInPropSheet(visible)) {
            if (visible) {
                propertyDescriptors.put(prop_id, property.getPropertyDescriptor());
            } else {
                propertyDescriptors.remove(prop_id);
            }
        }
        property.setSavable(isSavable);
    }

    public void setSize(Dimension dimension) {
        setSize(dimension.width, dimension.height);
    }

    public void setSize(int width, int height) {
        setPropertyValue(PROP_WIDTH, width);
        setPropertyValue(PROP_HEIGHT, height);
    }

    public void setWidth(int width) {
        setPropertyValue(PROP_WIDTH, width);
    }

    public void setHeight(int height) {
        setPropertyValue(PROP_HEIGHT, height);
    }

    public void setX(int x) {
        setPropertyValue(PROP_XPOS, x);
    }

    public void setY(int y) {
        setPropertyValue(PROP_YPOS, y);
    }

    public void setTooltip(String tooltip) {
        setPropertyValue(PROP_TOOLTIP, tooltip);
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(AbstractContainerModel parent) {
        this.parent = parent;
    }

    /**
     * Set the widget version read from opi file.
     *
     * @param versionOnFile
     */
    public void setVersionOnFile(Version versionOnFile) {
        this.versionOnFile = versionOnFile;
    }

    /**
     * @return the parent
     */
    public AbstractContainerModel getParent() {
        return parent;
    }

    /**
     * @return the root display model for this widget. null if its parent is not set yet.
     */
    public DisplayModel getRootDisplayModel() {
        return getRootDisplayModel(true);
    }

    /**
     * @param useLinkingContainersDisplayModel
     *            if one of the parents of this widget is a linking container use its display model
     * @return the root display model for this widget. null if its parent is not set yet.
     */
    public DisplayModel getRootDisplayModel(boolean useLinkingContainersDisplayModel) {
        var parent = getParent();
        if (parent == null) {
            if (this instanceof DisplayModel) {
                return (DisplayModel) this;
            } else {
                return null;
            }
        }

        while (!(parent instanceof DisplayModel)) {
            // If parent is a linking container,
            // use the display model of that
            if (useLinkingContainersDisplayModel && parent instanceof AbstractLinkingContainerModel) {
                var m = ((AbstractLinkingContainerModel) parent).getDisplayModel();
                if (m != null) {
                    return m;
                }
            }
            // Otherwise follow links up the parent chain
            parent = parent.getParent();
        }
        return (DisplayModel) parent;
    }

    /**
     * @return the nested depth of the widget in the model tree.
     */
    public int getNestedDepth() {
        // display model
        if (getParent() == null) {
            return 0;
        }
        var i = 1;
        var parent = getParent();
        while (!(parent instanceof DisplayModel)) {
            i++;
            parent = parent.getParent();
        }
        return i;
    }

    /**
     * @return the index of the widget in its parent's children list
     */
    public int getIndex() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getChildren().indexOf(this);
    }

    /**
     * @param executionMode
     *            the executionMode to set
     */
    public void setExecutionMode(ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    /**
     * @return the executionMode
     */
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    /**
     * Flip the widget figure horizontally.
     */
    public void flipHorizontally() {
    }

    /**
     * Flip the widget figure horizontally.
     *
     * @param centerX
     *            the center X coordinate
     */
    public void flipHorizontally(int centerX) {
        setX(2 * centerX - getX() - getWidth());
    }

    /**
     * Flip the widget figure vertically.
     */
    public void flipVertically() {
    }

    /**
     * Flip the widget figure horizontally.
     *
     * @param centerY
     *            the center Y coordinate
     */
    public void flipVertically(int centerY) {
        setY(2 * centerY - getY() - getHeight());
    }

    /**
     * Rotate the widget figure 90 degree.
     *
     * @param clockwise
     *            true if rotate clockwise. false if counterclockwise.
     */
    public void rotate90(boolean clockwise) {
        var x = getX();
        var y = getY();
        var h = getHeight();
        var w = getWidth();

        int newX, newY, newH, newW;

        newX = x + w / 2 - h / 2;
        newY = y + h / 2 - w / 2;
        newH = w;
        newW = h;

        setLocation(newX, newY);
        setSize(newW, newH);
    }

    /**
     * Rotate the widget figure 90 degree.
     *
     * @param clockwise
     *            true if rotate clockwise. false if counterclockwise.
     */
    public void rotate90(boolean clockwise, Point center) {
        // Point shiftedPoint = moveCoordinateToCenter(getLocation(), center);
        var x = getX() - center.x;
        var y = center.y - getY();
        var h = getHeight();
        var w = getWidth();

        int newX, newY, newH, newW;
        if (clockwise) {
            newX = y - h;
            newY = -x;
        } else {
            newX = -y;
            newY = x + w;
        }
        // Point rotatedPoint = recoverFromCenterCoordinate(new Point(newX, newY), center);
        newX = newX + center.x;
        newY = center.y - newY;
        newH = w;
        newW = h;

        setLocation(newX, newY);
        setSize(newW, newH);
    }

    /**
     * @return a copy list of source connections.
     */
    public List<ConnectionModel> getSourceConnections() {
        return new ArrayList<>(sourceConnections);
    }

    /**
     * @return a copy list of target connections.
     */
    public List<ConnectionModel> getTargetConnections() {
        return new ArrayList<>(targetConnections);
    }

    public String getWUID() {
        return (String) getPropertyValue(PROP_WIDGET_UID);
    }

    /**
     * Generate a new WUID for this widget.
     */
    public void generateNewWUID() {
        setPropertyValue(PROP_WIDGET_UID, new UID().toString());
    }
}
