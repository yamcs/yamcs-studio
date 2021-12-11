/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.feedback.IGraphicalFeedbackFactory;
import org.csstudio.opibuilder.palette.MajorCategories;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * A service help to find the widget from extensions and help to maintain the widgets information.
 */
public final class WidgetsService {

    private static final String BOY_WIDGETS_PLUGIN_NAME = "org.csstudio.opibuilder";
    private static final String DEFAULT_CATEGORY = "Others";

    private static WidgetsService instance = null;

    private Map<String, WidgetDescriptor> allWidgetDescriptorsMap;
    private Map<String, List<String>> allCategoriesMap;
    private Map<String, IGraphicalFeedbackFactory> feedbackFactoriesMap;

    public synchronized static final WidgetsService getInstance() {
        if (instance == null) {
            instance = new WidgetsService();
        }
        return instance;
    }

    public WidgetsService() {
        feedbackFactoriesMap = new HashMap<>();
        allWidgetDescriptorsMap = new LinkedHashMap<>();
        allCategoriesMap = new LinkedHashMap<>();
        for (var mc : MajorCategories.values()) {
            allCategoriesMap.put(mc.toString(), new ArrayList<String>());
        }
        loadAllWidgets();
        loadAllFeedbackFactories();
    }

    /**
     * Load all widgets information from extensions.
     */
    private void loadAllWidgets() {
        var extReg = Platform.getExtensionRegistry();
        var confElements = extReg.getConfigurationElementsFor(OPIBuilderPlugin.EXTPOINT_WIDGET);
        var boyElements = new LinkedList<IConfigurationElement>();
        var otherElements = new LinkedList<IConfigurationElement>();
        // Sort elements. opibuilder.widgets should always appear first.
        for (var element : confElements) {
            if (element.getContributor().getName().equals(BOY_WIDGETS_PLUGIN_NAME)) {
                boyElements.add(element);
            } else {
                otherElements.add(element);
            }
        }
        boyElements.addAll(otherElements);
        for (var element : boyElements) {
            var typeId = element.getAttribute("typeId");
            var name = element.getAttribute("name");
            var icon = element.getAttribute("icon");
            var onlineHelpHtml = element.getAttribute("onlineHelpHtml"); //
            var pluginId = element.getDeclaringExtension().getNamespaceIdentifier();
            var description = element.getAttribute("description");
            if (description == null || description.trim().length() == 0) {
                description = "";
            }
            var category = element.getAttribute("category");
            if (category == null || category.trim().length() == 0) {
                category = DEFAULT_CATEGORY;
            }

            if (typeId != null) {
                var list = allCategoriesMap.get(category);
                if (list == null) {
                    list = new ArrayList<>();
                    allCategoriesMap.put(category, list);
                }
                // ensure no duplicates in the widgets palette
                if (!list.contains(typeId)) {
                    list.add(typeId);
                }
                allWidgetDescriptorsMap.put(typeId, new WidgetDescriptor(element, typeId, name, description, icon,
                        category, pluginId, onlineHelpHtml));
            }
        }

        // sort the widget in the categories
        // for(List<String> list : allCategoriesMap.values())
        // Collections.sort(list);
    }

    private void loadAllFeedbackFactories() {
        var extReg = Platform.getExtensionRegistry();
        var confElements = extReg.getConfigurationElementsFor(OPIBuilderPlugin.EXTPOINT_FEEDBACK_FACTORY);
        for (var element : confElements) {
            var typeId = element.getAttribute("typeId");
            if (typeId != null) {
                try {
                    feedbackFactoriesMap.put(typeId,
                            (IGraphicalFeedbackFactory) element.createExecutableExtension("class"));
                } catch (CoreException e) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "Cannot load feedback provider", e);
                }
            }
        }
    }

    /**
     * @return map which contains all the name of the categories and the widgets under them. The widgets list has been
     *         sorted by string.
     */
    public final Map<String, List<String>> getAllCategoriesMap() {
        return allCategoriesMap;
    }

    public final WidgetDescriptor getWidgetDescriptor(String typeId) {
        return allWidgetDescriptorsMap.get(typeId);
    }

    public final String[] getAllWidgetTypeIDs() {
        return allWidgetDescriptorsMap.keySet().toArray(new String[0]);
    }

    public final IGraphicalFeedbackFactory getWidgetFeedbackFactory(String typeId) {
        return feedbackFactoriesMap.get(typeId);
    }
}
