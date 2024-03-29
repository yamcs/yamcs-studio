/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.model;

import org.osgi.framework.Version;

/**
 * The abstract widget model for all PV related widgets.
 */
public abstract class AbstractPVWidgetModel extends AbstractWidgetModel implements IPVWidgetModel {

    private PVWidgetModelDelegate delegate;

    public AbstractPVWidgetModel() {
    }

    public PVWidgetModelDelegate getDelegate() {
        if (delegate == null) {
            delegate = new PVWidgetModelDelegate(this);
        }
        return delegate;
    }

    @Override
    protected void configureBaseProperties() {
        super.configureBaseProperties();
        getDelegate().configureBaseProperties();
    }

    @Override
    public void processVersionDifference(Version boyVersionOnFile) {
        super.processVersionDifference(boyVersionOnFile);
        delegate.processVersionDifference(boyVersionOnFile);
    }

    @Override
    public boolean isBorderAlarmSensitve() {
        return getDelegate().isBorderAlarmSensitve();
    }

    @Override
    public boolean isForeColorAlarmSensitve() {
        return getDelegate().isForeColorAlarmSensitve();
    }

    @Override
    public boolean isBackColorAlarmSensitve() {
        return getDelegate().isBackColorAlarmSensitve();
    }

    @Override
    public boolean isAlarmPulsing() {
        return getDelegate().isAlarmPulsing();
    }

    @Override
    public String getPVName() {
        return getDelegate().getPVName();
    }

    /**
     * Override to prevent unsightly unresolved macros including $(pv_name) or $(pv_value).
     */
    @Override
    public String getTooltip() {
        var rawTooltip = getRawTooltip();
        if ((rawTooltip.contains(PROP_PVNAME) || rawTooltip.contains(PROP_PVVALUE)) && (getPVName().equals(""))) {
            return "";
        } else {
            return super.getTooltip();
        }
    }
}
