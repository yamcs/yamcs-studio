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

import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.BoolSwitchModel;
import org.csstudio.swt.widgets.figures.BoolSwitchFigure;
import org.eclipse.draw2d.IFigure;

/**
 * Boolean Switch EditPart
 */
public class BoolSwitchEditPart extends AbstractBoolControlEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        var boolSwitch = new BoolSwitchFigure();

        initializeCommonFigureProperties(boolSwitch, model);
        boolSwitch.setEffect3D(model.isEffect3D());
        return boolSwitch;
    }

    @Override
    public BoolSwitchModel getWidgetModel() {
        return (BoolSwitchModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerCommonPropertyChangeHandlers();

        // effect 3D
        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                var boolSwitch = (BoolSwitchFigure) refreshableFigure;
                boolSwitch.setEffect3D((Boolean) newValue);
                return true;
            }
        };
        setPropertyChangeHandler(BoolSwitchModel.PROP_EFFECT3D, handler);
    }
}
