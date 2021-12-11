/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.editor.base;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editor.SchemaDecorator;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.yamcs.studio.core.YamcsPlugin;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.editor";

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        plugin = this;

        // Trigger other bundles
        YamcsPlugin.getDefault();

        OPIBuilderPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
            if (event.getProperty().equals(PreferencesHelper.SCHEMA_OPI)) {
                var decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
                decoratorManager.update(SchemaDecorator.ID);
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }
}
