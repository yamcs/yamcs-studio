/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.editparts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.util.AlarmRepresentationScheme;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;

/**
 * The handler help a widget to handle the pv connection event such as PVs' disconnection, connection recovered. It will
 * show a disconnect border on the widget if any one of the PVs is disconnected. The detailed disconnected information
 * will be displayed as tooltip.
 */
public class ConnectionHandler {

    private final class PVConnectionListener implements IPVListener {

        private boolean lastValueIsNull;

        @Override
        public void valueChanged(IPV pv) {
            if (lastValueIsNull && pv.getValue() != null) {
                lastValueIsNull = false;
                widgetConnectionRecovered(pv, true);
            }
        }

        @Override
        public void connectionChanged(IPV pv) {
            if (pv.isConnected()) {
                lastValueIsNull = (pv.getValue() == null);
                widgetConnectionRecovered(pv, false);
            } else {
                markWidgetAsDisconnected(pv);
            }
        }
    }

    private Map<String, IPV> pvMap;

    /**
     * True if all PVs are connected.
     */
    private boolean connected;

    private String toolTipText;

    private IFigure figure;

    private AbstractWidgetModel widgetModel;
    private Display display;

    protected AbstractBaseEditPart editPart;

    private boolean hasNullValue;

    /**
     * @param editpart
     *            the widget editpart to be handled.
     */
    public ConnectionHandler(AbstractBaseEditPart editpart) {
        editPart = editpart;
        figure = editpart.getFigure();
        widgetModel = editpart.getWidgetModel();
        display = editpart.getViewer().getControl().getDisplay();
        pvMap = new ConcurrentHashMap<>();
        connected = true;
    }

    /**
     * Add a PV to this handler, so its connection event can be handled.
     * 
     * @param pvName
     *            name of the PV.
     * @param pv
     *            the PV object.
     */
    public void addPV(String pvName, IPV pv) {
        pvMap.put(pvName, pv);
        markWidgetAsDisconnected(pv);
        pv.addListener(new PVConnectionListener());
    }

    public void removePV(String pvName) {
        if (pvMap == null) {
            return;
        }
        pvMap.remove(pvName);
    }

    private void refreshModelTooltip() {
        var sb = new StringBuilder();
        for (var entry : pvMap.entrySet()) {
            if (!entry.getValue().isConnected()) {
                sb.append(entry.getKey() + " is disconnected.\n");
            } else if (entry.getValue().getValue() == null) {
                sb.append(entry.getKey() + " has null value.\n");
            }
        }
        if (sb.length() > 0) {
            sb.append("------------------------------\n");
            toolTipText = sb.toString();
        } else {
            toolTipText = "";
        }
    }

    /**
     * Mark a widget as disconnected.
     */
    protected void markWidgetAsDisconnected(IPV pv) {
        refreshModelTooltip();
        if (!connected) {
            return;
        }
        connected = false;
        // Making this task execute in UI Thread
        // It will also delay the disconnect marking requested during widget activating
        // to execute after widget is fully activated.
        UIBundlingThread.getInstance().addRunnable(display, () -> {
            figure.setBorder(AlarmRepresentationScheme.getDisconnectedBorder());
        });
    }

    /**
     * Update the widget when a PV' connection is recovered.
     * 
     * @param valueChangedFromNull
     *            true if this is called because value changed from null value.
     */
    protected void widgetConnectionRecovered(IPV pv, boolean valueChangedFromNull) {

        if (connected && !valueChangedFromNull) {
            return;
        }
        var allConnected = true;
        hasNullValue = false;
        for (var pv2 : pvMap.values()) {
            allConnected &= pv2.isConnected();
            hasNullValue |= (pv2.getValue() == null);
        }
        refreshModelTooltip();
        if (allConnected) {
            connected = true;
            UIBundlingThread.getInstance().addRunnable(display, () -> {
                if (hasNullValue) {
                    figure.setBorder(AlarmRepresentationScheme.getInvalidBorder(BorderStyle.DOTTED));
                } else {
                    figure.setBorder(editPart.calculateBorder());
                }

            });
        }
    }

    /**
     * @return true if all pvs are connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return true if one or some PVs have null values.
     */
    public boolean isHasNullValue() {
        return hasNullValue;
    }

    /**
     * @return the map with all PVs. It is not allowed to change the Map.
     */
    public Map<String, IPV> getAllPVs() {
        return pvMap;
    }

    public String getToolTipText() {
        return toolTipText;
    }
}
