/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.datadefinition;

import java.util.List;

import org.csstudio.opibuilder.model.AbstractWidgetModel;

/**
 * The data for coping properties.
 */
public class PropertiesCopyData {

    private AbstractWidgetModel widgetModel;

    private List<String> propIDList;

    public PropertiesCopyData(AbstractWidgetModel widgetModel, List<String> propIDList) {
        this.widgetModel = widgetModel;
        this.propIDList = propIDList;
    }

    /**
     * @return the widgetModel
     */
    public final AbstractWidgetModel getWidgetModel() {
        return widgetModel;
    }

    /**
     * @param widgetModel
     *            the widgetModel to set
     */
    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
    }

    /**
     * @return the propIDList
     */
    public final List<String> getPropIDList() {
        return propIDList;
    }

    /**
     * @param propIDList
     *            the propIDList to set
     */
    public void setPropIDList(List<String> propIDList) {
        this.propIDList = propIDList;
    }

}
