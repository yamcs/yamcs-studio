/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.StringListProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.eclipse.swt.graphics.RGB;

/**
 * The model for combo widget.
 */
public class ComboModel extends AbstractPVWidgetModel {

    public final String ID = "org.csstudio.opibuilder.widgets.combo";
    /**
     * Items of the combo.
     */
    public static final String PROP_ITEMS = "items";

    /**
     * True if items are read from the input PV which must be an Enum PV.
     */
    public static final String PROP_ITEMS_FROM_PV = "items_from_pv";

    public ComboModel() {
        setBackgroundColor(new RGB(255, 255, 255));
        setForegroundColor(new RGB(0, 0, 0));
        setScaleOptions(true, false, false);
    }

    @Override
    protected void configureProperties() {
        addProperty(
                new StringListProperty(PROP_ITEMS, "Items", WidgetPropertyCategory.Behavior, new ArrayList<String>()));

        addProperty(new BooleanProperty(PROP_ITEMS_FROM_PV, "Items From PV", WidgetPropertyCategory.Behavior, false));
    }

    @SuppressWarnings("unchecked")
    public List<String> getItems() {
        return (List<String>) getPropertyValue(PROP_ITEMS);
    }

    public boolean isItemsFromPV() {
        return (Boolean) getPropertyValue(PROP_ITEMS_FROM_PV);
    }

    @Override
    public String getTypeID() {
        return ID;
    }

}
