/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

/**
 * Model for native button widget.
 *
 * @deprecated the native button is not used anymore. Instead use Style property of Action Button.
 *
 */
@Deprecated
public final class NativeButtonModel extends ActionButtonModel {

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.NativeButton";

    @Override
    protected void configureProperties() {
        super.configureProperties();
        removeProperty(PROP_BACKCOLOR_ALARMSENSITIVE);
        removeProperty(PROP_COLOR_BACKGROUND);
        removeProperty(PROP_ALARM_PULSING);
    }

    @Override
    public String getTypeID() {
        return ID;
    }
}
