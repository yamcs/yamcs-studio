/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.visualparts.RuntimePropertiesEditDialog;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * Configure widget properties on runtime.
 */
public class ConfigureRuntimePropertiesAction extends Action {

    private AbstractWidgetModel widgetModel;
    private Shell shell;

    public ConfigureRuntimePropertiesAction(Shell shell, AbstractWidgetModel widgetModel) {
        setText("Configure Runtime Properties...");
        setImageDescriptor(CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                "icons/settingRuntimeProperty.gif"));
        this.widgetModel = widgetModel;
        this.shell = shell;
    }

    @Override
    public void run() {
        var dialog = new RuntimePropertiesEditDialog(shell, widgetModel);
        if (dialog.open() == Window.OK) {
            for (var p : dialog.getOutput()) {
                widgetModel.setPropertyValue(p.property.getPropertyID(), p.tmpValue);
            }
        }
    }
}
