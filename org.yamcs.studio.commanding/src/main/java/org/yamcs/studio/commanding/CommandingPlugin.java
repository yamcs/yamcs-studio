/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CommandingPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.commanding";

    private static CommandingPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    public static CommandingPlugin getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return ImageDescriptor.createFromURL(getBundle().getEntry(path));
    }

    public IDialogSettings getCommandHistoryTableSettings() {
        var settings = getDialogSettings();
        var section = settings.getSection("cmdhist-table");
        if (section == null) {
            section = settings.addNewSection("cmdhist-table");
        }
        return section;
    }
}
