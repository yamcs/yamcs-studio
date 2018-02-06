/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.singlesource;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * API for obtaining single-source helper
 *
 * <p>
 * Acts as plugin activator
 *
 * @author Kay Kasemir
 */
public class SingleSourcePlugin implements BundleActivator {

    private static ResourceHelper resources;

    private static UIHelper ui;

    @Override
    public void start(BundleContext context) throws Exception {
        resources = new ResourceHelper();
        ui = new UIHelper();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        resources = null;
    }

    public static ResourceHelper getResourceHelper() {
        return resources;
    }

    public static UIHelper getUIHelper() {
        return ui;
    }
}
