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

import org.csstudio.ui.util.thread.UIBundlingThread;
import org.yamcs.studio.data.IPV;

/**
 * The connection handler for PV widget. It will set the enable state of the widget based on control PV's connectivity.
 */
public class PVWidgetConnectionHandler extends ConnectionHandler {

    /**
     * @param editpart
     *            the editpart must implemented {@link IPVWidgetEditpart}
     */
    public PVWidgetConnectionHandler(AbstractBaseEditPart editpart) {
        super(editpart);
    }

    @Override
    protected void markWidgetAsDisconnected(IPV pv) {
        super.markWidgetAsDisconnected(pv);
        var pvWidgetEditpart = (IPVWidgetEditpart) editPart;
        var controlPV = pvWidgetEditpart.getControlPV();
        if (controlPV != null && controlPV == pv) {
            UIBundlingThread.getInstance().addRunnable(editPart.getRoot().getViewer().getControl().getDisplay(),
                    () -> pvWidgetEditpart.setControlEnabled(false));
        }
    }

    // @Override
    // protected void widgetConnectionRecovered(PV pv) {
    // if(isConnected())
    // return;
    // super.widgetConnectionRecovered(pv);
    // final PV controlPV = ((IPVWidgetEditpart)editPart).getControlPV();
    // if(controlPV != null && controlPV == pv){
    // UIBundlingThread.getInstance().addRunnable(
    // editPart.getRoot().getViewer().getControl().getDisplay(),
    // new Runnable() {
    // public void run() {
    // editPart.getFigure().setEnabled(
    // editPart.getWidgetModel().isEnabled()
    // && controlPV.isWriteAllowed());
    // }
    // });
    // }
    //
    // }
}
