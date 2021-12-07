/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin implements BundleActivator {

    /** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.ui.util";

    // The shared instance
    private static Activator plugin;

    private BundleContext context;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setPlugin(this);
        this.context = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        setPlugin(this);
        super.stop(context);
    }

    public BundleContext getContext() {
        return context;
    }

    /** Static setter to avoid FindBugs warning */
    private static void setPlugin(Activator the_plugin) {
        plugin = the_plugin;
    }

    /** @eturn The shared instance. */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Obtain image descriptor for image in plugin
     * 
     * @param path
     *            Path to image within plugin
     * @return {@link ImageDescriptor}
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
    }
}
