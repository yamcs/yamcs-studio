/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.persistence.XMLUtil;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.csstudio.opibuilder.widgets.model.TabModel;
import org.csstudio.opibuilder.widgets.model.TabModel.TabProperty;
import org.eclipse.osgi.util.NLS;

/**
 * The tab item, which host all the properties data for a tab item.
 */
public class TabItem {

    private GroupingContainerModel groupingContainerModel;

    private Map<TabProperty, Object> propertyMap;

    /**
     * The tab item will be initialized with the corresponding tab properties value in tab model.
     */
    public TabItem(TabModel tabModel, int index, GroupingContainerModel groupingContainerModel) {
        this.groupingContainerModel = groupingContainerModel;
        propertyMap = new HashMap<>();
        injectPropertiesValue(tabModel, index);
    }

    public TabItem(TabModel tabModel, int tabIndex) {
        groupingContainerModel = TabEditPart.createGroupingContainer();
        propertyMap = new HashMap<>();
        injectPropertiesValue(tabModel, 0);
        setPropertyValue(TabProperty.TITLE, NLS.bind("Tab {0}", tabIndex));
    }

    private TabItem(GroupingContainerModel groupingContainerModel, Map<TabProperty, Object> propertyMap) {
        this.groupingContainerModel = groupingContainerModel;
        this.propertyMap = propertyMap;
    }

    public void setPropertyValue(TabProperty tabProperty, Object value) {
        if (tabProperty == TabProperty.TITLE) {
            groupingContainerModel.setName((String) value);
        }
        if (tabProperty == TabProperty.ENABLED) {
            // for (var model : groupingContainerModel.getAllDescendants())
            // model.setEnabled((Boolean) value);
            groupingContainerModel.setEnabled((Boolean) value);
        }
        propertyMap.put(tabProperty, value);
    }

    /**
     * Copy the properties from TabModel to this tab item.
     */
    public void injectPropertiesValue(TabModel tabModel, int index) {
        for (var tabProperty : TabProperty.values()) {
            propertyMap.put(tabProperty, tabModel.getTabPropertyValue(index, tabProperty));
        }
    }

    /**
     * Copy the default properties value from TabModel to this tab item.
     *
     * @param index
     *            the index of the tab item.
     */
    public void injectDefaultPropertiesValue(int index) {
        var tempModel = new TabModel();
        for (var tabProperty : TabProperty.values()) {
            var propID = TabModel.makeTabPropID(tabProperty.propIDPre, index);
            propertyMap.put(tabProperty, tempModel.getProperty(propID).getDefaultValue());
        }
    }

    public GroupingContainerModel getGroupingContainerModel() {
        return groupingContainerModel;
    }

    public void setGroupingContainerModel(GroupingContainerModel groupingContainerModel) {
        this.groupingContainerModel = groupingContainerModel;
    }

    /**
     * @return A copy of this tab.
     * @throws Exception
     */
    public TabItem getCopy() throws Exception {
        var xmlString = XMLUtil.widgetToXMLString(groupingContainerModel, false);
        var newGroupingContainerModel = (GroupingContainerModel) XMLUtil.XMLStringToWidget(xmlString);
        var newPropertyMap = new HashMap<TabProperty, Object>();
        for (var entry : propertyMap.entrySet()) {
            newPropertyMap.put(entry.getKey(), entry.getValue());
        }

        return new TabItem(newGroupingContainerModel, newPropertyMap);
    }

    public Object getPropertyValue(TabProperty tabProperty) {
        return propertyMap.get(tabProperty);
    }
}
