/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.editparts;

import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.vtype.VType;

public interface IPVWidgetEditpart {

    public interface ISetPVValueListener {
        /**
         * Called before setting PV Value;
         *
         * @param pvPropId
         * @param value
         */
        void beforeSetPVValue(String pvPropId, Object value);
    }

    /**
     * @return A String array with all PV names from PV properties. It only returns the visible and nonempty PV
     *         properties.
     */
    String[] getAllPVNames();

    /**
     * @return the control PV. null if no control PV on this widget.
     */
    IPV getControlPV();

    /**
     * @return the major PV.
     */
    IPV getPV();

    /**
     * @return name of the major PV.
     */
    String getPVName();

    /**
     * Get the pv by PV property id.
     *
     * @param pvPropId
     *            the PV property id.
     * @return the corresponding pv for the pvPropId. null if the pv doesn't exist.
     */
    IPV getPV(String pvPropId);

    /**
     * Get value from one of the attached PVs.
     *
     * @param pvPropId
     *            the property id of the PV. It is "pv_name" for the main PV.
     * @return the value of the PV.
     */
    VType getPVValue(String pvPropId);

    /**
     * Set PV to given value. Should accept Double, Double[], Integer, String, maybe more.
     *
     * @param pvPropId
     * @param value
     */
    void setPVValue(String pvPropId, Object value);

    void addSetPVValueListener(ISetPVValueListener listener);

    void setControlEnabled(boolean enabled);

    boolean isPVControlWidget();
}
