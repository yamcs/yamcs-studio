/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.palette;

import java.util.List;
import java.util.Map.Entry;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The factory help to create the palette.
 */
public class OPIEditorPaletteFactory {

    public static PaletteRoot createPalette() {
        var palette = new PaletteRoot();
        createToolsGroup(palette);
        createPaletteContents(palette);
        return palette;
    }

    private static void createToolsGroup(PaletteRoot palette) {
        var toolbar = new PaletteToolbar("Tools");
        // Add a selection tool to the group
        ToolEntry tool = new PanningSelectionToolEntry();
        toolbar.add(tool);
        palette.setDefaultEntry(tool);

        tool = new ConnectionCreationToolEntry("Connection", "Create a connection between widgets",
                new CreationFactory() {

                    @Override
                    public Object getObjectType() {
                        return null;
                    }

                    @Override
                    public Object getNewObject() {
                        return null;
                    }
                },
                CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                        "icons/connection_s16.gif"),
                CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                        "icons/connection_s24.gif"));
        toolbar.add(tool);
        palette.add(toolbar);

    }

    private static void createPaletteContents(PaletteRoot palette) {
        var categoriesMap = WidgetsService.getInstance().getAllCategoriesMap();
        var hiddenWidgets = PreferencesHelper.getHiddenWidgets();

        for (Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            var categoryDrawer = new PaletteDrawer(entry.getKey());
            for (String typeId : entry.getValue()) {
                if (hiddenWidgets.contains(typeId)) {
                    continue;
                }
                var widgetDescriptor = WidgetsService.getInstance().getWidgetDescriptor(typeId);
                var icon = CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(widgetDescriptor.getPluginId(),
                        widgetDescriptor.getIconPath());
                var widgetEntry = new CombinedTemplateCreationEntry(widgetDescriptor.getName(),
                        widgetDescriptor.getDescription(), new WidgetCreationFactory(widgetDescriptor), icon, icon);

                var feedbackFactory = WidgetsService.getInstance()
                        .getWidgetFeedbackFactory(widgetDescriptor.getTypeID());
                if (feedbackFactory != null && feedbackFactory.getCreationTool() != null) {
                    widgetEntry.setToolClass(feedbackFactory.getCreationTool());
                }
                categoryDrawer.add(widgetEntry);
            }
            palette.add(categoryDrawer);
        }
    }

}
