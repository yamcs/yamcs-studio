/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.util;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;

public final class SchemaService {

    private static SchemaService instance;
    private Map<String, AbstractWidgetModel> schemaWidgetsMap;

    /*
     * Instantiating the schema service with the modal process dialog uses the UI thread.
     * When process dialog is open in UI thread it interferes with connection which is drawn from/to linking containers.
     *
     * This causes bug with the connections not being displayed.
     *
     * Instance of SchemaService without dialog is created from first instance of WidgetNodeEditPolicy
    */

    private SchemaService() {
        schemaWidgetsMap = new HashMap<>();
        reload();
    }

    public static final synchronized SchemaService getInstance() {
        if (instance == null) {
            instance = new SchemaService();
        }
        return instance;
    }

    public void reload() {
        schemaWidgetsMap.clear();
        var schemaOPI = PreferencesHelper.getSchemaOPIPath();
        if (schemaOPI == null || schemaOPI.isEmpty()) {
            return;
        }
        loadSchema(schemaOPI);
    }

    public void loadSchema(IPath schemaOPI) {
        try (var inputStream = ResourceUtil.pathToInputStream(schemaOPI)) {
            var displayModel = new DisplayModel(schemaOPI);
            XMLUtil.fillDisplayModelFromInputStream(inputStream, displayModel, Display.getDefault());
            schemaWidgetsMap.put(displayModel.getTypeID(), displayModel);
            loadModelFromContainer(displayModel);
            if (!displayModel.getConnectionList().isEmpty()) {
                schemaWidgetsMap.put(ConnectionModel.ID, displayModel.getConnectionList().get(0));
            }
        } catch (FileNotFoundException e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Cannot locate OPI Schema: " + schemaOPI, e);
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to load OPI Schema: " + schemaOPI, e);
        }
    }

    private void loadModelFromContainer(AbstractContainerModel containerModel) {
        for (var model : containerModel.getChildren()) {
            // always add only the first model of its type that is found
            // the main container might contain several instances of the same widget
            // (e.g. GroupingContainer can appear multiple times; it is by default the base
            // layer of a tab and sash - we don't want the tab to override our container settings)
            if (!schemaWidgetsMap.containsKey(model.getTypeID())) {
                schemaWidgetsMap.put(model.getTypeID(), model);
            }
            if (model instanceof AbstractContainerModel) {
                loadModelFromContainer((AbstractContainerModel) model);
            }
        }
    }

    public void applySchema(AbstractWidgetModel widgetModel) {
        if (schemaWidgetsMap.isEmpty()) {
            return;
        }
        if (schemaWidgetsMap.containsKey(widgetModel.getTypeID())) {
            var schemaWidgetModel = schemaWidgetsMap.get(widgetModel.getTypeID());
            for (var id : schemaWidgetModel.getAllPropertyIDs()) {
                widgetModel.setPropertyValue(id, schemaWidgetModel.getPropertyValue(id), false);
            }
        }
    }

    /**
     * Return the default property value of the widget when it is created.
     * 
     * @param typeId
     *            typeId of the widget.
     * @param propId
     *            propId of the property.
     */
    public Object getDefaultPropertyValue(String typeId, String propId) {
        if (schemaWidgetsMap.containsKey(typeId)) {
            return schemaWidgetsMap.get(typeId).getPropertyValue(propId);
        }
        var desc = WidgetsService.getInstance().getWidgetDescriptor(typeId);
        if (desc != null) {
            return desc.getWidgetModel().getPropertyValue(propId);
        }
        if (typeId.equals(ConnectionModel.ID)) {
            return new ConnectionModel(null).getPropertyValue(propId);
        }
        return null;
    }
}
