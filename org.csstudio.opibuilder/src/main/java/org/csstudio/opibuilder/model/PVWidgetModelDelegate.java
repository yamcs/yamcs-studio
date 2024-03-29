/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.model;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.PVNameProperty;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.UpgradeUtil;
import org.osgi.framework.Version;

/**
 * The model delegate for widgets have PV Name property.
 */
public class PVWidgetModelDelegate implements IPVWidgetModel {

    AbstractWidgetModel model;

    public PVWidgetModelDelegate(AbstractWidgetModel model) {
        this.model = model;
    }

    public void configureBaseProperties() {
        model.addPVProperty(new PVNameProperty(PROP_PVNAME, "PV Name", WidgetPropertyCategory.Basic, ""),
                new PVValueProperty(PROP_PVVALUE, null));

        model.addProperty(new BooleanProperty(PROP_BORDER_ALARMSENSITIVE, "Alarm Sensitive",
                WidgetPropertyCategory.Border, true));
        model.addProperty(new BooleanProperty(PROP_FORECOLOR_ALARMSENSITIVE, "ForeColor Alarm Sensitive",
                WidgetPropertyCategory.Display, false));
        model.addProperty(new BooleanProperty(PROP_BACKCOLOR_ALARMSENSITIVE, "BackColor Alarm Sensitive",
                WidgetPropertyCategory.Display, false));
        model.addProperty(
                new BooleanProperty(PROP_ALARM_PULSING, "Alarm Pulsing", WidgetPropertyCategory.Display, false));

        model.setTooltip("$(" + PROP_PVNAME + ")\n" + "$(" + PROP_PVVALUE + ")");
    }

    @Override
    public boolean isBorderAlarmSensitve() {
        if (model.getProperty(PROP_BORDER_ALARMSENSITIVE) == null) {
            return false;
        }
        return (Boolean) model.getCastedPropertyValue(PROP_BORDER_ALARMSENSITIVE);
    }

    @Override
    public boolean isForeColorAlarmSensitve() {
        if (model.getProperty(PROP_FORECOLOR_ALARMSENSITIVE) == null) {
            return false;
        }
        return (Boolean) model.getCastedPropertyValue(PROP_FORECOLOR_ALARMSENSITIVE);
    }

    @Override
    public boolean isBackColorAlarmSensitve() {
        if (model.getProperty(PROP_BACKCOLOR_ALARMSENSITIVE) == null) {
            return false;
        }
        return (Boolean) model.getCastedPropertyValue(PROP_BACKCOLOR_ALARMSENSITIVE);
    }

    @Override
    public boolean isAlarmPulsing() {
        if (model.getProperty(PROP_ALARM_PULSING) == null) {
            return false;
        }
        return (Boolean) model.getCastedPropertyValue(PROP_ALARM_PULSING);
    }

    @Override
    public String getPVName() {
        return (String) model.getCastedPropertyValue(PROP_PVNAME);
    }

    public void processVersionDifference(Version boyVersionOnFile) {
        if (UpgradeUtil.VERSION_WITH_PVMANAGER.compareTo(boyVersionOnFile) > 0) {
            model.setPropertyValue(PROP_PVNAME, UpgradeUtil.convertUtilityPVNameToPM(getPVName()));
        }
    }
}
