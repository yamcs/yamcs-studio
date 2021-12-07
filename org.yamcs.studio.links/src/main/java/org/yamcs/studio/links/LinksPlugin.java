/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.links;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class LinksPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.yamcs.studio.links";

    private static LinksPlugin plugin;

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

    public static LinksPlugin getDefault() {
        return plugin;
    }

    public ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
